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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener, ServerResponseListener {

    private ListView mListView;
    private Toolbar mToolbar;
    private ImageView mIvHeaderBkgrd;
    private BottomNavigationView mBottomNavigationView;

    private float mHeaderHeight;
    private float mMinHeaderHeight;
    List<ScheduleDataModel> mMuharramSchedule = null;
    private ScheduleArrayAdapter mAdapter;

    protected MuharramClient    mMuharramClient = null;



    private static final String TAG = "MainActivity";


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mListView.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    mListView.setVisibility(View.GONE);
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
        mListView = (ListView) findViewById(R.id.lv_main);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        mToolbar = (Toolbar) findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);
    }

    private void initListView() {
        mMuharramSchedule = new ArrayList<ScheduleDataModel>();
        mMuharramClient.getMuharramSchedule(this);
        mAdapter = new ScheduleArrayAdapter(this, this, mMuharramSchedule);
        mListView.setAdapter(mAdapter);
        mListView.setVisibility(View.GONE);
    }

    private void initListViewHeader() {
        View headerContainer = LayoutInflater.from(this).inflate(R.layout.header, mListView, false);
        mIvHeaderBkgrd = (ImageView) headerContainer.findViewById(R.id.img_header_bg);
        //mLayout = (RelativeLayout)headerContainer.findViewById(R.id.rl_header);
        mListView.addHeaderView(headerContainer);
    }

    private void initEvent() {
        mListView.setOnScrollListener(this);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        mAdapter.clear();
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
        mAdapter.addAll(mMuharramSchedule);
        mListView.setVisibility(View.VISIBLE);
    }
}
