package net.tudelft.xflash.gogogym;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import android.widget.TabHost;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;


public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    protected static final String TAG = "DashboardActivity";

    /* Basic location */
    protected Location mLastLocation;

    /* Geofencing and activity detection */
    /**
     * A receiver for DetectedActivity objects broadcast by the
     * {@code ActivityDetectionIntentService}.
     */
    protected DashboardActivity.ActivityDetectionBroadcastReceiver mBroadcastReceiver;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The DetectedActivities that we track in this sample. We use this for initializing the
     * {@code DetectedActivitiesAdapter}. We also use this for persisting state in
     * {@code onSaveInstanceState()} and restoring it in {@code onCreate()}. This ensures that each
     * activity is displayed with the correct confidence level upon orientation changes.
     */
    private ArrayList<DetectedActivity> mDetectedActivities;

    /**
     * The list of geofences used in this sample.
     */
    protected ArrayList<Geofence> mGeofenceList;

    /**
     * Used to keep track of whether geofences were added.
     */
    private boolean mGeofencesAdded;
    private boolean isGeofencesEntered;
    private boolean isInGym = Boolean.FALSE;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    /**
     * Used to persist application state about whether geofences were added.
     */
    private SharedPreferences mSharedPreferences;

    //call sqlite, mandatory!!
    public static UData cur_user;
    private DBHandler db;

    private String start_time;
    private String userid;
    private String name;

    private ArrayList<UserLog> activityLogs = new ArrayList<>();

    private ListView activity_lv;

    private Handler handler;
    private Runnable runnable;

    private ArrayAdapter<UserLog> adapter;

    private GifImageView gifImageView;

    // Progress Bar (energy & exp)
    ProgressBar pg_energy;
    ProgressBar pg_exp;

    int pet_exp;
    int pet_energy;
    int pet_level;
    int pet_max_energy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Bundle inBundle = getIntent().getExtras();
        String surname = "";
        String imageUrl = "";
        if ((inBundle.get("userid") == null) || ((inBundle.get("userid") == "null"))){
            userid = getLoggedId();
            name = getLoggedName();
        } else {
            userid = inBundle.get("userid").toString();
            name = inBundle.get("name").toString();
            setLoggedId(userid);
            setLoggedName(name);
        }

        // Set title
        setTitle("" + name + " " + surname);

        // FB context
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Download profile pic async
        if(imageUrl.equals(""))
        {
            Profile profile = Profile.getCurrentProfile();
            if(profile != null)
                imageUrl = profile.getProfilePictureUri(200,200).toString();
        } else {
//            new DownloadImage((ImageView) findViewById(R.id.profile_drawer)).execute(imageUrl);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /* Geofencing and activity detection create */
        // Get a receiver for broadcasts from ActivityDetectionIntentService.
        mBroadcastReceiver = new DashboardActivity.ActivityDetectionBroadcastReceiver();

        // Reuse the value of mDetectedActivities from the bundle if possible. This maintains state
        // across device orientation changes. If mDetectedActivities is not stored in the bundle,
        // populate it with DetectedActivity objects whose confidence is set to 0. Doing this
        // ensures that the bar graphs for only only the most recently detected activities are
        // filled in.
        if (savedInstanceState != null && savedInstanceState.containsKey(
                Constants.DETECTED_ACTIVITIES)) {
            mDetectedActivities = (ArrayList<DetectedActivity>) savedInstanceState.getSerializable(
                    Constants.DETECTED_ACTIVITIES);
        } else {
            mDetectedActivities = new ArrayList<DetectedActivity>();

            // Set the confidence level of each monitored activity to zero.
            for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
                mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
            }
        }

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);

        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();

        if (getIntent().hasExtra("fromNotification")) {
            isGeofencesEntered = Boolean.TRUE;
            isInGym = Boolean.TRUE;
        } else {
            isGeofencesEntered = Boolean.FALSE;
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        db = new DBHandler(this);
        DashboardActivity.cur_user = db.getUData(1);

        // Set PANDA
        // TODO: Threshold value, mood managament

        pet_exp = cur_user.pet_exp;
        pet_energy = cur_user.pet_energy;
        pet_level =  (int) Math.floor(pet_exp/100);
        pet_max_energy = 20+pet_level;

        // Draw PANDA
        gifImageView = (GifImageView) findViewById(R.id.GifImageView);
        gifImageView.setGifImageResource(R.drawable.pandas_happy);

        pg_energy = (ProgressBar) findViewById(R.id.progressBarEnergy);
        pg_exp = (ProgressBar) findViewById(R.id.progressBarExp);

        Integer pg_exp_int = pg_exp.getProgress();  // get value exp
        pg_exp.setProgress(pet_exp); // set value exp
        pg_energy.setProgress(pet_energy);

        // Set PANDA
        // TODO: Threshold value, mood managament
        double ratio = pet_energy /  pet_max_energy;
        if(ratio > 0.7){
            // Happy
            gifImageView.setGifImageResource(R.drawable.pandas_happy);
        }else if(ratio > 0.5){
            // Eating
            gifImageView.setGifImageResource(R.drawable.pandas_eating);
        }else if(pet_energy >= 1){
            // Sad
            gifImageView.setGifImageResource(R.drawable.pandas_sad);
        }else{
            // Dead
            gifImageView.setGifImageResource(R.drawable.pandas_dead);
        }

        pg_exp.setMax(100);    // Max exp
        pg_energy.setMax(pet_max_energy); // Max energy

        // TODO: Display activities
        activityLogs = db.getAllLogs();
        adapter = new detectedActivitiesAdapter(this, 0, activityLogs);
        activity_lv = (ListView) findViewById(R.id.detected_activities_listview);
        activity_lv.setAdapter(adapter);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                activityLogs = db.getAllLogs();
                adapter = new detectedActivitiesAdapter(getApplicationContext(), 0, activityLogs);
                handler.postDelayed(this, 1000);
                activity_lv.setAdapter(adapter);

                UData current_user = db.getUData(1);
                if (isInGym) {
                    pet_energy = current_user.pet_energy+Constants.ACTIVE_ENERGY_DIFF;
                    if (pet_energy>100) { pet_energy = 100; }
                    db.updatePoint(1, current_user.pet_exp, current_user.pet_energy+Constants.ACTIVE_ENERGY_DIFF);
                    Log.d("GEOFENCE", "increasing, now: "+Integer.toString(current_user.pet_exp) + ", " +Integer.toString(current_user.pet_energy));
                } else {
                    pet_energy = current_user.pet_energy-Constants.ACTIVE_ENERGY_DIFF;
                    if (pet_energy<0) { pet_energy = 0; }
                    db.updatePoint(1, current_user.pet_exp, pet_energy);
                    Log.d("GEOFENCE", "decreasing, now: "+Integer.toString(current_user.pet_exp) + ", " +Integer.toString(current_user.pet_energy));
                }
                pg_exp.setProgress(current_user.pet_exp);
                pg_energy.setProgress(current_user.pet_energy);
                double ratio = pet_energy /  pet_max_energy;
                if(ratio > 0.7){
                    // Happy
                    gifImageView.setGifImageResource(R.drawable.pandas_happy);
                }else if(ratio > 0.5){
                    // Eating
                    gifImageView.setGifImageResource(R.drawable.pandas_eating);
                }else if(pet_energy >= 1){
                    // Sad
                    gifImageView.setGifImageResource(R.drawable.pandas_sad);
                }else{
                    // Dead
                    gifImageView.setGifImageResource(R.drawable.pandas_dead);
                }
            }
        };

        handler.postDelayed(runnable, 1000);

        // Tab Host
        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        //Tab 1 -- Activity
        TabHost.TabSpec spec = host.newTabSpec("Activity");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Activity");
        host.addTab(spec);

        //Tab 2 -- Location
        spec = host.newTabSpec("Location");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Location");
        host.addTab(spec);

        //Tab 3 -- Inventory
        spec = host.newTabSpec("Inventory");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Inventory");
        host.addTab(spec);

        //Tab 4 -- Rewards
        spec = host.newTabSpec("Rewards");
        spec.setContent(R.id.tab4);
        spec.setIndicator("Rewards");
        host.addTab(spec);

        TextView ld = (TextView) findViewById(R.id.level_game);
        ld.setText("Level " + pet_level);
        // Change Tab color
        for(int i=0; i<host.getTabWidget().getChildCount(); i++)
        {
            TextView tv = (TextView) host.getTabWidget().getChildAt(i).findViewById(android.R.id.title); //Unselected Tabs
            tv.setTextColor(Color.parseColor("#ffffff"));
            tv.setTextSize(10);
        }

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_logout)
        {
            LoginManager.getInstance().logOut();
            Intent login = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver that informs this activity of the DetectedActivity
        // object broadcast sent by the intent service.
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        // Unregister the broadcast receiver that was registered during onResume().
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (isGeofencesEntered) {
            stopGeofences();
            startActivityUpdates();
            Toast.makeText(this, "Welcome back " + name + ", lets start working out!",
                    Toast.LENGTH_LONG).show();
            // TODO: ADD EXP POINTS & RECORDS FOR VISITING
            start_time = Calendar.getInstance().getTime().toString();
            db.addLog( Integer.parseInt(userid), 1, start_time, Constants.VISIT_DESC);
            UData userdat = db.getUData(Integer.parseInt(userid));
            int updatedExp = userdat.pet_exp + Constants.VISIT_EXP_INC;
            int updatedEnergy = userdat.pet_energy + Constants.VISIT_ENERGY_INC;
            db.updatePoint(Integer.parseInt(userid), updatedExp, updatedEnergy);
            isGeofencesEntered = Boolean.FALSE;
            activityLogs = db.getAllLogs();
            adapter = new detectedActivitiesAdapter(this, 0, activityLogs);
        }
        else {
            try {
//                stopActivityUpdates();
            } catch (Error err) {

            }
            startGeofences();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Registers for activity recognition updates using
     * {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates} which
     * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
     * implements the PendingResult interface, the activity itself receives the callback, and the
     * code within {@code onResult} executes. Note: once {@code requestActivityUpdates()} completes
     * successfully, the {@code DetectedActivitiesIntentService} starts receiving callbacks when
     * activities are detected.
     */
    public void startActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    /**
     * Removes activity recognition updates using
     * {@link com.google.android.gms.location.ActivityRecognitionApi#removeActivityUpdates} which
     * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
     * implements the PendingResult interface, the activity itself receives the callback, and the
     * code within {@code onResult} executes. Note: once {@code removeActivityUpdates()} completes
     * successfully, the {@code DetectedActivitiesIntentService} stops receiving callbacks about
     * detected activities.
     */
    public void stopActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Retrieves a SharedPreference object used to store or read values in this app. If a
     * preferences file passed as the first argument to {@link #getSharedPreferences}
     * does not exist, it is created when {@link SharedPreferences.Editor} is used to commit
     * data.
     */
    private SharedPreferences getSharedPreferencesInstance() {
        return getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    private String getLoggedId() {
        return getSharedPreferencesInstance()
                .getString(Constants.LOGGED_USERID, "1"); // should have been 0 --> logged out
    }

    private void setLoggedId(String loggedId) {
        getSharedPreferencesInstance()
                .edit()
                .putString(Constants.LOGGED_USERID, loggedId)
                .commit();
    }

    private String getLoggedName() {
        return getSharedPreferencesInstance()
                .getString(Constants.LOGGED_USERNAME, "Bontor"); // should have been 0 --> logged out
    }

    private void setLoggedName(String loggedId) {
        getSharedPreferencesInstance()
                .edit()
                .putString(Constants.LOGGED_USERNAME, loggedId)
                .commit();
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private boolean getUpdatesRequestedState() {
        return getSharedPreferencesInstance()
                .getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private void setUpdatesRequestedState(boolean requestingUpdates) {
        getSharedPreferencesInstance()
                .edit()
                .putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates)
                .commit();
    }

    /**
     * Stores the list of detected activities in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(Constants.DETECTED_ACTIVITIES, mDetectedActivities);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Receiver for intents sent by DetectedActivitiesIntentService via a sendBroadcast().
     * Receives a list of one or more DetectedActivity objects associated with the current state of
     * the device.
     */
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "activity-detection-response-receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> updatedActivities =
                    intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
            Log.d("DashboardActivity", "Activity detected!" + updatedActivities.toString());
            if (updatedActivities.get(0).getType() != DetectedActivity.STILL) { // not still = active
                // TODO: ADD EXP & ENERGY POINTS FOR ACTIVITITY
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 30); // assume to be in gym for 30 minutes
                String time = cal.getTime().toString();
                db.finishLog(Integer.parseInt(userid), 1, start_time, time);

                db.addLog( Integer.parseInt(userid), 1, start_time, Constants.ACTIVE_DESC);
                String active_time = Calendar.getInstance().getTime().toString();
                db.finishLog(Integer.parseInt(userid), 1, start_time, active_time);

                UData userdat = db.getUData(Integer.parseInt(userid));
                int updatedExp = userdat.pet_exp + Constants.ACTIVE_EXP_INC;
                int updatedEnergy = userdat.pet_energy + Constants.ACTIVE_ENERGY_INC;
                db.updatePoint(Integer.parseInt(userid), updatedExp, updatedEnergy);

                activityLogs = db.getAllLogs();
                adapter = new detectedActivitiesAdapter(getApplicationContext(), 0, activityLogs);
            }
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void startGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void stopGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();
        } else {
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.GOGOGYM_MERCHANTS.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
    }

}
