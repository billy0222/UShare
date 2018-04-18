package lix5.ushare;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final int PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    static final int DROPOFF_PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    String date_time = "";
    int mYear, mMonth, mDay, mHour, mMinute;
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database
    private Geocoder geocoder;
    private List<Address> addresses;
    private Toolbar toolbar;
    private TabLayout tbl_pages;
    private ViewPager vp_pages;
    private ViewPagerAdapter adapter;
    private TextView searchPickup, searchDropoff, searchTime;
    private String TAG = "Search Autocomplete";
    private ImageView up_down_arrow, uIcon;
    private TextView uName, uEmail;
    private ImageView search_pick_up_cancel, search_drop_off_cancel, search_time_cancel;
    private String pickUpID = "", dropOffID = "";
    private Double pickUpLat = 0.0, pickUpLng = 0.0, dropOffLat = 0.0, dropOffLng = 0.0;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View HeaderView = navigationView.getHeaderView(0);      //initialize name, email in navigation bar
        uIcon = HeaderView.findViewById(R.id.uicon);
        uName = HeaderView.findViewById(R.id.uname);
        uEmail = HeaderView.findViewById(R.id.uemail);
        mDatabase.child("users/").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                uName.setText(dataSnapshot.child("username").getValue(String.class));   //set the name
                uEmail.setText(dataSnapshot.child("email").getValue(String.class));    //set the email
                    if(!dataSnapshot.child("avatar").getValue(String.class).equals(""))
                    Picasso.get().load(dataSnapshot.child("avatar").getValue(String.class)).into(uIcon);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.sendMessage);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreateActivity.class)));

        setupSearchBar();

        vp_pages = (ViewPager) findViewById(R.id.vp_pages);
        setupViewPager(vp_pages);
        tbl_pages = (TabLayout) findViewById(R.id.tabLayout);
        tbl_pages.setupWithViewPager(vp_pages);
        setupTabIcons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        // FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUI(currentUser);
    }

    private void datePicker() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) (DatePicker view, int year, int monthOfYear, int dayOfMonth) -> {
            Date date = new Date(year - 1900, monthOfYear, dayOfMonth);
            timePicker(date);
        }, mYear, mMonth, mDay);
        dpd.getDatePicker().setMinDate(c.getTimeInMillis());
        c.add(Calendar.YEAR, 1);
        long afterOneYearInMillis = c.getTimeInMillis();
        dpd.getDatePicker().setMaxDate(afterOneYearInMillis);
        dpd.show();

    }

    private void timePicker(Date date) {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 30); // Set the dialog init time 30 minutes after
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        c.add(Calendar.MINUTE, -30);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (TimePicker view, int hourOfDay, int minute) -> {
            mHour = hourOfDay;
            mMinute = minute;
            date.setHours(hourOfDay);
            date.setMinutes(minute);
            if (date.before(c.getTime()))
                new AlertDialog.Builder(MainActivity.this).setMessage("The pick up time can only be future.").setPositiveButton("OK", (dialog, which) -> datePicker()).show();
            else
                date_time = (date.getYear() + 1900 == c.get(Calendar.YEAR) ? new SimpleDateFormat("EE, dd MMMM, HH:mm", Locale.US).format(date) : new SimpleDateFormat("EE, dd MMMM yyyy, HH:mm", Locale.US).format(date));
            searchTime.setText(date_time);

        }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    CharSequence result = null;
                    Bundle results = data.getExtras();
                    if (results != null) {
                        result = results.getCharSequence("result");
                        pickUpID = results.getString("placeID");
                        pickUpLat = results.getDouble("latitude");
                        pickUpLng = results.getDouble("longitude");
                    }
                    searchPickup.setText(result);
                    Log.i(TAG, "Place: " + result);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.i(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            case DROPOFF_PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    CharSequence result = null;
                    Bundle results = data.getExtras();
                    if (results != null) {
                        result = results.getCharSequence("result");
                        dropOffID = results.getString("placeID");
                        dropOffLat = results.getDouble("latitude");
                        dropOffLng = results.getDouble("longitude");
                    }
                    searchDropoff.setText(result);
                    Log.i(TAG, "Place: " + result);
                    //filtering the event
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.i(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FragmentTaxi(), "Taxi");
        adapter.addFrag(new FragmentCar(), "Private car");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText(getResources().getText(R.string.taxi));
        tabOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.taxi_sign_icon, 0, 0, 0);
        tbl_pages.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText(getResources().getText(R.string.private_car));
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_directions_car_black_48dp, 0, 0, 0);
        tbl_pages.getTabAt(1).setCustomView(tabTwo);
    }

    private void setupSearchBar() {
        // Setup autocomplete
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("HK").build();
        searchPickup = (TextView) findViewById(R.id.search_pick_up);
        searchPickup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AutocompleteActivity.class);
            startActivityForResult(intent, PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE);
        });
        searchDropoff = (TextView) findViewById(R.id.search_drop_off);
        searchDropoff.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AutocompleteActivity.class);
            startActivityForResult(intent, DROPOFF_PLACE_AUTOCOMPLETE_REQUEST_CODE);
        });
        up_down_arrow = (ImageView) findViewById(R.id.up_down_arrow);
        up_down_arrow.setOnClickListener(v -> {
            String temp = pickUpID;
            pickUpID = dropOffID;
            dropOffID = temp;
            CharSequence dummy = searchPickup.getText();
            searchPickup.setText(searchDropoff.getText());
            searchDropoff.setText(dummy);
        });
        searchTime = (TextView) findViewById(R.id.search_time);
        searchTime.setOnClickListener(v -> datePicker());

        //set up cancel icon
        search_pick_up_cancel = findViewById(R.id.search_pick_up_cancel);
        search_drop_off_cancel = findViewById(R.id.search_drop_off_cancel);
        search_time_cancel = findViewById(R.id.search_time_cancel);
        // Set icons
        searchPickup.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Drawable img = MainActivity.this.getResources().getDrawable(
                                R.drawable.current_location);
                        Drawable cancel = MainActivity.this.getResources().getDrawable(R.drawable.cross);
                        img.setBounds(0, 0, img.getIntrinsicWidth() * searchPickup.getMeasuredHeight() / img.getIntrinsicHeight(), searchPickup.getMeasuredHeight());
                        searchPickup.setCompoundDrawables(img, null, null, null);
                        searchPickup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        searchDropoff.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Drawable img = MainActivity.this.getResources().getDrawable(
                                R.drawable.destination);
                        img.setBounds(0, 0, img.getIntrinsicWidth() * searchDropoff.getMeasuredHeight() / img.getIntrinsicHeight(), searchDropoff.getMeasuredHeight());
                        searchDropoff.setCompoundDrawables(img, null, null, null);
                        searchDropoff.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        searchTime.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Drawable img = MainActivity.this.getResources().getDrawable(
                                R.drawable.clock);
                        img.setBounds(0, 0, img.getIntrinsicWidth() * searchTime.getMeasuredHeight() / img.getIntrinsicHeight(), searchTime.getMeasuredHeight());
                        searchTime.setCompoundDrawables(img, null, null, null);
                        searchTime.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        search_pick_up_cancel.setOnClickListener(view -> {
            pickUpID = "";
            searchPickup.setText("");
        });
        search_drop_off_cancel.setOnClickListener(view -> {
            dropOffID = "";
            searchDropoff.setText("");
        });
        search_time_cancel.setOnClickListener(view -> searchTime.setText(""));

        //filtering
        searchPickup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                FragmentCar car = (FragmentCar) adapter.getItem(1);
                FragmentTaxi taxi = (FragmentTaxi) adapter.getItem(0);
                car.filter(pickUpID, dropOffID, searchTime.getText(), pickUpLat, pickUpLng, dropOffLat, dropOffLng);
                taxi.filter(pickUpID, dropOffID, searchTime.getText(), pickUpLat, pickUpLng, dropOffLat, dropOffLng);
            }
        });

        searchDropoff.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                FragmentCar car = (FragmentCar) adapter.getItem(1);
                FragmentTaxi taxi = (FragmentTaxi) adapter.getItem(0);
                car.filter(pickUpID, dropOffID, searchTime.getText(), pickUpLat, pickUpLng, dropOffLat, dropOffLng);
                taxi.filter(pickUpID, dropOffID, searchTime.getText(), pickUpLat, pickUpLng, dropOffLat, dropOffLng);
            }
        });

        searchTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                FragmentCar car = (FragmentCar) adapter.getItem(1);
                FragmentTaxi taxi = (FragmentTaxi) adapter.getItem(0);
                car.filter(pickUpID, dropOffID, searchTime.getText(), pickUpLat, pickUpLng, dropOffLat, dropOffLng);
                taxi.filter(pickUpID, dropOffID, searchTime.getText(), pickUpLat, pickUpLng, dropOffLat, dropOffLng);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar taxi_item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_event){
            startActivity(new Intent(MainActivity.this, MyEventActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view taxi_item clicks here.
        switch (item.getItemId()){
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_follow:
                break;
            case R.id.liveView:
                startActivity(new Intent(MainActivity.this, LiveView.class));
                break;
            case R.id.nav_scheduler:
                startActivity(new Intent(MainActivity.this, SchedulerActivity.class));
                break;
            case R.id.nav_trip:
                startActivity(new Intent(MainActivity.this, MyEventActivity.class));
                break;
            case R.id.nav_logout:
                logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void logout() {
        SharedPreferences mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean("is_logged_in", false);
        editor.apply();
        startActivity(new Intent(MainActivity.this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
