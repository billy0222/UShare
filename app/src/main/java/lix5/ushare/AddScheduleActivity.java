package lix5.ushare;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mahfa.dnswitch.DayNightSwitch;
import com.touchboarder.weekdaysbuttons.WeekdaysDataItem;
import com.touchboarder.weekdaysbuttons.WeekdaysDataSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddScheduleActivity extends AppCompatActivity implements WeekdaysDataSource.Callback {
    private TimePicker daytimePicker, nighttimePicker;
    private CheckBox twice;
    private DayNightSwitch dayNightSwitch;
    private TextView repeatDay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_schedule);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New schedule");

        repeatDay = (TextView) findViewById(R.id.repeat_day);
        daytimePicker = (TimePicker) findViewById(R.id.daytime_picker);
        nighttimePicker = (TimePicker) findViewById(R.id.nighttime_picker);
        dayNightSwitch = (DayNightSwitch) findViewById(R.id.day_night_switch);

        View parent = (View) findViewById(R.id.rl).getParent();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        WeekdaysDataSource wds = new WeekdaysDataSource(this, R.id.weekdays_buttons).setNumberOfLetters(3).setViewWidth(size.x / 8).setLocale(Locale.US).start(this);
        twice = (CheckBox) findViewById(R.id.twice);
        daytimePicker.setIs24HourView(true);
        nighttimePicker.setIs24HourView(true);
        dayNightSwitch.setDuration(450);
        dayNightSwitch.setListener(isNight -> {
            twice.setChecked(true);
            if (isNight) {
                daytimePicker.setVisibility(View.INVISIBLE);
                nighttimePicker.setVisibility(View.VISIBLE);
            } else {
                daytimePicker.setVisibility(View.VISIBLE);
                nighttimePicker.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void onWeekdaysItemClicked(int attachID, WeekdaysDataItem item) {
        // Do something if today is selected?
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        if (item.getCalendarDayId() == today && item.isSelected()) {
            //TODO today isSelected
        }
    }

    @Override
    public void onWeekdaysSelected(int attachID, ArrayList<WeekdaysDataItem> items) {
        String selectedDays = getSelectedDaysFromWeekdaysData(items);
        int selected = 0;
        if (!TextUtils.isEmpty(selectedDays))
            repeatDay.setText("Every " + selectedDays);
        for (WeekdaysDataItem dataItem : items) {
            if (dataItem.isSelected())
                selected++;
        }
        if (!TextUtils.isEmpty(selectedDays) && selected == 7)
            repeatDay.setText("Every day");
        if (!TextUtils.isEmpty(selectedDays) && selected == 0)
            repeatDay.setText(selectedDays);

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

}