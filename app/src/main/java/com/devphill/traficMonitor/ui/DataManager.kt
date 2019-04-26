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

    val isAppLaunchFirstTime: Boolean?
        get() {
            return loginSharedPreferences?.getBoolean(Constants.APP_LAUNCH_FIRST_TIME, true)
        }

    val alertLevel: Int?
        get() {
            return loginSharedPreferences?.getInt(Constants.APP_PREFERENCES_ALLERT_LEVEL, 0)
        }
    val stopLevel: Int?
        get() {
            return loginSharedPreferences?.getInt(Constants.APP_PREFERENCES_STOP_LEVEL, 0)
        }
    val period: Int?
        get() {
            return loginSharedPreferences?.getInt(Constants.APP_PREFERENCES_PERIOD, 0)
        }
    val isDisableConectionWhenLimit: Boolean?
        get() {
            return loginSharedPreferences?.getBoolean(Constants.APP_PREFERENCES_DISABLE_INTERNET, false)
        }
    val isShowAlert: Boolean?
        get() {
            return loginSharedPreferences?.getBoolean(Constants.APP_PREFERENCES_SHOW_ALLERT, false)
        }


    val date: String?
        get() {
            return loginSharedPreferences?.getString(Constants.APP_DATE, "")
        }

    fun setDate(str: String) {
        editor?.putString(Constants.APP_DATE, str)
        editor?.commit()
    }

    fun setPeriod(int: Int) {
        editor?.putInt(Constants.APP_PREFERENCES_PERIOD, int)
        editor?.commit()
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

    fun setIsAppLaunchFirstTime(bool: Boolean) {
        editor?.putBoolean(Constants.APP_LAUNCH_FIRST_TIME, bool)
        editor?.commit()
    }
}