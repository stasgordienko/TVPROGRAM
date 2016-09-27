package stang.tv;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends FragmentActivity {
    public static final String TAG = "MY_APP";

    ArrayList<ChannelItem> channels;
    ArrayList<String> channelsForDate;
    ArrayList<String> dates;
    DBHelper dbHelper;
    String dateToShow;


    ViewPager pager;
    PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dates = dbHelper.getDates();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMMdd");
        String TODAY = dateFormat.format( new Date());
        dateToShow = pref.getString("dateToShow", TODAY);

        Log.d(TAG, "MainActivity dateToShow=" + dateToShow);

        if (dateToShow.equals("") || dates.size()==0 || !dates.contains(dateToShow)){
            // Открываеи SelectDateActivity
            Intent sda = new Intent(MainActivity.this, SelectDateActivity.class);
            sda.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(sda);
            finish();
        }

        onSelectDate();

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected, position = " + position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        //if intent has channelID - find index of tab and scroll to it
        //
        Intent intent = getIntent();
        String channelToShow = intent.getStringExtra("channel");
        int j;
        for (j = 0; j < channelsForDate.size(); j++) {
            if(channelsForDate.get(j).equals(channelToShow)){
                break;
            }
        }
        if(j < channelsForDate.size()) {
            pager.setCurrentItem(j,true);
        }

    }



    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(getApplicationContext(), dbHelper, position, dateToShow, channelsForDate.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String tabName = "";
            String channelID = channelsForDate.get(position);
            if(position <= channelsForDate.size()) {
                for (int i = 0; i < channels.size(); i++) {
                    if(channels.get(i).id.equals(channelID)) {
                        tabName = channels.get(i).name + " (" + channelID + ") ";
                        break;
                    }
                }
            } else {
                tabName = channelID + "(" + position + ")";
            }
            return tabName;
        }

        public int getItemPosition(Object object){
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return channelsForDate.size();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //MenuItem mi = menu.add(0, 1, 0, "Update");
        //mi.setIntent(new Intent(this, UpdateActivity.class));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_categories:
                // Открываеи SelectDateActivity
                i = new Intent(this, CategoriesActivity.class);
                startActivity(i);
                return true;

            case R.id.action_favorites:
                // Открываеи SelectDateActivity
                i = new Intent(this, ChannelsActivity.class);
                i.putExtra("favorites", true);
                startActivity(i);
                return true;

            case R.id.action_channels:
                // Открываеи SelectDateActivity
                i = new Intent(this, ChannelsActivity.class);
                //i.putExtra("",);
                startActivity(i);
                return true;

            case R.id.action_select_date:
                // Открываеи SelectDateActivity
                i = new Intent(this, SelectDateActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;

            case R.id.action_update:
                // Открываеи UpdateActivity
                i = new Intent(this, UpdateActivity.class);
                startActivity(i);
                return true;

            case R.id.action_settings:
                // Открываеи настройки
                //i = new Intent(this, PrefActivity.class);
                //startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        pref.edit().putString("dateToShow", data.getStringExtra("date")).apply();
        onSelectDate();
    }

    void onSelectDate() {
        //GET channels from db to ArrayList
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dateToShow = pref.getString("dateToShow", dateToShow);

        Log.d(TAG,"OnSelectDate");
        channels = dbHelper.getChannels("", "");
        channelsForDate = dbHelper.getProgramChannels(dateToShow);
        dates = dbHelper.getDates();

        if(pager != null) {
            pagerAdapter.notifyDataSetChanged();
            pager.destroyDrawingCache();
            pager.removeAllViews();
            //pager.setAdapter(null);
            //pager = null;
            //pagerAdapter = null;
        }
    }

}
