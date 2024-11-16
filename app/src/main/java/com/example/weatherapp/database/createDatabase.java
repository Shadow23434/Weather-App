package com.example.weatherapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CreateDatabase extends SQLiteOpenHelper {
    private static String dbname = "Login.db";
    private static String dbTable = "Users";
    private static String version = "1";

    private static String Column_id = "id";
    private static String Column_fullname = "fullname";
    private static String Column_email = "email";
    private static String Column_phonenumber = "phoneNumber";
    private static String Column_password = "password";

    public CreateDatabase(Context context) {
        super(context, dbname, null, Integer.parseInt(version));
    }

    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        String query = "create table " + dbTable + "(" + Column_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Column_fullname + " TEXT, "
                + Column_email + " TEXT, " + Column_phonenumber + " TEXT, " + Column_password + " TEXT)";
        MyDB.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int i, int i1) {
        MyDB.execSQL("DROP TABLE IF EXISTS " + dbTable);
        onCreate(MyDB);

    }
    public boolean addUser(String fullname, String email, String phonenumber, String password){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Column_fullname, fullname);
        values.put(Column_email, email);
        values.put(Column_phonenumber, phonenumber);
        values.put(Column_password, password);
        String Users;
        long result = MyDB.insert("Users", null, values);
        if(result == -1) return false;
        else
            return  true;
    }

    public boolean checkEmail(String email){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("SELECT * FROM Users WHERE email = ?", new String[] {email});
        if(cursor.getCount() > 0){
            return false;
        }
        else
            return true;
    }

    public boolean checkUser(String email, String password){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("SELECT * FROM Users WHERE email = ? and password = ?", new String[] {email, password});
        if(cursor.getCount() > 0)
            return  true;
        else
            return false;
    }
}
