package com.hosung.note.noteandapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Hosung, Lee on 2016. 12. 10..
 */

public class NoteListDB {
    public static final String DB_NAME = "notelist.db";
    public static final int    DB_VERSION = 1;

    public static final String CREATE_NOTEINFO_TABLE
            = "CREATE TABLE NoteInfo (noteno INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "note TEXT, photofile TEXT, " +
            "latitude DOUBLE NOT NULL DEFAULT(0), longitude DOUBLE NOT NULL DEFAULT (0), " +
            "address TEXT) ";

    public static final String DELETE_NOTEINFO_TABLE
            = "DROP TABLE IF EXISTS NoteInfo ";

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name,
                        SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create tables
            db.execSQL(CREATE_NOTEINFO_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("Task list", "Upgrading db from version " + oldVersion + " to " + newVersion);
            Log.d("Task list", "Deleting all data!");
            db.execSQL(DELETE_NOTEINFO_TABLE);
            onCreate(db);
        }
    }

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public NoteListDB(Context context) {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    private void closeDB() {
        if (db != null) db.close();
    }

    public ArrayList<NoteInfo> getList() {
        ArrayList<NoteInfo> list = new ArrayList<NoteInfo>();
        openReadableDB();
        Cursor cursor = db.query("NoteInfo", null, null, null, null, null, "noteno DESC");
        while (cursor.moveToNext()) {
            NoteInfo noteinfo = new NoteInfo();
            noteinfo.setNoteno(cursor.getLong(0));
            noteinfo.setNote(cursor.getString(1));
            noteinfo.setPhotofile(cursor.getString(2));
            noteinfo.setLatitude(cursor.getDouble(3));
            noteinfo.setLongitude(cursor.getDouble(4));
            noteinfo.setAddress(cursor.getString(5));
            list.add(noteinfo);
        }
        cursor.close();
        closeDB();
        return list;
    }

    public NoteInfo getNote(int noteno) {
        String where = "noteno = ?";
        String[] whereArgs = { String.valueOf(noteno) };

        openReadableDB();
        Cursor cursor = db.query("NoteInfo", null,
                where, whereArgs, null, null, null);
        cursor.moveToFirst();
        NoteInfo noteinfo = new NoteInfo();
        noteinfo.setNoteno(cursor.getLong(0));
        noteinfo.setNote(cursor.getString(1));
        noteinfo.setPhotofile(cursor.getString(2));
        noteinfo.setLatitude(cursor.getDouble(3));
        noteinfo.setLongitude(cursor.getDouble(4));
        noteinfo.setAddress(cursor.getString(5));
        cursor.close();
        this.closeDB();

        return noteinfo;
    }

    public long insertNoteInfo(NoteInfo noteinfo) {
        ContentValues cv = new ContentValues();
        cv.put("note", noteinfo.getNote());
        cv.put("photofile", noteinfo.getPhotofile());
        cv.put("latitude", noteinfo.getLatitude());
        cv.put("longitude", noteinfo.getLongitude());
        cv.put("address", noteinfo.getAddress());

        this.openWriteableDB();
        long rowID = db.insert("NoteInfo", null, cv);
        this.closeDB();

        return rowID;
    }

    public int updateNoteInfo(NoteInfo noteinfo) {
        ContentValues cv = new ContentValues();
        cv.put("note", noteinfo.getNote());
        cv.put("photofile", noteinfo.getPhotofile());
        cv.put("latitude", noteinfo.getLatitude());
        cv.put("longitude", noteinfo.getLongitude());
        cv.put("address", noteinfo.getAddress());

        String where = "noteno = ?";
        String[] whereArgs = { String.valueOf(noteinfo.getNoteno()) };

        this.openWriteableDB();
        int rowCount = db.update("NoteInfo", cv, where, whereArgs);
        this.closeDB();

        return rowCount;
    }

    public int deleteNoteInfo(NoteInfo noteinfo) {
        String where = "noteno = ?";
        String[] whereArgs = { String.valueOf(noteinfo.getNoteno()) };

        this.openWriteableDB();
        int rowCount = db.delete("NoteInfo", where, whereArgs);
        this.closeDB();

        return rowCount;
    }
}
