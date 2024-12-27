package com.kazumaproject.markdownhelperkeyboard.setting_activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Console;

public class MyOpenHelper2 extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "TangoDictionary.db";
    private static final String TABLE_NAME = "tango";
    private static final String _ID = "_id";
    private static final String COLUMN_NAME_HIRAGANA = "hiragana";
    private static final String COLUMN_NAME_KATAKANA = "katakana";
    private static final String COLUMN_NAME_KANJI = "kanji";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_HIRAGANA + " TEXT," +
                    COLUMN_NAME_KATAKANA + " TEXT," +
                    COLUMN_NAME_KANJI + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    MyOpenHelper2(Context context) { super(context, DATABASE_NAME,
            null, DATABASE_VERSION);
        System.out.println("オープン");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        System.out.println("ID:");

        db.execSQL(
                SQL_CREATE_ENTRIES
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion , int newVersion) {
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion , int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
