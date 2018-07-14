package schedule.com.syed.muharram;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener,
        ServerResponseListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener

{

    private ListView mMuharramListView;
    private Toolbar mToolbar;
    private ImageView mIvHeaderBkgrd;
    private BottomNavigationView mBottomNavigationView;

    private float mHeaderHeight;
    private float mMinHeaderHeight;
    List<ScheduleDataModel> mMuharramSchedule = null;
    private ScheduleArrayAdapter mMuharramAdapter;

    protected MuharramClient    mMuharramClient = null;

    private static final String TAG = "MainActivity";





    //------

    private TextView 				mTvCityName;
    private TextView 				mTvTodayDate;
    private TextView 				mTvHijriDate;
    private List<PrayTime> 			mPrayTimes;
    private ListView	 			mLvPrayTimes;
    private PrayTimeAdapter 		mAdapter;
    private boolean					mPrayerTimesFromWebInProgress;
    private SwipeRefreshLayout mSwipeRefreshLayout	= null;
    private	boolean					mLocationProcessed = false;
    private LocationUpdateWaitTimer mLocationUpdateWaitTimer = null;
    private boolean					mIsLocationInProgress;
    private boolean					mIsSettingsAlertDisplayed;
    private boolean					mIsFreshLaunched;

    // ======= Google Play Services..
    //private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation = null;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


        /*
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            this.setTitle("");// Need this to make it little compatible with API 16. might work for API 14 as well.
            View view = inflater.inflate(R.layout.fragment_pray_times, container, false);
            setupUI(view);
            setDates();
            mIsFreshLaunched = true;
            return view;
        }
        */

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private void setupUI() {
        //mTvCityName 			= (TextView) findViewById(R.id.tvCityName);
        //mTvHijriDate			= (TextView) findViewById(R.id.tvHijriDate);
        //mTvTodayDate		 	= (TextView) findViewById(R.id.tvEnglishDate);
        //mLvPrayTimes 			= (eu.erikw.PullToRefreshListView) findViewById(R.id.lvPrayTimes);
        mLvPrayTimes 			= (ListView) findViewById(R.id.lvPrayTimes);

        //Initialize swipe to refresh view
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mPrayerTimesFromWebInProgress)
                    return;
                //    SabaApplication.sendAnalyticsEvent(getResources().getString(R.string.prayer_times_fragment),
                //            getResources().getString(R.string.event_category_prayer_times),
                //            getResources().getString(R.string.refresh_event_action_swiped),
                //            getResources().getString(R.string.refresh_event_label));

                //Refreshing data from server
                refreshUI();
            }
        });

        // shows the refreshView in begining - if we sent the network request.
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                refreshUI();
            }
        });

    }

    private void clearUIControls(){
        if(mAdapter!=null)
            mAdapter.clear();
        //mTvHijriDate.setText("");
        //mTvCityName.setText("Loading...");
    }

    private void refreshUI(){
        if (mGoogleApiClient != null) {
            if(!isLocationEnabled(this /*this*/)){
                showSettingsAlert();

            } else {
                //mIsSettingsAlertDisplayed = false;
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(true);

                mLastLocation = null;
                clearUIControls();

                setDates();
                // Kicking off the process to get the location and then prayer times,
                // first we will try Database, if city doesn't exits then we will reach out
                // to web to get the prayer times.
                if (mGoogleApiClient != null) {
                    if(!mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.connect();
                    } else {
                        if(this.checkAndRequestPermissions()){
                            startLocationUpdates();
                        }
                        //processCurrentLocation();
                    }
                }
            }
        }
    }

    private String getCurrentTimezoneOffsetInMinutes() {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

        return String.format("%d", offsetInMillis/(1000*60)); // in minutes
    }

    private void setDates(){
        // Setting english date.
        if(mTvTodayDate != null){
            DateFormat dateInstance = SimpleDateFormat.getDateInstance(DateFormat.FULL);
            mTvTodayDate.setText(dateInstance.format(Calendar.getInstance().getTime()));
        }

        // get Hijri date.
        String hijriDate = mMuharramClient.getHijriDate();
        if(mTvHijriDate != null){
            mTvHijriDate.setText(hijriDate);
        }

        // try sending a network call to get the hijridate from SABA server.
        //if(hijriDate==null || hijriDate.isEmpty()){
        //	mMuharramClient.getHijriDate("hijriDate", this);
        //}
    }

    public void showAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this /*this*/);

        // Setting Dialog Title
        alertDialog.setTitle("Geocoder is not responding in timely fashion.");

        // Setting Dialog Message
        alertDialog.setMessage("Make sure, you are connected to internet and GPS is working. Please try again in few minutes.");

        // on pressing OK button
        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String cityName;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    cityName = bundle.getString("cityname");
                    break;
                default:
                    Log.d(TAG, "Geocoder was not able to return city_name.");
                    cityName = null;
            }
            Log.d(TAG, "Prayer City Name: " + cityName);
            mTvCityName.setText(cityName);

            if(cityName == null || cityName.isEmpty()) {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                //SabaApplication.sendAnalyticsEvent(getResources().getString(R.string.prayer_times_fragment),
                //        getResources().getString(R.string.event_category_prayer_times),
                //        getResources().getString(R.string.geocoder_event_action_error),
                //        getResources().getString(R.string.geocoder_event_label));

                showAlert();
                return;
            }

            // checking our database for city name and prayerTimes
            int index = cityName.indexOf(',');
            if(index != -1){
                String city = cityName.substring(0, index);
                if(!city.isEmpty())
                    getPrayerTimes(city);
            }
        }
    }

    public void processCurrentLocation(){
        if (mLastLocation == null) {
            showSettingsAlert();
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        } else {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this /*this*/, "Sorry, Geocoder is not available.", Toast.LENGTH_LONG).show();
                return;
            }

            if(mIsLocationInProgress == true)
                return;

            mIsLocationInProgress = true;
            // initiate the request to get the city name based of current latitude and longitude.
            LocationBasedCityName locationBasedCityName = new LocationBasedCityName();
            locationBasedCityName.getAddressFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                    this, new GeocoderHandler());

            mLocationProcessed = true;
            stopLocationUpdates();
        }
    }
/*
	-(void) getPrayerTimesWithPlacemark:(CLPlacemark*)placemark
	withLatitude:(double)latitude
	withLongitude:(double)longitude{

    [self setCityNameWithPlacemark:placemark];
		if(){
			// call Salat Service on Saba Service
        [self getPrayerTimeFromSaba];
		} else {
			// Getting prayerTimes from Web.
        [self getPrayerTimeFromWebWithLatitude:latitude withLongitude:longitude];
		}
*/

    /**
     * getPrayerTimes(city) - checks the database if prayerTimes exists for given city.
     * If it does then will convert "PrayerTimes" to array to "PrayTime". I know it its confusing :(
     *  sorry about it...
     * **/
    public void getPrayerTimes(String city){
        //List<PrayerTimes> prayerTimes = getTodayPrayerTimesFromDB(city);

        // following block of code converts "PrayerTimes"(read from database) to
        // "PrayTime"(adapter uses this array).
		/*if(prayerTimes!=null && prayerTimes.size()>0){
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}

			mPrayTimes = new ArrayList<>(prayerTimes.size());
			PrayerTimes prayerTime = prayerTimes.get(0);

			if(prayerTime == null){
				Log.d(TAG, "Something went wrong. Dabatbase shoudn't return null.");
				return;
			}
			// Please don't change the order. It might effect the dispplay order.
			mPrayTimes.add(new PrayTime("Imsaak", prayerTime.getImsaak()));
			mPrayTimes.add(new PrayTime("Fajr", prayerTime.getFajar()));
			mPrayTimes.add(new PrayTime("Sunrise", prayerTime.getSunrise()));
			mPrayTimes.add(new PrayTime("Dhuhr", prayerTime.getZohar()));
			mPrayTimes.add(new PrayTime("Sunset", prayerTime.getSunset()));
			mPrayTimes.add(new PrayTime("Maghrib", prayerTime.getMaghrib()));
			mPrayTimes.add(new PrayTime("Midnight", prayerTime.getMidnight()));

			if(this!=null) {
				mAdapter = new PrayTimeAdapter(this, mPrayTimes);
				mLvPrayTimes.setAdapter(mAdapter);
			}
		} else*/

        if(city.equals("San Jose") || city.equals("Milpitas") ||
                city.equals("Sunnyvale") || city.equals("Gilroy") ||
                city.equals("Morgan Hill") || city.equals("Mountain View") ||
                city.equals("Fremont") || city.equals("Santa Clara") ||
                city.equals("Campbell") || city.equals("Los Gatos") ||
                city.equals("Cupertino") || city.equals("Saratoga") ||
                city.equals("Alum Rock") || city.equals("Evergreen") ||
                city.equals("Newark") ){

            mMuharramClient.getPrayerTimeFromSaba("prayerTimesFromSaba", this);
        } else {
            // Now, its time to get the prayer times from web.
            // Once, we will get the date, pasrse it and display on UI.

            //we have the city but we want now to get the prayer times only.
            //We are going to send network request yo get prayerInfo.
            mMuharramClient.getPrayTimes(getCurrentTimezoneOffsetInMinutes(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), this);
        }
    }


    //------ refresh menu item.
    //@Override
    //public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    //    super.onCreateOptionsMenu(menu, inflater);
    //    menu.clear();
    //    inflater.inflate(R.menu.refresh_menu, menu);
    //}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* switch (item.getItemId()) {
            case R.id.refreshFragment:
                SabaApplication.sendAnalyticsEvent(getResources().getString(R.string.prayer_times_fragment),
                        getResources().getString(R.string.event_category_prayer_times),
                        getResources().getString(R.string.refresh_event_action_clicked),
                        getResources().getString(R.string.refresh_event_label));

                refreshUI();
                return true;
        } */

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient !=null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mIsFreshLaunched){
            mIsFreshLaunched = false;
            return;
        }

        refreshUI();
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        // Location updates intervals in sec
        final int UPDATE_INTERVAL = 2000; // 2 sec
        final int FATEST_INTERVAL = 2000; // 2 sec
        final int DISPLACEMENT = 0; // 0 meters

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                //finish();
                // lets find out what needs to be done.
            }
            return false;
        }
        return true;
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        if(mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        if(this.checkAndRequestPermissions()){
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        if(mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if(mLocationUpdateWaitTimer == null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

            mLocationProcessed = false;
            mLocationUpdateWaitTimer = new LocationUpdateWaitTimer(5500, 5500);
            mLocationUpdateWaitTimer.start();
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        mIsLocationInProgress = false;
        mLocationProcessed = true;
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    /*
     *
     *
     * */
    @Override
    public void onLocationChanged(Location location) {
        if(mLastLocation == null || mLocationProcessed){
            mLastLocation = location;
            return;
        }

        if(mLocationUpdateWaitTimer==null || isItBetterLocation(location)) {
            processCurrentLocation();
        }

        // Assign the new location
        mLastLocation = location;
    }

    // if oldLocation and newLocation is same then we should get the city name and then prayerTimes.
    private boolean isItBetterLocation(Location newLocation){
        double oldLon = Math.round(mLastLocation.getLongitude()*1000)/1000.0d;
        double oldLat = Math.round(mLastLocation.getLatitude()*1000)/1000.0d;

        double newLon = Math.round(newLocation.getLongitude()*1000)/1000.0d;
        double newLat = Math.round(newLocation.getLatitude()*1000)/1000.0d;

        return (oldLon == newLon && oldLat == newLat);
    }
    // show settings Alert
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        mSwipeRefreshLayout.setRefreshing(false);
        if(mIsSettingsAlertDisplayed)
            return;

        mIsSettingsAlertDisplayed = true;
        AlertDialog.Builder alertDialog;

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            alertDialog = new AlertDialog.Builder(this);
        }

        // Setting Dialog Title
        alertDialog.setTitle("Location Settings");

        // Setting Dialog Message
        alertDialog.setMessage("Location is NOT enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                mIsSettingsAlertDisplayed = false;
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mIsSettingsAlertDisplayed = false;
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 10;

    private boolean checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            //int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            List<String> listPermissionsNeeded = new ArrayList<>();
//			if (locationPermission != PackageManager.PERMISSION_GRANTED) {
//				listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
//			}
            if (coarsePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            refreshUI();
                            Log.e("msg", "location granted - ACCESS_COARSE_LOCATION");
                        }
                    }
//					else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
//						if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//							refreshUI();
//							Log.e("msg", "location granted - ACCESS_FINE_LOCATION");
//						}
//					}
                }
            }
        }
    }

    // CountDownTimer class
    class LocationUpdateWaitTimer extends CountDownTimer {
        public LocationUpdateWaitTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            mLocationUpdateWaitTimer.cancel();
            mLocationUpdateWaitTimer = null;
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }
    //----- End-copy





    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mMuharramListView.setVisibility(View.VISIBLE);
                    mLvPrayTimes.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    mMuharramListView.setVisibility(View.GONE);
                    mLvPrayTimes.setVisibility(View.VISIBLE);
                    return true;
                /*case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;*/
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMuharramClient = MuharramApplication.getSabaClient();

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        initMeasure();
        initView();
        initListViewHeader();
        initListView();
        initEvent();
        setupUI();
    }

    private void initMeasure() {
        mHeaderHeight = getResources().getDimension(R.dimen.header_height);
        mMinHeaderHeight = getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
    }

    private void initView() {
        mMuharramListView = (ListView) findViewById(R.id.lv_main);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        mToolbar = (Toolbar) findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);
    }

    private void initListView() {
        mMuharramSchedule = new ArrayList<ScheduleDataModel>();
        mMuharramClient.getMuharramSchedule(this);
        mMuharramAdapter = new ScheduleArrayAdapter(this, this, mMuharramSchedule);
        mMuharramListView.setAdapter(mMuharramAdapter);
        mMuharramListView.setVisibility(View.GONE);
    }

    private void initListViewHeader() {
        View headerContainer = LayoutInflater.from(this).inflate(R.layout.header, mMuharramListView, false);
        mIvHeaderBkgrd = (ImageView) headerContainer.findViewById(R.id.img_header_bg);
        //mLayout = (RelativeLayout)headerContainer.findViewById(R.id.rl_header);
        mMuharramListView.addHeaderView(headerContainer);
    }

    private void initEvent() {
        mMuharramListView.setOnScrollListener(this);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mMuharramListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long viewId = view.getId();

                if (viewId == R.id.tvAddress) {
                    /*SpannableString spanStr = new SpannableString(((TextView)view).getText());
                    spanStr.setSpan(new UnderlineSpan(), 0, spanStr.length(), 0);
                    ((TextView)view).setText(spanStr);*/

                   // Use the following code to open it with map app on click as follows :

                    Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="
                            +((TextView)view).getText().toString()));
                    startActivity(geoIntent);

                } else if (viewId == R.id.tvName) {
                    //Toast.makeText(this, "Name clicked", Toast.LENGTH_SHORT).show();
                }



                //Intent i=new Intent(ListOfAstrologers.this,AstroProForUser.class);
                //i.putExtra("hello",adapter.getItem(position).getUsername() );
                //startActivity(i);
            }
        });
    }

    protected void populatePrograms() {
        //mSwipeRefreshLayout.setRefreshing(true);
        mMuharramAdapter.clear();
        //mRefreshInProgress = true;
        mMuharramClient.getMuharramSchedule(this);
    }

    /*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        long viewId = view.getId();

        if (viewId == R.id.tvAddress) {
            Toast.makeText(this, "Address clicked", Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.tvName) {
            Toast.makeText(this, "Name clicked", Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        float scrollY = getScrollY(view);

        float headerBarOffsetY = mHeaderHeight - mMinHeaderHeight;
        float offset = 1 - Math.max((headerBarOffsetY - scrollY) / headerBarOffsetY, 0f);

        mToolbar.setBackgroundColor(Color.argb((int) (offset * 255), 0, 0, 0));
        mIvHeaderBkgrd.setTranslationY(scrollY / 2);

        if (scrollY > headerBarOffsetY) {
            mToolbar.setTitle(getResources().getString(R.string.toolbar_title));
        } else {
            mToolbar.setTitle("");
        }


    }

    public float getScrollY(AbsListView view) {
        View child = view.getChildAt(0);

        if (child == null)
            return 0;

        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = child.getTop();

        float headerHeight = 0;
        if (firstVisiblePosition >= 1)
            headerHeight = mHeaderHeight;

        return -top + firstVisiblePosition * child.getHeight() + headerHeight;
    }

    // ServerResponseListener implementation

    @Override
    public void processJsonObject(String programName, JSONObject response) {
        if(response == null){
            Log.d(TAG, "processJsonObject: responseJSONArray is null");
            return;
        }

        if(programName.compareToIgnoreCase("prayerTimesFromSaba") == 0){
            try{
                String hijriDate = response.getString("hijridate");
                if(hijriDate != null){
                    Log.d(TAG, "HijriDate: " + hijriDate);
                    if(mTvHijriDate != null){
                        mTvHijriDate.setText(hijriDate);
                    }
                    mMuharramClient.saveHijriDate(hijriDate);
                }

                mPrayTimes = PrayTime.fromSabaPraytimesJSON(response);
                if(this != null) {
                    mAdapter = new PrayTimeAdapter(this, mPrayTimes);
                    mLvPrayTimes.setAdapter(mAdapter);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(mPrayerTimesFromWebInProgress)
                mSwipeRefreshLayout.setRefreshing(true);

            return; // we should return from here. Processing of hijriDate and pray times from Saba is done here.
        } else if (programName.compareToIgnoreCase("prayerTimes") == 0) {

            if(mPrayerTimesFromWebInProgress)
                mSwipeRefreshLayout.setRefreshing(true);

            // Handling data returned from http://praytime.info
            //mLvPrayTimes.onRefreshComplete();
            //{"Fajr":"05:59","Isha":"18:18","Asr":"14:43","Dhuhr":"12:11","Sunset":"17:01","Sunrise":"07:21","Maghrib":"17:19","Imsaak":"05:48"}
            Log.d(TAG, "prayerTimes: " + response.toString());
            mPrayTimes = PrayTime.fromJSON(response);
            if(this != null) {
                mAdapter = new PrayTimeAdapter(this, mPrayTimes);
                mLvPrayTimes.setAdapter(mAdapter);
            }

            return;
        }

        mPrayerTimesFromWebInProgress = false;

        try{
            processJsonObject("MuharramSchedule", response.getJSONArray("Sheet1"));
        } catch (JSONException e){

        }
    }

    @Override
    public void processJsonObject(String programName, JSONArray responseJSONArray) {
        //mRefreshInProgress = false;
        //if (mSwipeRefreshLayout.isRefreshing()) {
        //    mSwipeRefreshLayout.setRefreshing(false);
        //}

        if(responseJSONArray == null){
            Log.d(TAG, "processJsonObject: responseJSONArray is null");
            return;
        }


        mMuharramSchedule = ScheduleDataModel.fromJSONArray(programName, responseJSONArray);
        mMuharramAdapter.addAll(mMuharramSchedule);
        mMuharramListView.setVisibility(View.VISIBLE);
    }


    /*
    @Override
    public void processJsonObject(String programName, JSONObject response) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        if(response == null){
            Log.d(TAG, "json object is null for :" + programName);
            return;
        }

        if(programName.compareToIgnoreCase("prayerTimesFromSaba") == 0){
            try{
                String hijriDate = response.getString("hijridate");
                if(hijriDate != null){
                    Log.d(TAG, "HijriDate: " + hijriDate);
                    if(mTvHijriDate != null){
                        mTvHijriDate.setText(hijriDate);
                    }
                    mMuharramClient.saveHijriDate(hijriDate);
                }

                mPrayTimes = PrayTime.fromSabaPraytimesJSON(response);
                if(this != null) {
                    mAdapter = new PrayTimeAdapter(this, mPrayTimes);
                    mLvPrayTimes.setAdapter(mAdapter);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(mPrayerTimesFromWebInProgress)
                mSwipeRefreshLayout.setRefreshing(true);

            return; // we should return from here. Processing of hijriDate and pray times from Saba is done here.
        }

        mPrayerTimesFromWebInProgress = false;

        // Handling data returned from http://praytime.info
        //mLvPrayTimes.onRefreshComplete();
        //{"Fajr":"05:59","Isha":"18:18","Asr":"14:43","Dhuhr":"12:11","Sunset":"17:01","Sunrise":"07:21","Maghrib":"17:19","Imsaak":"05:48"}
        Log.d(TAG, "prayerTimes: " + response.toString());
        mPrayTimes = PrayTime.fromJSON(response);
        if(this != null) {
            mAdapter = new PrayTimeAdapter(this, mPrayTimes);
            mLvPrayTimes.setAdapter(mAdapter);
        }
    } */
}
