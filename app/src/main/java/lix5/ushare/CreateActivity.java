package lix5.ushare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static lix5.ushare.MainActivity.DROPOFF_PLACE_AUTOCOMPLETE_REQUEST_CODE;
import static lix5.ushare.MainActivity.PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE;

public class CreateActivity extends AppCompatActivity {
    String date_time = "";
    int mYear, mMonth, mDay, mHour, mMinute;
    Boolean typeIsCar, typeIsTaxi, boysOnly = false, girlsOnly = false, eventIsRequest = false;
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database
    private EditText seats, remarks_input;
    private ImageView add, remove;
    private ImageButton taxiButton, carButton;
    private CheckBox boys, girls, isRequest;
    private TextView createPickup, createDropoff, createTime, errorMessage;
    private String TAG = "Create Autocomplete";
    private Button create;
    private String pickUpID, dropOffID;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        seats = findViewById(R.id.number_of_seats);
        remarks_input = findViewById(R.id.remarks_input);
        add = findViewById(R.id.add_seat);
        remove = findViewById(R.id.remove_seat);
        taxiButton = findViewById(R.id.taxi_button);
        carButton = findViewById(R.id.car_button);
        boys = findViewById(R.id.boys);
        girls = findViewById(R.id.girls);
        isRequest = findViewById(R.id.isRequest);
        create = findViewById(R.id.create);
        errorMessage = findViewById(R.id.errorMessage_createEvent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupRouteInputBar();

        Schedule loadedSchedule = (Schedule) getIntent().getSerializableExtra("schedule");
        boolean isDay = getIntent().getBooleanExtra("isDaytime", false);
        if (loadedSchedule != null) {
            if (isDay) {
                createPickup.setText(loadedSchedule.getFirstLoc());
                createDropoff.setText(loadedSchedule.getFirstDes());
                pickUpID = loadedSchedule.getFirstLocID();
                dropOffID = loadedSchedule.getFirstDesID();
                //            createTime.setText();
            } else {
                createPickup.setText(loadedSchedule.getSecLoc());
                createDropoff.setText(loadedSchedule.getSecDes());
                pickUpID = loadedSchedule.getSecLocID();
                dropOffID = loadedSchedule.getSecDesID();
            }
            if (loadedSchedule.getType() != null) {
                if (loadedSchedule.getType().equals("Taxi")) {
                    taxiButton.setImageDrawable(getResources().getDrawable(R.drawable.taxi_sign_icon));
                    carButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_car_gray_48dp));
                    taxiButton.setSelected(true);
                    isRequest.setChecked(true);
                    isRequest.setClickable(false);
                    eventIsRequest = true;
                    typeIsTaxi = true;
                    typeIsCar = false;
                }
                if (loadedSchedule.getType().equals("Private car")) {
                    carButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_car_black_48dp));
                    taxiButton.setImageDrawable(getResources().getDrawable(R.drawable.taxi_sign_gray));
                    carButton.setSelected(true);
                    typeIsTaxi = false;
                    typeIsCar = true;
                }
            }
            if (loadedSchedule.getPreference() != null) {
                if (loadedSchedule.getPreference().equals("Men only")) {
                    boys.setChecked(true);
                    boysOnly = true;
                }
                if (loadedSchedule.getPreference().equals("Women only")) {
                    girls.setChecked(true);
                    girlsOnly = true;
                }
            }
            if (loadedSchedule.getSeats() != null && !loadedSchedule.getSeats().equals("")) {
                seats.setText(loadedSchedule.getSeats());
                if (loadedSchedule.getSeats().equals("10"))
                    add.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_circle_outline_gray_18dp));
                if (loadedSchedule.getSeats().equals("1"))
                    remove.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_circle_outline_gray_18dp));
                if (!loadedSchedule.getSeats().equals("1"))
                    remove.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_circle_outline_black_18dp));
            }
        }

        seats.setFilters(new InputFilter[]{new InputFilterMinMax("1", "10")});
        add.setOnClickListener(v -> {
            if (!String.valueOf(seats.getText()).equals("10")) {
                seats.setText(String.valueOf(Integer.parseInt(seats.getText().toString()) + 1));
                remove.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_circle_outline_black_18dp));
                if (String.valueOf(seats.getText()).equals("10"))
                    add.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_circle_outline_gray_18dp));
            }
        });
        remove.setOnClickListener(v -> {
            if (!String.valueOf(seats.getText()).equals("1")) {
                seats.setText(String.valueOf(Integer.parseInt(seats.getText().toString()) - 1));
                add.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_circle_outline_black_18dp));
                if (String.valueOf(seats.getText()).equals("1"))
                    remove.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_circle_outline_gray_18dp));
            }
        });

        create.setOnClickListener(v -> {        // sync with database
            if (!validEventCheck()) {
                mDatabase.child("users/").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Event event = new Event(mAuth.getUid(), dataSnapshot.child("username").getValue().toString(), createPickup.getText().toString(), createDropoff.getText().toString(), createTime.getText().toString(),
                                seats.getText().toString(), typeToString(typeIsTaxi, typeIsCar), boysOnly.toString(), girlsOnly.toString(),
                                remarks_input.getText().toString(), eventIsRequest.toString(), pickUpID, dropOffID);
                        mDatabase.child("events").push().setValue(event, (databaseError, databaseReference) -> {
                            String key = databaseReference.getKey();
                            FirebaseMessaging.getInstance().subscribeToTopic(key);      // subscribed to event
                        });
                        Toast.makeText(getApplicationContext(), "Your event has been created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreateActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "DB Error: Event creation failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupRouteInputBar() {
        // Setup autocomplete
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("HK").build();
        createPickup = findViewById(R.id.create_pick_up);
        createPickup.setOnClickListener(v -> {
            Intent intent = new Intent(CreateActivity.this, AutocompleteActivity.class);
            startActivityForResult(intent, PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE);
        });
        createDropoff = findViewById(R.id.create_drop_off);
        createDropoff.setOnClickListener(v -> {
            Intent intent = new Intent(CreateActivity.this, AutocompleteActivity.class);
            startActivityForResult(intent, DROPOFF_PLACE_AUTOCOMPLETE_REQUEST_CODE);
        });
        createTime = findViewById(R.id.create_time);
        createTime.setOnClickListener(v -> datePicker());

        // Set icons
        createPickup.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Drawable img = CreateActivity.this.getResources().getDrawable(
                                R.drawable.current_location);
                        img.setBounds(0, 0, img.getIntrinsicWidth() * createPickup.getMeasuredHeight() / img.getIntrinsicHeight(), createPickup.getMeasuredHeight());
                        createPickup.setCompoundDrawables(img, null, null, null);
                        createPickup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
        createDropoff.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Drawable img = CreateActivity.this.getResources().getDrawable(
                                R.drawable.destination);
                        img.setBounds(0, 0, img.getIntrinsicWidth() * createDropoff.getMeasuredHeight() / img.getIntrinsicHeight(), createDropoff.getMeasuredHeight());
                        createDropoff.setCompoundDrawables(img, null, null, null);
                        createDropoff.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
        createTime.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Drawable img = CreateActivity.this.getResources().getDrawable(
                                R.drawable.clock);
                        img.setBounds(0, 0, img.getIntrinsicWidth() * createTime.getMeasuredHeight() / img.getIntrinsicHeight(), createTime.getMeasuredHeight());
                        createTime.setCompoundDrawables(img, null, null, null);
                        createTime.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }

    public void onTypeSelected(View view) {
        if (view.getId() == R.id.taxi_button) {
            taxiButton.setImageDrawable(getResources().getDrawable(R.drawable.taxi_sign_icon));
            carButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_car_gray_48dp));
            taxiButton.setSelected(true);
            isRequest.setChecked(true);
            isRequest.setClickable(false);
            eventIsRequest = true;
            typeIsTaxi = true;
            typeIsCar = false;
        }
        if (view.getId() == R.id.car_button) {
            carButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_car_black_48dp));
            taxiButton.setImageDrawable(getResources().getDrawable(R.drawable.taxi_sign_gray));
            carButton.setSelected(true);
            //TODO if no car plate -> isRequest.setChecked(true);
            if (!isRequest.isClickable()) {
                isRequest.setClickable(true);
            }
            typeIsTaxi = false;
            typeIsCar = true;
        }
    }

    private void datePicker() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (DatePicker view, int year, int monthOfYear, int dayOfMonth) -> {
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
                new AlertDialog.Builder(CreateActivity.this).setMessage("The pick up time can only be future.").setPositiveButton("OK", (dialog, which) -> datePicker()).show();
            else
                date_time = (date.getYear() + 1900 == c.get(Calendar.YEAR) ? new SimpleDateFormat("EE, dd MMMM, HH:mm", Locale.US).format(date) : new SimpleDateFormat("EE, dd MMMM yyyy, HH:mm", Locale.US).format(date));
            createTime.setText(date_time);

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
                    }
                    createPickup.setText(result);
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
                    }
                    createDropoff.setText(result);
                    Log.i(TAG, "Place: " + result);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.i(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
        }
    }

    public void onCheckboxClicked(View view) {
        if (view.getId() == R.id.boys) {
            girls.setChecked(false);
            boysOnly = true;
            girlsOnly = false;
        }
        if (view.getId() == R.id.girls) {
            boys.setChecked(false);
            boysOnly = false;
            girlsOnly = true;
        }
    }

    public void onCheckboxClickedRequest(View view) {
        if (view.getId() == R.id.isRequest) {
            eventIsRequest = isRequest.isChecked();
        }
    }

    public String typeToString(Boolean taxi, Boolean car) {
        if (taxi && !car) {
            return "Taxi";
        } else if (car && !taxi) {
            return "Car";
        }
        return "No such type";
    }

    public boolean validEventCheck() {
        Boolean haveError = false;
        if (TextUtils.isEmpty(createPickup.getText().toString())) {
            createPickup.setError("Please enter pick up station");
            createPickup.requestFocus();
            haveError = true;
        }
        if (TextUtils.isEmpty(createDropoff.getText().toString())) {
            createDropoff.setError("Please enter drop off station");
            createDropoff.requestFocus();
            haveError = true;
        }
        if (TextUtils.isEmpty(createTime.getText().toString())) {
            createTime.setError("Please enter depart time");
            createTime.requestFocus();
            haveError = true;
        }
        if (!taxiButton.isSelected() && !carButton.isSelected()) {
            errorMessage.setVisibility(View.VISIBLE);
            haveError = true;
        } else {
            errorMessage.setVisibility(View.GONE);
        }
        return haveError;
    }

    public class InputFilterMinMax implements InputFilter {
        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
}