package com.example.administrator.wordslot;

import android.database.sqlite.SQLiteDatabase;
import android.os.Message;

/**
 * Created by Administrator on 2017/11/5.
 */

class User
{
    private long coin = 0;
    private int lv = 0;
    private long exp = 1;

    private MainActivity activity;

    DateBaseHelper helper;
    SQLiteDatabase db;

    User(MainActivity activity, long coin, int lv, long exp)
    {
        this.activity = activity;
        this.coin = coin;
        this.lv = lv;
        this.exp = exp;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
        helper = DateBaseHelper.getInstance(activity);
        db = helper.getWritableDatabase();
    }
/*
    void getInfo()
    {
        //RRR
    }
*/
    void setCoin(long coin)
    {
        this.coin = coin;
        refresh();
        update();
    }

    long getCoin()
    {
        return coin;
    }

    private void levelUp()
    {
        lv += 1;
        refresh();
        update();
    }

    int getLv()
    {
        return lv;
    }

    long getExp()
    {
        return exp;
    }

    void addExp(int exp0)
    {
        exp += exp0;
        if(exp >= getMaxExp())
        {
            exp -= getMaxExp();
            levelUp();
        }
        refresh();
        update();
    }

    int getMaxExp()
    {
        return 40 + lv;
    }

    private void refresh()
    {
        Message msg = new Message();
        msg.what = MainActivity.REFRESH;
        activity.handler.sendMessage(msg);
    }

    private void update()
    {
        helper.updateUser(0, coin, lv, exp, db);
    }
}
