package myhomework.com.meilmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import myhomework.com.meilmanager.adapter.EmailListViewAdapter;
import myhomework.com.meilmanager.database.ContactDatabaseHandler;
import myhomework.com.meilmanager.database.MessageDatabaseHandler;
import myhomework.com.meilmanager.model.EmailData;
import myhomework.com.meilmanager.model.UserInfomation;




public class LoginActivity extends Activity {
    private EditText usernameEditBox;
    private Button loginButton;
    private TextView signupLink;

    private ServerAPI serverAPI = ServerAPI.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();
    }

    public void initUI() {
        usernameEditBox = (EditText) findViewById(R.id.input_username);

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverAPI.login(usernameEditBox.getText().toString());
                Intent iter = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(iter);

            }
        });

        signupLink = (TextView) findViewById(R.id.link_signup);
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }
}
