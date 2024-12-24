package com.kazumaproject.markdownhelperkeyboard.setting_activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class TangoDB {
    private static final String LOG_TAG = "tangoLogger";

    private static final String DATABASE_NAME = "tangolog.db";
    private static final String TANGO_TABLE_NAME = "photolog";
    private static final String COL_ID = "id";
    private static final String COL_TANGO_URI = "tango_uri";
    private static final String TANGO_TABLE_CREATE =
            " CREATE TABLE " + TANGO_TABLE_NAME + "(" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_TANGO_URI + " TEXT);";
    private static Context context;

    SQLiteDatabase db;

     void LogDBAdapter( Context con ){
        context = con;

        try {
            db = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);

            //テーブルの存在確認
            final String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + TANGO_TABLE_NAME + "';";
            Cursor c = db.rawQuery(query, null);
            int cnt = c.getCount();
            c.close();

            if( cnt == 0 ){
                // テーブルがない時→新規作成
                db.execSQL(TANGO_TABLE_CREATE);
            }
        }catch(SQLiteException err){
            Log.e(LOG_TAG, err.getMessage());
        }
    }

    void saveTangoURI(Uri uri){
         String strURI = uri.toString();
         String sql = "insert into" + TANGO_TABLE_NAME + "(" + COL_TANGO_URI + ") values'" + strURI + "');";
         Cursor c = null;
         try{
             db.execSQL(sql);
         }catch(SQLiteException err){
             Log.e(LOG_TAG, err.getMessage());
         }finally{
             if(c != null){
                 c.close();
             }
         }
    }

    void checkRecord(){
         final String query = " SELECT " + COL_ID + "," + COL_TANGO_URI + "FROM" + TANGO_TABLE_NAME + ";";
         Cursor c = null;
         try{
             c = db.rawQuery(query, null);
             c.moveToFirst();
             while(!c.isAfterLast()){
                 String id = c.getString(0);
                 String uri = c.getString(1);
                 Log.i(LOG_TAG, "id:" + id + ",uri:" + uri);
                 c.moveToNext();
             }
         }catch(SQLiteException err){
             Log.e(LOG_TAG, err.getMessage());
         }finally{
             if(c != null){
                 c.close();
             }
         }
    }

    Cursor getURIcursor(){
        final String query = "SELECT " + COL_ID + " as _id," + COL_TANGO_URI + " FROM " + TANGO_TABLE_NAME + ";";
        Cursor c = null;

        try{
            c = db.rawQuery(query, null);
        }catch(SQLiteException err){
            Log.e(LOG_TAG, err.getMessage());
        }
        return c;
    }
}
