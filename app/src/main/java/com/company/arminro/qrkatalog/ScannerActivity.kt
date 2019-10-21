package com.company.arminro.qrkatalog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.company.arminro.qrkatalog.helpers.getCurrentDateTimeString
import com.company.arminro.qrkatalog.model.CodeData
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_scanner.*
import kotlinx.android.synthetic.main.data_details.view.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler  {
    private var cameraId: Int = 0
    private var mScannerView: ZXingScannerView? = null
    private var dataToUpdate: CodeData? = null
    private var dialogOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val extras = intent.extras

        // if the activity sent an object to work with, this is an update instead of adding a new item
        /* having a mechanism to explicitly tell the scanner activity that it is performing an update
        * may be more elegant, but the scanner always scans a code then returns the result*/

        dataToUpdate = extras?.getParcelable(getString(R.string.qr_data_intent_extra)) as? CodeData

        fab.setOnClickListener { view ->
            // todo: how to fire only on fab press
        }

        title = "QR Scanner"
        cameraId = getCameraId()

        mScannerView = ZXingScannerView(this)
        val preview = findViewById<FrameLayout>(R.id.camera_preview)
        preview.addView(mScannerView)
    }

    override fun onResume() {
        super.onResume()

        // we start the cam only in resume, since it always executes when we open the app
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mScannerView?.startCamera(cameraId)
            mScannerView?.setResultHandler(this)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), resources.getInteger(R.integer.camera_permission) )
        }
    }

    override fun onPause() {
        // always called when the app goes to background
        super.onPause()
        mScannerView?.stopCamera()
    }





    override fun handleResult(rawResult: Result) {
        if (rawResult.text != null && rawResult.text.isNotEmpty() && fab.isPressed) {
            // parsing the text only if it is valid
            val text = rawResult.text
            var resultData: CodeData? = validateResultString(text)
            resultData?.timestampCreated = getCurrentDateTimeString()

            if (resultData != null && !dialogOpen) {

                promptUserToSaveData(resultData)
            }
            else if(fab.isPressed){
                Toast.makeText(this, "The loaded QR Code is not valid", Toast.LENGTH_SHORT).show()
            }

        }

        mScannerView?.resumeCameraPreview(this)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            resources.getInteger(R.integer.camera_permission) -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // start the cam only when we have the necessary permission to it
                    mScannerView?.startCamera(cameraId)
                    mScannerView?.setResultHandler(this)

                } else {
                    Toast.makeText(this,"Please provide the following permission: " + Manifest.permission.CAMERA,Toast.LENGTH_LONG)
                    this.finishAffinity()
                }
                return
            }
        }
    }


    // getting the first camera facing backwards
    private fun getCameraId(): Int {
        var i = 0
        try {
            i = 0
            while (i < Camera.getNumberOfCameras()) {
                var info = Camera.CameraInfo()
                Camera.getCameraInfo(i, info)
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Log.println(1, "CAM", "CAMERA CAPTURED")
                    break
                }
                i++
            }
        } catch (e: Exception) {
            Log.e("CAM", "Could not start the camera: " + e.message)
        }
        return i
    }

    private fun promptUserToSaveData(resultData: CodeData) {
        dialogOpen = true
        // asking the user if s/he wants to save the valid data
        val mDialogView = createDialogView(resultData)

        // this is actually cheating on kotlin null check
        var titleString = "Save the following?"
        var confirmString = "Save"

        if(dataToUpdate != null){
            titleString = "Edit existing with the following?"
            confirmString = "Perform edit"
        }


        buildDialog(mDialogView, titleString, confirmString, resultData)
            ?.show()
    }

    private fun buildDialog(
        mDialogView: View?,
        titleString: String,
        confirmString: String,
        resultData: CodeData
    ): AlertDialog.Builder? {
        mScannerView?.stopCamera()
        mScannerView?.isActivated = false
        return AlertDialog.Builder(this@ScannerActivity)
            .setView(mDialogView)
            .setTitle(titleString)
            .setPositiveButton(confirmString) { _, _ ->

                if(dataToUpdate != null){
                    resultData.id = dataToUpdate!!.id
                }

                val intent = Intent(this@ScannerActivity, MainActivity::class.java)
                    .putExtra(getString(R.string.qr_data_intent_extra), resultData)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                mScannerView?.startCamera()
                mScannerView?.isActivated = true
                dialogOpen = false
            }
    }

    private fun createDialogView(resultData: CodeData): View? {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.data_details, null)
        mDialogView.companyField_details.text = resultData.companyName
        mDialogView.description_details.text = resultData.description
        mDialogView.fromField_details.text = resultData.source
        mDialogView.toField_details.text = resultData.destination
        mDialogView.timestamp_details.text = resultData.timestampCreated
        return mDialogView
    }

    private fun validateResultString(text: String?): CodeData? {
        // this is a very rudimentary way of validation
        val gson = Gson()
        return try {
            gson.fromJson(text, CodeData::class.java)
        } catch (ex: JsonParseException){
            null
        }
    }
}
