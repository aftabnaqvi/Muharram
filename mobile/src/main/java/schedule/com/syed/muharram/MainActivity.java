package schedule.com.syed.muharram;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener,
        ServerResponseListener

{
    private TextView mTvHijriDate;
    private TextView mTvEnglishDate;
    private ListView mMuharramListView;
    private Toolbar mToolbar;
    private ImageView mIvHeaderBkgrd;
    private boolean mbInProgress = false;
    //private BottomNavigationView mBottomNavigationView;

    private float mHeaderHeight;
    private float mMinHeaderHeight;
    List<ScheduleDataModel> mMuharramSchedule = null;
    private ScheduleArrayAdapter mMuharramAdapter;

    protected MuharramClient    mMuharramClient = null;

    private static final String TAG = "MainActivity";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mMuharramListView.setVisibility(View.VISIBLE);
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

        // un comment this line....
        mMuharramClient = MuharramApplication.getSabaClient();

        initMeasure();
        initView();
        initListViewHeader();
        initListView();
        initEvent();
    }

    private void initMeasure() {
        mHeaderHeight = getResources().getDimension(R.dimen.header_height);
        mMinHeaderHeight = getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
    }

    private void initView() {
        mMuharramListView = (ListView) findViewById(R.id.lv_main);

        //mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        mToolbar = (Toolbar) findViewById(R.id.tb_main);
        //setSupportActionBar(mToolbar);
    }

    private void initListView() {
        mMuharramSchedule = new ArrayList<ScheduleDataModel>();
        mMuharramClient.getMuharramSchedule(this);
        mMuharramClient.getPrayerTimeFromSaba("prayerTimesFromSaba", this);

        mMuharramAdapter = new ScheduleArrayAdapter(this, this, mMuharramSchedule);
        mMuharramListView.setAdapter(mMuharramAdapter);
        mMuharramListView.setVisibility(View.GONE);
    }

    private void initListViewHeader() {
        View headerContainer = LayoutInflater.from(this).inflate(R.layout.header, mMuharramListView, false);
        mIvHeaderBkgrd = (ImageView) headerContainer.findViewById(R.id.img_header_bg);
        mTvHijriDate = (TextView) headerContainer.findViewById(R.id.tv_hijri_date);
        mTvEnglishDate = (TextView)headerContainer.findViewById(R.id.tv_english_date);

        DateFormat dateInstance = SimpleDateFormat.getDateInstance(DateFormat.FULL);
        mTvEnglishDate.setText(dateInstance.format(Calendar.getInstance().getTime()));

        mMuharramListView.addHeaderView(headerContainer);
    }

    private void initEvent() {
        mMuharramListView.setOnScrollListener(this);
        ///mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mMuharramListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long viewId = view.getId();

                if (viewId == R.id.tvAddress) {
                    Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="
                            +((TextView)view).getText().toString()));
                    startActivity(geoIntent);
                } else {
                    ScheduleDataModel schedule = mMuharramAdapter.getItem(position);
                    if(schedule.getMasayab().contains("Shabber Ali Khan")){
                        // We have to launch this detail activity.
                        //Intent intent = new Intent(MainActivity.this, SendMessage.class);
                        //String message = entry.getMessage();
                        //intent.putExtra(EXTRA_MESSAGE, message);
                        //startActivity(intent);
                        int x;
                    }
                }
            }
        });
    }

    protected void populatePrograms() {
        if(mbInProgress)
            return;

        mbInProgress = true;

        mMuharramAdapter.clear();
        mMuharramClient.getMuharramSchedule(this);
    }

    // menu options - refresh

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                populatePrograms();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
                if(hijriDate != null) {
                    Log.d(TAG, "HijriDate: " + hijriDate);
                    if (mTvHijriDate != null) {
                        mTvHijriDate.setText(hijriDate);
                    }
                    mMuharramClient.saveHijriDate(hijriDate);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return; // we should return from here. Processing of hijriDate and pray times from Saba is done here.
        }

        try{
            processJsonObject("MuharramSchedule", response.getJSONArray("Sheet1"));
        } catch (JSONException e){

        }
    }

    @Override
    public void processJsonObject(String programName, JSONArray responseJSONArray) {

        if(responseJSONArray == null){
            Log.d(TAG, "processJsonObject: responseJSONArray is null");
            return;
        }

        mMuharramSchedule = ScheduleDataModel.fromJSONArray(programName, responseJSONArray);
        mMuharramAdapter.addAll(mMuharramSchedule);
        mMuharramListView.setVisibility(View.VISIBLE);
        mbInProgress = false;
    }
}
