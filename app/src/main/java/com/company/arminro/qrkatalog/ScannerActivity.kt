package com.company.arminro.qrkatalog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
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


    // getting the first camera facing backwards
    fun getCameraId(): Int {
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


    override fun handleResult(rawResult: Result) {
        if (rawResult.text != null && rawResult.text.isNotEmpty() && fab.isPressed) {
            // parsing the text only if it is valid
            val text = rawResult.text
            var resultData: CodeData? = validateResultString(text)
            resultData?.timestampCreated = getCurrentDateTimeString()

            if (resultData != null) {

                mScannerView?.stopCameraPreview()
                // asking the user if s/he wants to save the valid data
                val mDialogView = LayoutInflater.from(this).inflate(R.layout.data_details, null)
                val mBuilder = AlertDialog.Builder(this@ScannerActivity)
                    .setView(mDialogView)
                    .setTitle("Save the following?")
                    .setPositiveButton("Save"){_, _ ->

                        // stopping the camera and sending data to the other activity
                        mScannerView?.stopCamera()
                        val intent = Intent(this@ScannerActivity, MainActivity::class.java)
                            .putExtra(getString(R.string.qr_data_intent_extra), resultData)
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel"){dialog,which ->
                        dialog.cancel()
                    }
                mDialogView.companyField_details.text = resultData.companyName
                mDialogView.description_details.text = resultData.description
                mDialogView.fromField_details.text = resultData.source
                mDialogView.toField_details.text = resultData.destination
                mDialogView.timestamp_details.text = resultData.timestampCreated

                val  mAlertDialog = mBuilder.show()
            }

            mScannerView?.resumeCameraPreview(this)
        }
        else if(fab.isPressed){
            Toast.makeText(this, "The loaded QR Code is not valid", Toast.LENGTH_SHORT)
        }
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
}
