package com.devphill.traficMonitor.ui

import android.content.Context
import android.content.SharedPreferences

class DataManager(context: Context?) {

    companion object {
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    }

    var loginSharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private var mContext: Context? = context

    init {
        loginSharedPreferences = mContext?.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE)
        editor = loginSharedPreferences?.edit()
    }


    val alertLevel: Int?
        get() {
            return loginSharedPreferences?.getInt(Constants.APP_PREFERENCES_ALLERT_LEVEL, 0)
        }
    val stopLevel: Int?
        get() {
            return loginSharedPreferences?.getInt(Constants.APP_PREFERENCES_STOP_LEVEL, 0)
        }

    val isDisableConectionWhenLimit: Boolean?
        get() {
            return loginSharedPreferences?.getBoolean(Constants.APP_PREFERENCES_DISABLE_INTERNET, false)
        }
    val isShowAlert: Boolean?
        get() {
            return loginSharedPreferences?.getBoolean(Constants.APP_PREFERENCES_SHOW_ALLERT, false)
        }

    fun setAlertLevel(int: Int) {
        editor?.putInt(Constants.APP_PREFERENCES_ALLERT_LEVEL, int)
        editor?.commit()
    }

    fun setStopLevel(int: Int) {
        editor?.putInt(Constants.APP_PREFERENCES_STOP_LEVEL, int)
        editor?.commit()
    }
    fun setDisableConectionWhenLimit(bool: Boolean) {
        editor?.putBoolean(Constants.APP_PREFERENCES_DISABLE_INTERNET, bool)
        editor?.commit()
    }

    fun setIsShowAlert(bool: Boolean) {
        editor?.putBoolean(Constants.APP_PREFERENCES_SHOW_ALLERT, bool)
        editor?.commit()
    }
}