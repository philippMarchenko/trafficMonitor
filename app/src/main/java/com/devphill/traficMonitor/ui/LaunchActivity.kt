package com.devphill.traficMonitor.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.devphill.traficMonitor.R
import com.devphill.traficMonitor.premmision.Permission
import org.jetbrains.anko.startActivity
import timber.log.Timber

/**
 * Created by Philipp Marchenko on 09.08.2020.
 * Copyright (c) 2020 mova.io. All rights reserved.
 */
class LaunchActivity : AppCompatActivity(){
    var permission: Permission? = null

    var dialog1: AlertDialog? = null
    var dialog2: AlertDialog? = null

    var builder1: AlertDialog.Builder? = null
    var builder2: AlertDialog.Builder? = null

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        Timber.d("onCreate")

        toolbar = findViewById<View>(R.id.toolbar) as Toolbar

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        permission = Permission(baseContext, this)


    }

    override fun onResume() {
        super.onResume()

        if (permission?.hasPermissionToReadPhoneStats() == false) {
            if (dialog1 == null) {
                showQueryPermissionPhoneStats()
            }
        }
        else if(permission?.hasPermissionToReadNetworkHistory() == false){
            if (dialog2 == null) {
                showQueryPermissionNetworkHistoryAccess()
            }
        }
        else{
            startActivity<MainActivity>()
            finish()
        }
    }

    private fun showQueryPermissionPhoneStats() {
        builder1 = AlertDialog.Builder(this, R.style.MyDialogTheme)
        builder1?.setTitle(getString(R.string.need_permissions))
        builder1?.setMessage(getString(R.string.need_permissions_text))
        builder1?.setPositiveButton(getString(R.string.make_request)
        ) { dialog, which ->
            dialog.cancel()
            permission?.requestPermissionPhoneStateStats()
        }
        builder1?.setNegativeButton(getString(R.string.cancel)
        ) { dialog, which ->
            dialog.cancel()
            finish()
        }
        dialog1 = builder1?.create()
        dialog1?.show()
    }

    private fun showQueryPermissionNetworkHistoryAccess() {
        builder2 = AlertDialog.Builder(this, R.style.MyDialogTheme)
        builder2?.setTitle(getString(R.string.need_permissions))
        builder2?.setMessage(getString(R.string.need_permissions2_text))
        builder2?.setPositiveButton(getString(R.string.make_request2)
        ) { dialog, which ->
            dialog.cancel()
            permission?.requestReadNetworkHistoryAccess()
        }
        builder2?.setNegativeButton(getString(R.string.cancel)
        ) { dialog, which ->
            dialog.cancel()
            finish()
        }
        dialog2 = builder2?.create()
        dialog2?.show()
    }


/*    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Permission.READ_PHONE_STATE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity<MainActivity>()
            }
        }
    }*/

}