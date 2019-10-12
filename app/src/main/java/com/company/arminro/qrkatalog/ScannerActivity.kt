package com.company.arminro.qrkatalog

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.zxing.Result

import kotlinx.android.synthetic.main.activity_scanner.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler  {
    override fun handleResult(rawResult: Result?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var cameraId: Int = 0
    private var mScannerView: ZXingScannerView? = null
    private var imView: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        //setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        title = "QR Scanner"
        cameraId = getCameraId()

        mScannerView = ZXingScannerView(this)
        val preview = findViewById<FrameLayout>(R.id.camera_preview)
        imView = findViewById<ImageView>(R.id.contentImage)

        preview.addView(mScannerView)
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mScannerView?.startCamera(cameraId)
            mScannerView?.setResultHandler(this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )
        }
    }


    // getting the first camera facing backwards
    fun getCameraId(): Int {
        val c: Camera? = null
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

}
