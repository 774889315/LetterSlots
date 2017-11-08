package com.example.administrator.wordslot;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//import com.example.daily.myapplication.EntityClass.Task;

/**
 * 使用说明：
 * 用 DateBaseHelper helper = DateBaseHelper.getInstance(Context yourContext);
 * 得到一个实例。
 * 用 SQLiteDatabase db = helper.getWritableDatabase();得到数据库对象。
 * 以上是初始化操作，接下来：
 * addUser
 * delUser
 * updateUser
 * removeAllColumns
 * getUserInformation
 * 最后一个会返回一个User类，可以用get方法获取level（int），coin（long），exp（long）
 */
public class DateBaseHelper extends SQLiteOpenHelper {
    private static final String NAME = "Users.db";
    private static int VERSION = 1;
    private static DateBaseHelper dbHelper = null;
    private final String TAG = "@vir DateBaseHelper";

    public static final String CREATE_BOOK = "create table Users(" +
            "id integer primary key," +
            "coin Long," +
            "level integer," +
            "exp Long)";

    public DateBaseHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    public static DateBaseHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DateBaseHelper(context);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addUser(int id, long coin, int level, long exp, SQLiteDatabase db) {
        db.execSQL("insert into Users (id, coin, level, exp) values(?, ?, ?, ?)"
                , new String[]{Integer.toString(id),Long.toString(coin),Integer.toString
                        (level),Long.toString(exp)});
    }

    public void delUser(int id, SQLiteDatabase db) {
        db.execSQL("delete from Users where id = ?", new String[]{Integer.toString(id)});
        db.execSQL("update Users set id=id-1 where id > ?", new String[]{Integer.toString
                (id)});
    }

    public void updateUser(int id, long coin, int level, long exp, SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL("update Users set coin = ? where id = ?", new String[]{Long.toString(coin),
                Integer.toString(id)});
        db.execSQL("update Users set level = ? where id = ?", new String[]{Integer.toString(level),
                Integer.toString(id)});
        db.execSQL("update Users set exp = ? where id = ?", new String[]{Long.toString(exp),
                Integer.toString(id)});
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void removeAllColumns(SQLiteDatabase db) {
        db.delete("Users", null, null);
    }

    public User getUserInformation(int id, SQLiteDatabase db) {
        Cursor cursor = db.query("Users",null,null,null,null,null,null);
        if (cursor.moveToFirst()) {
            do {
                int thisId = cursor.getInt(cursor.getColumnIndex("id"));
                if(thisId == id) {
                    long coin = cursor.getLong(cursor.getColumnIndex("coin"));
                    int level = cursor.getInt(cursor.getColumnIndex("level"));
                    long exp = cursor.getLong(cursor.getColumnIndex("exp"));
                    return new User(null, coin,level,exp);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return null;
    }

}
