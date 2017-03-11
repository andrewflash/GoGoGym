package net.tudelft.xflash.gogogym;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call sqlite, mandatory!!
        DBHandler db = new DBHandler(this);

        //initiate DB & data (only 1st time)
        db.initiateDB();
        UData dat = new UData(1,"Bontor", "bontor@gmail.com", "12345", 17, "Bambang", 9, 11); boolean check1 = db.addUData(dat); Log.i("haha"," "+check1);
        boolean check5 = db.addGym("ETH",1.23,5.67); Log.i("haha5"," "+check5);



        //check retrieve
        String check2 = (db.getUData(1)).stringify_UData(); Log.i("haha"," "+check2);
        List<Gym> gyms = db.getAllGyms(); String check7 = gyms.get(0).stringify_Gym(); Log.i("haha7"," "+check7);
        Log.i("hoho","safe!!");


        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
//        info = (TextView)findViewById(R.id.);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
