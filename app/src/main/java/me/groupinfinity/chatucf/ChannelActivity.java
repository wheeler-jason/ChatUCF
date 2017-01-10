package me.groupinfinity.chatucf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.location.Location;
import android.widget.TextView;

//Added by Mitchell to get users location
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient; //new
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks; //new
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener; //new
import com.google.android.gms.location.LocationListener; //new
import com.google.android.gms.location.LocationRequest; //new
import com.google.android.gms.location.LocationServices; //new

import static android.content.Intent.ACTION_VIEW;

//added the 3 implements to get location to function properly
public class ChannelActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    ArrayList<Chatroom> chatrooms;
    ArrayList<String> chatroomStrings;
    Location myLocation;

    private MyClassAdapter myAdapter;

    //start new
    protected static final String TAG = "ChannelActivity";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    protected LocationRequest mLocationRequest;
    protected static Location mCurrentLocation;
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;

    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    //end new

    //moved the locations for the chat rooms to be global


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        //start new code for location enabling by Mitchell
        //locate the UI widgets
        /*

        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        //set Labels
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);
        */

        mRequestingLocationUpdates = true;     //originally set to false from sample app but since our app will always need to
        //mLastUpdateTime = "";                   // be requesting updates set to true

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();

        //end new
        //MOVED ALL TO UPDATE UI

    }

    public static double getMiles(float meters)
    {
        return meters*0.000621371192;
    }

    // Adds the same menu as in chat room but doesn't include number of subscribers.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_modified, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        MainActivity main = new MainActivity();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_sign_out:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            case R.id.action_doc:
                Uri uri = Uri.parse("https://drive.google.com/folderview?id=0B3mnZ3UIlXSuS0VZTnhrREZ6d1U&usp=sharing");
                Intent intent2 = new Intent(ACTION_VIEW, uri);
                startActivity(intent2);
                return true;

            case R.id.action_manual:
                Uri uri2 = Uri.parse("https://docs.google.com/document/d/1l3BnF_wMAlLRFtVkTJ0Xu3rbDa4xKfhGlCNLhR8eZJI/edit?usp=sharing");
                Intent intent3 = new Intent(ACTION_VIEW, uri2);
                startActivity(intent3);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //start new
    //Enables users location to be obtained

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    private void updateUI() {
        //for debugging purposes, displays users lat, long, and last location update time
       // mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel,
               // mCurrentLocation.getLatitude()));
       // mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel,
               // mCurrentLocation.getLongitude()));
       // mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel,
               // mLastUpdateTime));

        // put the UI stuff here because getting the current location of the user was pulling a
        // null pointer reference as it hadnt been instansiated yet
        //LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        // Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Chatroom studentUnion = new Chatroom("Student Union", 28.601925, -81.200535);
        Chatroom library = new Chatroom("Library", 28.600368, -81.201542);
        Chatroom starbucks = new Chatroom("Starbucks", 28.603421, -81.198883);
        Chatroom hec = new Chatroom("HEC", 28.600583, -81.197702);
        Chatroom gym = new Chatroom("UCF Gym", 28.595816, -81.199396);
        Chatroom arena = new Chatroom("CFE Arena", 28.607238, -81.197364);

        ListView channelList = (ListView)findViewById(R.id.listView);
        //mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //  myLocation = new Location("myLocation");

        // myLocation = getLoc();

        // myLocation.setLatitude(28.602390);
        //myLocation.setLongitude(-81.200929);


        Location locationA = new Location("locationA");
        locationA.setLatitude(studentUnion.latitude);
        locationA.setLongitude(studentUnion.longitude);

        Location locationB = new Location("locationB");
        locationB.setLatitude(library.latitude);
        locationB.setLongitude(library.longitude);

        Location locationC = new Location("locationB");
        locationC.setLatitude(starbucks.latitude);
        locationC.setLongitude(starbucks.longitude);
        Location locationD = new Location("locationD");
        locationD.setLatitude(hec.latitude);
        locationD.setLongitude(hec.longitude);

        Location locationE = new Location("LocationE");
        locationE.setLatitude(gym.latitude);
        locationE.setLongitude(gym.longitude);

        Location locationF = new Location("LocationF");
        locationF.setLatitude(arena.latitude);
        locationF.setLongitude(arena.longitude);

        studentUnion.distanceTo = getMiles(mCurrentLocation.distanceTo(locationA));
        library.distanceTo = getMiles(mCurrentLocation.distanceTo(locationB));
        starbucks.distanceTo = getMiles(mCurrentLocation.distanceTo(locationC));
        hec.distanceTo = getMiles(mCurrentLocation.distanceTo(locationD));
        gym.distanceTo = getMiles(mCurrentLocation.distanceTo(locationE));
        arena.distanceTo = getMiles(mCurrentLocation.distanceTo(locationF));

        chatrooms = new ArrayList<Chatroom>();
        chatrooms.add(studentUnion);
        chatrooms.add(library);
        chatrooms.add(starbucks);
        chatrooms.add(hec);
        chatrooms.add(gym);
        chatrooms.add(arena);

        Collections.sort(chatrooms, new Comparator<Chatroom>() {
            public int compare(Chatroom x, Chatroom y) {
                if (x.distanceTo < y.distanceTo) return -1;
                if (x.distanceTo > y.distanceTo) return 1;
                return 0;
            }
        });

        chatroomStrings = new ArrayList<String>();

        for(int i = 0; i < chatrooms.size(); i++) {
            chatroomStrings.add(chatrooms.get(i).name);
        }

        myAdapter = new MyClassAdapter(ChannelActivity.this, R.layout.list_item, chatrooms);
        channelList.setAdapter(myAdapter);

        /*
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chatroomStrings);
        channelList.setAdapter(arrayAdapter);
        */



        channelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.channel = chatroomStrings.get(i).toString();

                Intent intent = new Intent(ChannelActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();

    }
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
      /*  mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel,
                    mLastLocation.getLatitude()));
            mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel,
                    mLastLocation.getLongitude()));
        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
        */
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
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
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
       // Toast.makeText(this, getResources().getString(R.string.location_updated_message),
            //    Toast.LENGTH_SHORT).show();
    }

    private Location getLoc()
    {
        return mCurrentLocation;
    }


    //end new
}
