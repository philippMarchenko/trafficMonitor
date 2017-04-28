package com.devphill.traficMonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

 public class DBHelper extends SQLiteOpenHelper {

	  public static final String LOG_TAG_DB = "dbLogs";
      public String db_idsim;
      int mydbVersion;

     public DBHelper(Context context) {
         // конструктор суперкласса
         super(context, "trafficDB", null, 1);
     }

     @Override
     public void onCreate(SQLiteDatabase db) {

         Log.d(LOG_TAG_DB, "--- onCreate database ---");
         // создаем таблицу с полями

     }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }




  }