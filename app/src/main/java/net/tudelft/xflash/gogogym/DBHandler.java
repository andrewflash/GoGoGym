package net.tudelft.xflash.gogogym;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "gogogym";

    // Contacts table name
    private static final String TABLE_UDATA = "user_data";
    private static final String TABLE_GYM = "gym";
    private static final String TABLE_ULOG = "user_log";

    public SQLiteDatabase myDB;

    public boolean status;

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initiateDB(db);
        //this.myDB = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //removeTables(db);
        onCreate(db);
    }

    public void removeTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        removeTables(db);
        //removeTables(myDB);
    }
    public void removeTables(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UDATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ULOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GYM);
    }

    public void initiateDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        initiateDB(db);

        //initiateDB(myDB);
    }

    public void initiateDB(SQLiteDatabase db){

        removeTables(db);
        String CREATE_USERDATA_TABLE = "CREATE TABLE "+TABLE_UDATA+" ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_name TEXT,"
                + "email TEXT,"
                + "password TEXT,"
                + "user_age INTEGER,"
                + "pet_name TEXT,"
                + "pet_energy INTEGER,"
                + "pet_exp INTEGER)";
        db.execSQL(CREATE_USERDATA_TABLE);

        String CREATE_GYM_TABLE = "CREATE TABLE "+TABLE_GYM+" ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "gym_name TEXT,"
                + "longitude REAL,"
                + "latitude REAL)";
        db.execSQL(CREATE_GYM_TABLE);

        String CREATE_USERLOG_TABLE = "CREATE TABLE "+TABLE_ULOG+" ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INT,"
                + "gym_id INT,"
                + "start_time TEXT,"
                + "finish_time TEXT)";
        db.execSQL(CREATE_USERLOG_TABLE);

        //initiateData();
    }

    public void initiateData(){
        UData ud = new UData(1,"Bontor", "bontor@gmail.com", "12345", 17, "Bambang", 9, 11);
        this.addUData(ud);


    }

    public boolean addUData(UData ud) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_name", ud.user_name);
        values.put("email", ud.email);
        values.put("password", ud.password);
        values.put("user_age", ud.user_age);
		values.put("pet_name", ud.pet_name);
		values.put("pet_energy", ud.pet_energy);
		values.put("pet_exp", ud.pet_exp);

        // Inserting Row
        boolean status = db.insert(TABLE_UDATA, null, values) > 0;
        db.close(); // Closing database connection
        return status;
    }

    // Getting one shop
    public UData getUData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT  * FROM " + TABLE_UDATA + " WHERE id="+id;

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null)
            cursor.moveToFirst();

        UData ud = new UData(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), Integer.parseInt(cursor.getString(4)), cursor.getString(5), Integer.parseInt(cursor.getString(6)), Integer.parseInt(cursor.getString(7)));
        return ud;
    }

    // Getting shops Count
    public int getUserCount() {
        String countQuery = "SELECT  * FROM " + TABLE_UDATA;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public String readUData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT  * FROM " + TABLE_UDATA + " WHERE id="+id;

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor.getString(0);
    }

    public boolean addVisit(int user_id, int gym_id, String time){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_id", user_id);
        values.put("gym_id", gym_id);
        values.put("start_time", time);

        // Inserting Row
        boolean status = db.insert(TABLE_ULOG, null, values) > 0;
        db.close(); // Closing database connection
        return status;
    }

    public void finishVisit(int user_id, int gym_id, String start_time, String finish_time) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE "+TABLE_ULOG+" SET finish_time = "+finish_time+" WHERE user_id="+user_id
                +" AND gym_id = "+gym_id+" AND start_time="+start_time;

        db.rawQuery(sql, null);
        db.close();
    }

    public boolean addGym(String gym_name, double longitude, double latitude){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("gym_name", gym_name);
        values.put("longitude", longitude);
        values.put("latitude", latitude);

        // Inserting Row
        boolean status = db.insert(TABLE_GYM, null, values) > 0;
        db.close(); // Closing database connection
        return status;
    }

    public List<Gym> getAllGyms() {
        List<Gym> gymList = new ArrayList<Gym>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GYM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Gym gg = new Gym( (int) Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        (double) Double.parseDouble(cursor.getString(2)),
                        (double) Double.parseDouble(cursor.getString(3))
                    );

                // Adding contact to list
                gymList.add(gg);
            } while (cursor.moveToNext());
        }

        // return contact list
        return gymList;
    }

    public void updatePoint(int user_id, int exp, int energy){
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE "+TABLE_UDATA+" SET pet_exp = "+exp+ ", pet_energy = "+ energy +" WHERE id="+user_id;

        db.rawQuery(sql, null);
        db.close();
    }

}