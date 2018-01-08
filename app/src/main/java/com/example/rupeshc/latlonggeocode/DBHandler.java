package com.example.rupeshc.latlonggeocode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

public class DBHandler extends SQLiteOpenHelper {

    private static DBHandler dbInstance = null;

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "CUS_LATLONG";
    private Context context;
    private static SQLiteDatabase db;
    public static boolean isDbOpened = false;
    private File originalFile = null;

    public String Create_User_Tbl = "CREATE TABLE " + DBBuilder.userTableName +
            " (" + DBBuilder.UserTable.GEOCODE_ID + " VARCHAR(5000), "
            + DBBuilder.UserTable.CUST_ADDRESS + " VARCHAR(5000), "
            + DBBuilder.UserTable.LATTITUDE+ " VARCHAR(5000), "
            + DBBuilder.UserTable.LONGITUDE + " VARCHAR(5000), "
            + DBBuilder.UserTable.PINCODE + " VARCHAR(5000),"
            + DBBuilder.UserTable.CITY + " VARCHAR(5000), "
            + DBBuilder.UserTable.STATE + " VARCHAR(5000), "
            + DBBuilder.UserTable.LASTUPDATED_DTS + " VARCHAR(5000),"
            + DBBuilder.UserTable.CUST_ID + " VARCHAR(5000))";

    public String Delete_User_Tbl = "delete from " + DBBuilder.userTableName;




    public interface DBBuilder {
        String userTableName = "UserTable";

        interface UserTable {
            String GEOCODE_ID = "GEOCODE_ID";
            String CUST_ADDRESS = "CUST_ADDRESS";
            String LATTITUDE = "LATTITUDE";
            String LONGITUDE = "LONGITUDE";
            String PINCODE = "PINCODE";
            String CITY = "CITY";
            String STATE = "STATE";
            String LASTUPDATED_DTS = "LASTUPDATED_DTS";
            String CUST_ID = "CUST_ID";
        }

    }

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        originalFile = context.getDatabasePath(DATABASE_NAME);
    }

    public static synchronized DBHandler getInstance(Context ctx) {
        if (dbInstance == null) {
            dbInstance = new DBHandler(ctx.getApplicationContext());
        }
        return dbInstance;
    }

    // Open the database connection.
    public void open() {
        try {
            db = this.getWritableDatabase();
            isDbOpened = true;

        } catch (Exception e1) {
            try {
                //upgrade from plain to encrypted database
                isDbOpened = true;

//                e1.printStackTrace();
            } catch (Exception e2) {
//                e2.printStackTrace();
            }
        }
        //Log.i("DB", "open");
    }

    public void close() {

        try {
            if (db != null && db.isOpen()) {
                db.close();
                isDbOpened = false;
            }
        } catch (Exception e) {
        }
        //Log.i("DB", "close");
    }

    public boolean isDbOpen() {
        if (db != null)
            return db.isOpen();
        else return false;
    }

    public void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(Create_User_Tbl);
        }
        catch (Exception e){
            Log.e("","Error="+e.toString());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void FlushDatabase() {
        db.execSQL(Delete_User_Tbl);
    }


    public void insertUserMaster(String GEOCODE_ID, String CUST_ID, String CUST_ADDRESS, String LATTITUDE, String LONGITUDE,
                                 String PINCODE,String CITY,String STATE,String LASTUPDATED_DTS) {
        try {

            insert(new String[]{GEOCODE_ID, CUST_ID, CUST_ADDRESS, LATTITUDE, LONGITUDE, PINCODE,CITY,STATE,LASTUPDATED_DTS},
                    new String[]{DBBuilder.UserTable.GEOCODE_ID, DBBuilder.UserTable.CUST_ID,
                            DBBuilder.UserTable.CUST_ADDRESS, DBBuilder.UserTable.LATTITUDE,
                            DBBuilder.UserTable.LONGITUDE,DBBuilder.UserTable.PINCODE,
                            DBBuilder.UserTable.CITY,DBBuilder.UserTable.STATE,DBBuilder.UserTable.LASTUPDATED_DTS},
                    DBBuilder.userTableName);

            Cursor cursor = db.rawQuery("select * from "+DBBuilder.userTableName,null);
            Log.e("","Count="+cursor.getCount());
            //deleteUserMaster();
        } catch (Exception e) {
            Log.e("","Error="+e.toString());
        }
    }

    public Cursor GetAllLatLong() {
        Cursor cur = fetch(DBBuilder.userTableName, null, null,null, null, null, true, null, null);
        return cur;
    }

    public long insert(String values[], String names[], String tbl) {
        ContentValues initialValues = createContentValues(values, names);
        long longInsertId = 0;
        if (isDbOpened) {
            try {
                if (!isDbOpen())
                    open();
                longInsertId = db.insertOrThrow(tbl, null, initialValues);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("","Error="+e.toString());
            }
        }
        return longInsertId;
    }

    @SuppressWarnings("DatabaseObjectNotClosedException")
    public Cursor fetch(String tbl, String names[], String where, String args[],
                        String order, String limit, boolean isDistinct, String groupBy, String having) {
        Cursor cur = null;
        if (isDbOpened) {
            try {
                if (!isDbOpen())
                    open();
                cur = db.query(isDistinct, tbl, names, where, args, groupBy, having, order, limit);
            }
            catch (Exception e) {
                Log.e("","Error="+e.toString());
            }
        }
        return cur;
    }

    //for param query
    private ContentValues createContentValues(String values[], String names[]) {
        ContentValues values1 = new ContentValues();
        for (int i = 0; i < values.length; i++) {
            values1.put(names[i], values[i]);
        }
        return values1;
    }

}
