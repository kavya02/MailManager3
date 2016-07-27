package myhomework.com.meilmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import myhomework.com.meilmanager.Utils.GenericListener;
import myhomework.com.meilmanager.adapter.EmailListViewAdapter;
import myhomework.com.meilmanager.database.ContactDatabaseHandler;
import myhomework.com.meilmanager.database.MessageDatabaseHandler;
import myhomework.com.meilmanager.model.EmailData;
import myhomework.com.meilmanager.model.UserInfomation;

public class MainActivity extends Activity {
    Context mContext;
    private ImageView mImgSetting, mImgUser, mImgEdit;
    private ListView mListEmail;
    private List<EmailData> newEmails;

    private EmailListViewAdapter adapter;
    public static List<EmailData> mainData;
    private MessageDatabaseHandler messageDB;

    private int nConter = 0;
    ServerAPI serverAPI;

    RunRollTask roll;
    Timer timer;

    HashMap<String,ServerAPI.UserInfo> myUserMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        messageDB = new MessageDatabaseHandler(mContext);

        SharedPreferences settings = getSharedPreferences(AppConstants.MY_PREFS_NAME, 0);
        boolean firstStart = settings.getBoolean("firstStart", true);

        generate3Message();

        mainData = (ArrayList) messageDB.getAllEmailInfo();
        adapter = new EmailListViewAdapter(this, (ArrayList) mainData);

        initUI();
        Toast.makeText(this, "Just created this activity", Toast.LENGTH_SHORT);
        //getKeyPair();

        if(timer != null){
            timer.cancel();
        }
        timer = new Timer();
        roll = new RunRollTask();
        timer.schedule(roll, 5000, 5000);

    }

    public void getKeyPair() {
        SharedPreferences prefs = getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE);
        String nPrivateKey = prefs.getString("RSAPrivateKey", "");
        String nPublicKey = prefs.getString("RSAPublicKey", "");

        Toast.makeText(MainActivity.this, "PrivateKey="+nPrivateKey, Toast.LENGTH_LONG).show();
        Toast.makeText(MainActivity.this, "PublicKey="+nPublicKey, Toast.LENGTH_LONG).show();
    }

    public void initUI() {
        mImgSetting = (ImageView)findViewById(R.id.imgSetting);
        mImgSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iner = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(iner);
                //finish();
            }
        });

        mImgUser = (ImageView) findViewById(R.id.imgUser);
        mImgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iner = new Intent(MainActivity.this, ContactsActivity.class);
                startActivity(iner);
                //finish();
            }
        });

        mImgEdit = (ImageView) findViewById(R.id.imgEdit);
        mImgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iner = new Intent(MainActivity.this, ComposeActivity.class);
                startActivity(iner);
                //finish();
            }
        });

        mListEmail = (ListView) findViewById(R.id.listEmail);
        mListEmail.setAdapter(adapter);

        refresh();

    }

    private void refresh() {
        mainData = messageDB.getAllEmailInfo();
        adapter.swapData((ArrayList) mainData);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        timer.cancel();
        finish();
    }
    private void generate3Message() {
        messageDB.AddEmailInfo(new EmailData(
                "id",
                "Smith",
                "Love",
                "Hi Dear, I love you. from me",
                "5"
        ));
        messageDB.AddEmailInfo(new EmailData(
                "id",
                "John",
                "Study",
                "Hi Dear, I study English. from me",
                "15"
        ));
        messageDB.AddEmailInfo(new EmailData(
                "id",
                "Jane",
                "Song",
                "Hi Dear, I sing a song. from me",
                "900"
        ));
    }

    private void updateEmailList(ArrayList<EmailData> emails) {
        newEmails = emails;
        nConter += 5;

        new Thread(new Runnable() {
            @Override
            public void run() {

                for(EmailData emailData : newEmails) {
                    emailData.setTime("" + System.currentTimeMillis());
                    messageDB.AddEmailInfo(emailData);
                    mainData.add(emailData);
                }

                for (int i = 0; i < mainData.size(); i++) {
                    //System.out.println(mainData.get(i).getSubject() + " : ttl: " + mainData.get(i).getTime());

                    if (System.currentTimeMillis() - Long.parseLong(mainData.get(i).getTime()) >= 60000l) {
                        messageDB.DeleteEmailInfo(mainData.get(i).getEmailID());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refresh();
                            }
                        });
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    class RunRollTask extends TimerTask {
        @Override
        public void run() {
            SharedPreferences settings = getSharedPreferences(AppConstants.MY_PREFS_NAME, 0);
            String username = settings.getString("username", "FailedToRetrieveUsername");

            serverAPI.getInstance().startPushListener(username, new GenericListener<ArrayList<EmailData>>() {
                @Override
                public void onResponse(ArrayList<EmailData> emails) {
                    for(EmailData email : emails)
                        System.out.println("\n RECEIVED EMAIL: " + email);
                    updateEmailList(emails);
                }

                @Override
                public void onError(String errorMessage) {

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mainData = messageDB.getAllEmailInfo();
        adapter.swapData((ArrayList) mainData);
    }

}
