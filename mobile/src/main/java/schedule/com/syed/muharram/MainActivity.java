package schedule.com.syed.muharram;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_schedule);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_prayer_times);
                    return true;
                /*case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;*/
            }
            return false;
        }
    };

    //@Override
    /*protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }*/

    private ListView mListView;
    private Toolbar mToolbar;
    private TextView    mTvFloatTitle;
    private ImageView mIvHeaderBkgrd;
    //private RelativeLayout mLayout;


    private float mHeaderHeight;
    private float mMinHeaderHeight;
    private float mTitleLeftMargin;
    private float mTitleSize;
    //private float mTitleSizeLarge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMeasure();
        initView();
        initListViewHeader();
        initListView();
        initEvent();
    }

    private void initMeasure() {
        mHeaderHeight = getResources().getDimension(R.dimen.header_height);
        mMinHeaderHeight = getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
        mTitleLeftMargin = getResources().getDimension(R.dimen.float_title_left_margin);
        mTitleSize = getResources().getDimension(R.dimen.float_title_size);
        //mTitleSizeLarge = getResources().getDimension(R.dimen.float_title_size_medium);
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.lv_main);
        //mTvFloatTitle = (TextView) findViewById(R.id.tv_main_title);
        mToolbar = (Toolbar) findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);
    }

    private void initListView() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            data.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.activity_list_item, android.R.id.text1, data);
        mListView.setAdapter(adapter);
    }

    private void initListViewHeader() {
        View headerContainer = LayoutInflater.from(this).inflate(R.layout.header, mListView, false);
        mIvHeaderBkgrd = (ImageView) headerContainer.findViewById(R.id.img_header_bg);
        //mLayout = (RelativeLayout)headerContainer.findViewById(R.id.rl_header);
        mListView.addHeaderView(headerContainer);
    }

    private void initEvent() {
        mListView.setOnScrollListener(this);
    }

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

        //mTvFloatTitle.setPivotX(mTvFloatTitle.getLeft() + mTvFloatTitle.getPaddingLeft());
        //float titleScale = mTitleSize / mTitleSizeLarge;
        //mTvFloatTitle.setTranslationX(mTitleLeftMargin * offset);
        //mTvFloatTitle.setTranslationY(
        //        (-(mTvFloatTitle.getHeight() - mMinHeaderHeight) +
        //                mTvFloatTitle.getHeight() * (1 - titleScale))
        //                / 2 * offset +
        //                (mHeaderHeight - mTvFloatTitle.getHeight()) * (1 - offset));

        //mTvFloatTitle.setScaleX(1 - offset * (1 - titleScale));
        //mTvFloatTitle.setScaleY(1 - offset * (1 - titleScale));

        if (scrollY > headerBarOffsetY) {
            mToolbar.setTitle(getResources().getString(R.string.toolbar_title));
            //mTvFloatTitle.setVisibility(View.GONE);
        } else {
            mToolbar.setTitle("");
            //mTvFloatTitle.setVisibility(View.VISIBLE);
        }


    }

    public float getScrollY(AbsListView view) {
        View c = view.getChildAt(0);

        if (c == null)
            return 0;

        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = c.getTop();

        float headerHeight = 0;
        if (firstVisiblePosition >= 1)
            headerHeight = mHeaderHeight;

        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }
}
