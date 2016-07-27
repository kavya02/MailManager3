package myhomework.com.meilmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import myhomework.com.meilmanager.Utils.GenericListener;
import myhomework.com.meilmanager.adapter.ContactListViewAdapter;
import myhomework.com.meilmanager.database.ContactDatabaseHandler;
import myhomework.com.meilmanager.model.UserInfomation;

public class ContactsActivity extends Activity {
    private ImageView mImgPlus, mImgFilter;
    private ListView mListContacts;
    private ServerAPI serverAPI = ServerAPI.getInstance();

    private Timer timer;
    private RunRollTask roll;
    private ContactDatabaseHandler userDB;

    private String username;
    private ContactListViewAdapter adapter;

    public static List<UserInfomation> mainData;
    public ArrayList<String> contactNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        SharedPreferences settings = getSharedPreferences(AppConstants.MY_PREFS_NAME, 0);
        username = settings.getString("username", "UsernameNotSavedAfterLogin");

        userDB = new ContactDatabaseHandler(this);
        mainData = (ArrayList) userDB.getAllContactInfo();
        adapter = new ContactListViewAdapter(this, (ArrayList) mainData);

        contactNames = new ArrayList<>();
        for(UserInfomation userInfo : mainData) {
            contactNames.add(userInfo.getUserName());
        }

        initUI();

        updateContactsLoginState();

        if(timer != null){
            timer.cancel();
        }
        timer = new Timer();;
        roll = new RunRollTask();
        timer.schedule(roll, 3000, 3000);
    }

    public void updateContactsLoginState() {
        serverAPI.registerContacts(username, contactNames, new GenericListener<HashMap<String, String>>() {
            @Override
            public void onResponse(HashMap<String, String> friends_status) {
                processUpdateContacts(friends_status);
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }

    public void processUpdateContacts(HashMap<String, String> friends_status) {
        System.out.println("ContactsActivity: " + "process UPDATES");
        for(UserInfomation userInfo : mainData) {
            String status = friends_status.get(userInfo.getUserName());


            if(status.equals("logged-in")) {
                userInfo.isOnline = true;
            } else {
                userInfo.isOnline = false;
            }
        }
/*
        for(UserInfomation userInfo : mainData) {
            System.out.println(userInfo.getUserName() + " :: " + userInfo.isOnline);

        }*/

        adapter.notifyDataSetChanged();
    }

    public void initUI() {
        mImgPlus = (ImageView) findViewById(R.id.imgPlus);
        mImgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iner = new Intent(ContactsActivity.this, ContactActivity.class);
                startActivity(iner);
            }
        });
        mImgFilter = (ImageView) findViewById(R.id.imgFilter);

        mListContacts = (ListView) findViewById(R.id.listContact);
        mListContacts.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        mainData = (ArrayList) userDB.getAllContactInfo();
        adapter.swapItems((ArrayList) mainData);

        for(UserInfomation userInfo : mainData) {
            contactNames.add(userInfo.getUserName());
        }
    }

    class RunRollTask extends TimerTask {
        @Override
        public void run() {
            updateContactsLoginState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        timer.cancel();
        finish();
    }
}
