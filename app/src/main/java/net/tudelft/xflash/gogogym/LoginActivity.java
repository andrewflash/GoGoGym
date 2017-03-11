package net.tudelft.xflash.gogogym;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends FragmentActivity {
    private LoginButton loginButton;
    private Button loginButton_nonfb;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    //Facebook login button
    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Profile profile = Profile.getCurrentProfile();
            nextActivity(profile);
        }
        @Override
        public void onCancel() {        }
        @Override
        public void onError(FacebookException e) {      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call sqlite, mandatory!!
        DBHandler db = new DBHandler(this);

        //initiate DB & data (only 1st time)
        db.initiateDB();
        UData dat = new UData(1,"Bontor", "bontor@gmail.com", "12345", 17, "Bambang", 19, 571); boolean check1 = db.addUData(dat); Log.i("haha"," "+check1);
        boolean check5 = db.addGym("ETH",1.23,5.67); Log.i("haha5"," "+check5);



        //check retrieve
        String check2 = (db.getUData(1)).stringify_UData(); Log.i("haha"," "+check2);
        List<Gym> gyms = db.getAllGyms(); String check7 = gyms.get(0).stringify_Gym(); Log.i("haha7"," "+check7);
        Log.i("hoho","safe!!");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                nextActivity(newProfile);
            }
        };
        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();
                nextActivity(profile);
                Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
                Log.e("Error", e.getMessage());
            }
        };
        loginButton.setReadPermissions(Arrays.asList("user_friends", "user_photos", "public_profile"));
        loginButton.registerCallback(callbackManager, callback);

        // Login Button non-FB
        Button loginButton_nonfb = (Button) findViewById(R.id.btn_login);
        loginButton_nonfb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText inputEmail = (EditText) findViewById(R.id.input_email);
                EditText inputPassword = (EditText) findViewById(R.id.input_password);
                if (inputEmail.getText().toString().equals("bontor") && !inputPassword.getText().toString().equals("")) {
                    Intent main = new Intent(LoginActivity.this, DashboardActivity.class);

                    // TODO: Authentikasi email/username & password

                    main.putExtra("name", "Bontor");
                    main.putExtra("surname", "Humala");
                    main.putExtra("imageUrl", "");

                    startActivity(main);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please input username and/or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Facebook login
        Profile profile = Profile.getCurrentProfile();
        nextActivity(profile);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    protected void onStop() {
        super.onStop();
        //Facebook login
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        //Facebook login
        callbackManager.onActivityResult(requestCode, responseCode, intent);

    }


    private void nextActivity(Profile profile){
        if(profile != null){
            Intent main = new Intent(LoginActivity.this, DashboardActivity.class);
            main.putExtra("name", profile.getFirstName());
            main.putExtra("surname", profile.getLastName());
            main.putExtra("imageUrl", profile.getProfilePictureUri(200,200).toString());
            startActivity(main);
            finish();
        }
    }
}
