package lix5.ushare;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class EventActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;

    private ViewPager mViewPager;
    private int[] iconID = {R.drawable.selector_info, R.drawable.selector_member, R.drawable.selector_chatroom};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);
        tabLayout = (TabLayout) findViewById(R.id.eventTabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        for(int i =0; i < iconID.length;i++){
            tabLayout.getTabAt(i).setIcon(iconID[i]);
        }

        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView_passenger);
        //TODO adapter


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.exit_event) {
            new AlertDialog.Builder(EventActivity.this)
                    .setMessage("Are you sure you want to exit this event?")
                    .setPositiveButton("YES", (dialog, which) -> exitEvent())
                    .setNegativeButton("NO", (dialog, which) ->{})
                    .show();
            return true;
        }else if(id == R.id.share){
            //TODO share event
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exitEvent() {
        startActivity(new Intent(EventActivity.this, MainActivity.class));
        //TODO exit event

    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_event_info, container, false);


            return rootView;
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFrag(new FragmentInfo());
        mSectionsPagerAdapter.addFrag(new FragmentMember());
        mSectionsPagerAdapter.addFrag(new FragmentChatroom());
        viewPager.setAdapter(mSectionsPagerAdapter);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        //private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
        }
    }



}
