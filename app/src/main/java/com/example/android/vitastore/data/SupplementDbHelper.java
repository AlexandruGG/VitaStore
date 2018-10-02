package com.example.android.vitastore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.vitastore.data.SupplementContract.SupplementEntry;


public class SupplementDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "vitashop.db";
    private static final int DATABASE_VERSION = 1;

    SupplementDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_SUPPLEMENTS_TABLE = "CREATE TABLE " + SupplementEntry.TABLE_NAME + " ("
                + SupplementEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SupplementEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + SupplementEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, "
                + SupplementEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 1, "
                + SupplementEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + SupplementEntry.COLUMN_SUPPLIER_TELEPHONE + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_SUPPLEMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
