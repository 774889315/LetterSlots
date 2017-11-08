package com.example.administrator.wordslot;

import android.app.Activity;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    TextView lv, exp, coin;
    TextView l1, l2, l3;
    TextView hint;

    EditText input;

    LinearLayout question, ask;

    Button spin, confirm;

    Boolean isSpinning = false;
    Thread spinner;

    User user;
    WordsList list;
    ArrayList<Word> word;
    Word word0;

    DateBaseHelper helper;
    SQLiteDatabase db;

    static int ref = 36;

    static final int REFRESH = 1;
    static final int CHECKED = 2;
    static final int UNCHECKED = 3;
    static final int HIDE = 4;

    static final int ID = 0;

    public Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == REFRESH)
            {
                refresh();
            }

            else if(msg.what == CHECKED)
            {
                ask();
            }

            else if(msg.what == UNCHECKED)
            {
                spin.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "很遗憾，没有中奖", Toast.LENGTH_SHORT).show();
            }

            else if(msg.what == HIDE)
            {
                spin.setVisibility(View.INVISIBLE);
            }

            else if(msg.what > 0)
            {
                l1.setText((char)(msg.what & 0xFF)+"");
                l2.setText((char)(msg.what >> 8 & 0xFF)+"");
                l3.setText((char)(msg.what >> 16 & 0xFF)+"");
            }

        }
    };

    void refresh()
    {
        lv.setText(user.getLv()+"");
        exp.setText(user.getExp()+"/"+user.getMaxExp());
        coin.setText(user.getCoin()+"");
    }

    boolean check() throws Exception
    {
        word = list.getList(l1.getText().toString() + l2.getText().toString() + l3.getText().toString());
        return word.size() != 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        helper = DateBaseHelper.getInstance(this);
        db = helper.getWritableDatabase();

        user = helper.getUserInformation(ID, db);
        if(user == null)
        {
            helper.addUser(ID, 20, 0, 0, db);
            user = helper.getUserInformation(ID, db);
        }

        user.setActivity(MainActivity.this);

        lv = (TextView) findViewById(R.id.lv);
        exp = (TextView) findViewById(R.id.exp);
        coin = (TextView) findViewById(R.id.coin);

        l1 = (TextView) findViewById(R.id.letter1);
        l2 = (TextView) findViewById(R.id.letter2);
        l3 = (TextView) findViewById(R.id.letter3);

        hint = (TextView) findViewById(R.id.hint);

        input = (EditText) findViewById(R.id.input);

        ask = (LinearLayout) findViewById(R.id.ask);
        question = (LinearLayout) findViewById(R.id.question);
        ask.setVisibility(View.INVISIBLE);

        spin = (Button) findViewById(R.id.spin);

        spin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSpinning) stop();
                else spin();
            }
        });

        confirm = (Button) findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input.getText().toString().equalsIgnoreCase(word0.getEnglish()))
                {
                    int coinGet = user.getLv() * 5 + 1;
                    Toast.makeText(MainActivity.this, "恭喜答对！ +" + coinGet + "金币\n"
                            + word0.getEnglish() + "\n"
                            + word0.getChinese(), Toast.LENGTH_LONG).show();
                    user.setCoin(user.getCoin() + coinGet);
                    user.addExp(20);
                    if(!ask())
                    {
                        ask.setVisibility(View.INVISIBLE);
                        spin.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "很遗憾答错了\n"
                            + word0.getEnglish() + "\n"
                            + word0.getChinese(), Toast.LENGTH_LONG).show();
                    ask.setVisibility(View.INVISIBLE);
                    spin.setVisibility(View.VISIBLE);
                }

            }
        });

        InputStream is = getResources().openRawResource(R.raw.t);
        list = new WordsList();
        list.setFile(is);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        refresh();
    }

    void stop()
    {
        isSpinning = false;
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Message msg = new Message();
                    msg.what = HIDE;
                    handler.sendMessage(msg);

                    Thread.sleep(500);
                    msg = new Message();
                    if(!check())
                    {
                        msg.what = UNCHECKED;
                    }
                    else
                    {
                        msg.what = CHECKED;
                    }
                    handler.sendMessage(msg);
                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this, "出错了", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();

    }

    void spin()
    {
        isSpinning = true;
        spinner = new Thread()
        {
            @Override
            public void run()
            {
                while(isSpinning)
                {
                    char[] c = new char[3];
                    c[0] = getRandomLetter();
                    c[1] = getRandomLetter();
                    c[2] = getRandomLetter();
                    Message msg = new Message();
                    msg.what = c[0] << 16 | c[1] << 8 | c[2];
                    handler.sendMessage(msg);
                    try
                    {
                        sleep(10);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        spinner.start();
        user.setCoin(user.getCoin() - user.getLv());
    }

    boolean ask()
    {
        if(word.size() == 0) return false;
        int index = (int) (Math.random() * word.size());
        word0 = word.get(index);
        word.remove(index);

        hint.setText(word0.getChinese());
        input.setText("");
        ask.setVisibility(View.VISIBLE);

        return true;
    }

    char getRandomLetter()
    {
        char c = (char) (Math.random() * ref + 'A');
        if(c > 'Z')
        {
            ref -= 5;
            switch (c % 5) {
                case 0:
                    c = 'A';
                    break;
                case 1:
                    c = 'E';
                    break;
                case 2:
                    c = 'I';
                    break;
                case 3:
                    c = 'O';
                    break;
                case 4:
                    c = 'U';
                    break;
            }
        }
        else ref += 5;

        return c;
    }


}
