package lix5.ushare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mahfa.dnswitch.DayNightSwitch;
import com.touchboarder.weekdaysbuttons.WeekdaysDataItem;
import com.touchboarder.weekdaysbuttons.WeekdaysDataSource;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddScheduleActivity extends AppCompatActivity implements WeekdaysDataSource.Callback, FragmentScheduler.OnDataPass {
    private TimePicker daytimePicker, nighttimePicker;
    private CheckBox twice;
    private DayNightSwitch dayNightSwitch;
    private TextView repeatDay;
    private int selected = 0;
    private Button cancel, save;
    private List<Schedule> schedules;
    private String firstTime, secTime, selectedDays = "", firstLoc, firstDes, secLoc, secDes, firstLocID, firstDesID, secLocID, secDesID, type, seats, preference;
    private Schedule loadedSchedule;
    private boolean isTwice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_schedule);
        twice = (CheckBox) findViewById(R.id.twice);
        repeatDay = (TextView) findViewById(R.id.repeat_day);
        daytimePicker = (TimePicker) findViewById(R.id.daytime_picker);
        nighttimePicker = (TimePicker) findViewById(R.id.nighttime_picker);
        dayNightSwitch = (DayNightSwitch) findViewById(R.id.day_night_switch);
        cancel = (Button) findViewById(R.id.cancel);
        save = (Button) findViewById(R.id.save);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New schedule");
        readData();
        loadedSchedule = (Schedule) getIntent().getSerializableExtra("schedule");
        if (loadedSchedule != null)
            getSavedSchedule();
        getSupportFragmentManager().beginTransaction().replace(R.id.advanced, new FragmentScheduler()).commit();

        View parent = (View) findViewById(R.id.rl).getParent();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        WeekdaysDataSource wds = new WeekdaysDataSource(this, R.id.weekdays_buttons).setNumberOfLetters(3).setViewWidth(size.x / 8).setLocale(Locale.US);
        if (loadedSchedule != null) {
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(selectedDays.split(", ")));
            selected = list.size();
            setSelectedWeekdays(wds, list);
            if (!TextUtils.isEmpty(selectedDays)) {
                repeatDay.setText("Every " + selectedDays);
                if (selected == 7)
                    repeatDay.setText("Every day");
                if (selected == 0)
                    repeatDay.setText("No days selected");
            }
        }
        wds.start(this);

        daytimePicker.setIs24HourView(true);
        daytimePicker.setOnTimeChangedListener((TimePicker view, int hourOfDay, int minute) -> firstTime = new SimpleDateFormat("HH:mm").format(new Time(hourOfDay, minute, 0)));
        nighttimePicker.setIs24HourView(true);
        nighttimePicker.setOnTimeChangedListener((TimePicker view, int hourOfDay, int minute) -> secTime = new SimpleDateFormat("HH:mm").format(new Time(hourOfDay, minute, 0)));
        dayNightSwitch.setDuration(400);
        dayNightSwitch.setListener(isNight -> {
            if (isNight) {
                twice.setChecked(true);
                daytimePicker.setVisibility(View.INVISIBLE);
                nighttimePicker.setVisibility(View.VISIBLE);
            } else {
                daytimePicker.setVisibility(View.VISIBLE);
                nighttimePicker.setVisibility(View.INVISIBLE);
            }
        });
        twice.setOnClickListener(v -> {
            if (!twice.isChecked())
                dayNightSwitch.setIsNight(false, true);
            isTwice = twice.isChecked();
            getSharedPreferences("loadSchedule", MODE_PRIVATE).edit().putBoolean("isTwice",isTwice).apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.advanced, new FragmentScheduler()).commit();
        });
        cancel.setOnClickListener(v -> {
            startActivity(new Intent(AddScheduleActivity.this, SchedulerActivity.class));
            finish();
        });
        Time daytime = new Time(daytimePicker.getCurrentHour(), daytimePicker.getCurrentMinute(), 0);
        Time nighttime = new Time(nighttimePicker.getCurrentHour(), nighttimePicker.getCurrentMinute(), 0);
        firstTime = new SimpleDateFormat("HH:mm").format(daytime);
        secTime = new SimpleDateFormat("HH:mm").format(nighttime);
        save.setOnClickListener(v -> {
            if (selectedDays.equals("No days selected") || selectedDays.equals("")) {
                Toast.makeText(this, "Please select the repeat day", Toast.LENGTH_SHORT).show();
            } else {
                if (loadedSchedule != null) {
                    schedules.remove(getIntent().getIntExtra("scheduleID", 0));
                }
                if (twice.isChecked()) {
                    schedules.add(new Schedule(firstTime, secTime, true, selectedDays, firstLoc, firstDes, secLoc, secDes, firstLocID, firstDesID, secLocID, secDesID, type, seats, preference, true));
                    Log.i("IS Checked", "Day:" + firstTime + "Night:" + secTime + "week:" + selectedDays + "\n" + firstLoc + firstDes + secLoc + secDes + firstLocID + firstDesID + secLocID + secDesID + type + seats + preference);
                } else {
                    schedules.add(new Schedule(firstTime, "", false, selectedDays, firstLoc, firstDes, "", "", firstLocID, firstDesID, "", "", type, seats, preference, true));
                    Log.i("IS Not Checked", "Day:" + firstTime + "Night:" + secTime + "week:" + selectedDays + "\n" + firstLoc + firstDes + secLoc + secDes + firstLocID + firstDesID + secLocID + secDesID + type + seats + preference);
                }
                saveData(schedules);
                startActivity(new Intent(AddScheduleActivity.this, SchedulerActivity.class));
                finish();
            }
        });
    }

    private void setSelectedWeekdays(WeekdaysDataSource wds, ArrayList<String> list) {
        if(list.contains("Sunday"))
            wds.setSelectedDays(Calendar.SUNDAY);
        if(list.contains("Monday"))
            wds.setSelectedDays(Calendar.MONDAY);
        if(list.contains("Tuesday"))
            wds.setSelectedDays(Calendar.TUESDAY);
        if(list.contains("Wednesday"))
            wds.setSelectedDays(Calendar.WEDNESDAY);
        if(list.contains("Thursday"))
            wds.setSelectedDays(Calendar.THURSDAY);
        if(list.contains("Friday"))
            wds.setSelectedDays(Calendar.FRIDAY);
        if(list.contains("Saturday"))
            wds.setSelectedDays(Calendar.SATURDAY);
    }

    private void getSavedSchedule() {
        firstTime = loadedSchedule.getDaytime();
        daytimePicker.setCurrentHour(Integer.parseInt(firstTime.substring(0, 2)));
        daytimePicker.setCurrentMinute(Integer.parseInt(firstTime.substring(3, 5)));
        if (!loadedSchedule.getNighttime().equals("")) {
            secTime = loadedSchedule.getNighttime();
            nighttimePicker.setCurrentHour(Integer.parseInt(secTime.substring(0, 2)));
            nighttimePicker.setCurrentMinute(Integer.parseInt(secTime.substring(3, 5)));
        }
        twice.setChecked(loadedSchedule.getTwice());
        selectedDays = loadedSchedule.getWeekday();
        firstLoc = loadedSchedule.getFirstLoc();
        firstLocID = loadedSchedule.getFirstLocID();
        firstDes = loadedSchedule.getFirstDes();
        firstDesID = loadedSchedule.getFirstDesID();
        secLoc = loadedSchedule.getSecLoc();
        secLocID = loadedSchedule.getSecLocID();
        secDes = loadedSchedule.getSecDes();
        secDesID = loadedSchedule.getSecDesID();
        type = loadedSchedule.getType();
        seats = loadedSchedule.getSeats();
        preference = loadedSchedule.getPreference();
        getSharedPreferences("loadSchedule", MODE_PRIVATE).edit()
                .putString("week", selectedDays)
                .putString("fl", firstLoc)
                .putString("flid", firstLocID)
                .putString("fd", firstDes)
                .putString("fdid", firstDesID)
                .putString("sl", secLoc)
                .putString("slid", secLocID)
                .putString("sd", secDes)
                .putString("sdid", secDesID)
                .putString("type", type)
                .putString("seats", seats)
                .putString("pref", preference)
                .putBoolean("isTwice", twice.isChecked())
                .apply();
    }

    @Override
    public void onDataPass(details dataType, String data) {
        switch (dataType) {
            case FIRSTLOC:
                firstLoc = data;
                break;
            case FIRSTDES:
                firstDes = data;
                break;
            case SECLOC:
                secLoc = data;
                break;
            case SECDES:
                secDes = data;
                break;
            case FIRSTLOCID:
                firstLocID = data;
                break;
            case FIRSTDESID:
                firstDesID = data;
                break;
            case SECLOCID:
                secLocID = data;
                break;
            case SECDESID:
                secDesID = data;
                break;
            case TYPE:
                type = data;
                break;
            case SEATS:
                seats = data;
                break;
            case PREFERENCE:
                preference = data;
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(AddScheduleActivity.this, SchedulerActivity.class));
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddScheduleActivity.this, SchedulerActivity.class));
        finish();
    }

    @Override
    public void onWeekdaysItemClicked(int attachID, WeekdaysDataItem item) {
        // Do something if today is selected?
//        Calendar calendar = Calendar.getInstance();
//        int today = calendar.get(Calendar.DAY_OF_WEEK);
//        if (item.getCalendarDayId() == today && item.isSelected()) {
//            // today isSelected
//        }
    }

    @Override
    public void onWeekdaysSelected(int attachID, ArrayList<WeekdaysDataItem> items) {
        selectedDays = getSelectedDaysFromWeekdaysData(items);
        selected = new ArrayList<String>(Arrays.asList(selectedDays.split(", "))).size();

        if (!TextUtils.isEmpty(selectedDays)) {
            repeatDay.setText("Every " + selectedDays);
            if (selected == 7)
                repeatDay.setText("Every day");
            if (selectedDays.equals("No days selected"))
                repeatDay.setText(selectedDays);
        }
    }

    private String getSelectedDaysFromWeekdaysData(ArrayList<WeekdaysDataItem> items) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean selected = false;
        for (WeekdaysDataItem dataItem : items) {
            if (dataItem.isSelected()) {
                selected = true;
                stringBuilder.append(dataItem.getLabel());
                stringBuilder.append(", ");
            }
        }
        if (selected) {
            String result = stringBuilder.toString();
            return result.substring(0, result.lastIndexOf(","));
        } else return "No days selected";
    }

    public void saveData(List<Schedule> schedules) {
        SharedPreferences schedulePreferences = getSharedPreferences("schedulers", MODE_PRIVATE);
        SharedPreferences.Editor editor = schedulePreferences.edit();
        Gson gson = new Gson();
        String jsonSchedules = gson.toJson(schedules);
        editor.putString("Schedules", jsonSchedules).apply();
    }

    public void readData() {
        SharedPreferences scheduledPreferences = getSharedPreferences("schedulers", MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonSchedules = scheduledPreferences.getString("Schedules", "");
        Type type = new TypeToken<ArrayList<Schedule>>() {
        }.getType();
        if (!jsonSchedules.isEmpty())
            schedules = gson.fromJson(jsonSchedules, type);
        else
            schedules = new ArrayList<Schedule>();
    }

    public enum details {
        FIRSTLOC, FIRSTDES, SECLOC, SECDES, FIRSTLOCID, FIRSTDESID, SECLOCID, SECDESID, TYPE, SEATS, PREFERENCE
    }

}