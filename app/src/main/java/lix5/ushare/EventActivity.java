package lix5.ushare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;

    private ViewPager mViewPager;
    private int[] iconID = {R.drawable.selector_info, R.drawable.selector_member, R.drawable.selector_chatroom};

    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);
        tabLayout = (TabLayout) findViewById(R.id.eventTabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < iconID.length; i++) {
            tabLayout.getTabAt(i).setIcon(iconID[i]);
        }
        if(getIntent().getBooleanExtra("event_is_history", false)){
            invalidateOptionsMenu();
        }
        //TODO adapter
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if(getIntent().getBooleanExtra("event_is_history", false)) {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.exit_event) {
            Event event = (Event) getIntent().getSerializableExtra("event");
            String event_key = (String) getIntent().getStringExtra("event_key");
            if (event.getHostID().equals(mAuth.getUid())) {       // You are the host and you want to disband the event
                new AlertDialog.Builder(EventActivity.this)
                        .setMessage("Are you sure you want to disband this sharing event?")
                        .setPositiveButton("YES", (dialog, which) -> disbandEvent(event_key))
                        .setNegativeButton("NO", (dialog, which) -> {
                        })
                        .show();
            } else if (event.getPassengers().contains(mAuth.getUid())) {        // You are the passenger and you want to quit the event
                new AlertDialog.Builder(EventActivity.this)
                        .setMessage("Are you sure you want to quit this sharing event?")
                        .setPositiveButton("YES", (dialog, which) -> quitEvent(event, event_key))
                        .setNegativeButton("NO", (dialog, which) -> {
                        })
                        .show();
            } else {       // You are not the host or the passenger
                new AlertDialog.Builder(EventActivity.this).setMessage("You are not the host or the passenger!").show();
            }
            return true;
        } else if (id == R.id.join_event) {
            Event event = (Event) getIntent().getSerializableExtra("event");
            String event_key = (String) getIntent().getStringExtra("event_key");
            if (event.getHostID().equals(mAuth.getUid())) {        // You are the host! Why do you have to join the event again?
                new AlertDialog.Builder(EventActivity.this).setMessage("You are the host!").show();
            } else {
                mDatabase.child("events").child(event_key).child("passengers").orderByValue().equalTo(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {      // have already joined the event
                            new AlertDialog.Builder(EventActivity.this).setMessage("You have already joined this event!").show();
                        } else {       // have not joined the event yet
                            if (Integer.parseInt(event.getNumOfSeat()) > 0) {        // have vacancy
                                event.getPassengers().add(mAuth.getUid());
                                int remaining_seat = Integer.parseInt(event.getNumOfSeat()) - 1;
                                event.setNumOfSeat(String.valueOf(remaining_seat));
                                Map<String, Object> postValues = event.toMapEvent();
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put(event_key, postValues);
                                mDatabase.child("events").updateChildren(childUpdates);
                                FirebaseMessaging.getInstance().subscribeToTopic(event_key);        // subscribed to event
                                new AlertDialog.Builder(EventActivity.this).setMessage("Join event success").show();
                            } else {
                                new AlertDialog.Builder(EventActivity.this).setMessage("Sorry, the event is currently full!").show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void disbandEvent(String event_key) {
        mDatabase.child("events/").child(event_key).removeValue();
        FirebaseMessaging.getInstance().unsubscribeFromTopic(event_key);      // unsubscribed from event
        Toast.makeText(getApplicationContext(), "Your event has been disbanded", Toast.LENGTH_SHORT).show();
        finish();
    }

    @SuppressLint("RestrictedApi")
    private void quitEvent(Event event, String event_key) {
        event.getPassengers().remove(mAuth.getUid());
        int remaining_seat = Integer.parseInt(event.getNumOfSeat()) + 1;
        event.setNumOfSeat(String.valueOf(remaining_seat));
        Map<String, Object> postValues = event.toMapEvent();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(event_key, postValues);
        mDatabase.child("events").updateChildren(childUpdates);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(event_key);        // unsubscribed from event
        new AlertDialog.Builder(EventActivity.this).setMessage("You have quited the event").show();
    }

    private void setupViewPager(ViewPager viewPager) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFrag(new FragmentInfo());
        mSectionsPagerAdapter.addFrag(new FragmentMember());
        mSectionsPagerAdapter.addFrag(new FragmentChatroom());
        viewPager.setAdapter(mSectionsPagerAdapter);
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }


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
