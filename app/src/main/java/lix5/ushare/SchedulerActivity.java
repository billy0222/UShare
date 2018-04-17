package lix5.ushare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchedulerActivity extends AppCompatActivity {
    RecyclerView mList;
    private List<Schedule> schedules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedular);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        schedules = readData();

        MyAdapter myAdapter = new MyAdapter(schedules);
        mList = (RecyclerView) findViewById(R.id.rv_schedule);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mList.setLayoutManager(layoutManager);
        mList.setAdapter(myAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_schedule);
        fab.setOnClickListener(v -> {
            getSharedPreferences("loadSchedule", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(SchedulerActivity.this, AddScheduleActivity.class));
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void saveData(List<Schedule> schedules) {
        SharedPreferences schedulePreferences = getSharedPreferences("schedulers", MODE_PRIVATE);
        SharedPreferences.Editor editor = schedulePreferences.edit();
        Gson gson = new Gson();
        String jsonSchedules = gson.toJson(schedules);
        editor.putString("Schedules", jsonSchedules).apply();
    }

    public List<Schedule> readData() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        SharedPreferences scheduledPreferences = getSharedPreferences("schedulers", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = scheduledPreferences.getString("Schedules", "");
        if (json.isEmpty()) {
            schedules = new ArrayList<Schedule>();
        } else {
            Type type = new TypeToken<ArrayList<Schedule>>() {
            }.getType();
            schedules = gson.fromJson(json, type);
        }
        Log.i("LOAD", json);
        return schedules;
    }

//    private void setTextViewDrawableColor(TextView textView, int color) {
//        for (Drawable drawable : textView.getCompoundDrawables()) {
//            if (drawable != null)
//                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//        }
//    }

    private void setupByState(MyAdapter.ViewHolder holder, String color) {
        holder.daytime.setTextColor(Color.parseColor(color));
        holder.nighttime.setTextColor(Color.parseColor(color));
//        setTextViewDrawableColor(holder.daytime, Color.parseColor(color));
//        setTextViewDrawableColor(holder.nighttime, Color.parseColor(color));
        holder.sun.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
        holder.moon.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
        holder.weekday.setTextColor(Color.parseColor(color));
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<Schedule> mData;

        public MyAdapter(List<Schedule> data) {
            mData = data;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.daytime.setText(mData.get(position).getDaytime());
            holder.nighttime.setText(mData.get(position).getNighttime());
            if (mData.get(position).getNighttime().equals("")) {
                holder.nighttime.setVisibility(View.GONE);
                holder.moon.setVisibility(View.GONE);
            }
            String repeatday;
            if (new ArrayList<String>(Arrays.asList(mData.get(position).getWeekday().split(", "))).size() == 7)
                repeatday = "Every day";
            else
                repeatday = "Every " + mData.get(position).getWeekday();
            holder.weekday.setText(repeatday);
            holder.state.setChecked(mData.get(position).getOn());
            holder.state.setOnClickListener(v -> {
                if (holder.state.isChecked()) {
                    schedules.get(position).setOn(true);
                    setupByState(holder, "#000000");
                } else {
                    schedules.get(position).setOn(false);
                    setupByState(holder, "#DDDDDD");
                }
                saveData(schedules);
            });
            if (mData.get(position).getOn())
                setupByState(holder, "#000000");
            else
                setupByState(holder, "#DDDDDD");
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView daytime, nighttime, weekday;
            ImageView sun, moon;
            Switch state;

            public ViewHolder(View v) {
                super(v);
                daytime = v.findViewById(R.id.schedule_daytime);
                nighttime = v.findViewById(R.id.schedule_nighttime);
                weekday = v.findViewById(R.id.schedule_weekday);
                state = v.findViewById(R.id.schedule_switch);
                sun = v.findViewById(R.id.sun);
                moon = v.findViewById(R.id.moon);

                v.setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("schedule", mData.get(getAdapterPosition()));
                    bundle.putInt("scheduleID", getAdapterPosition());
                    startActivity(new Intent(SchedulerActivity.this, AddScheduleActivity.class).putExtras(bundle));
                    finish();
                });

                v.setOnLongClickListener(view -> {
                    int position = getAdapterPosition();
                    PopupMenu popup = new PopupMenu(SchedulerActivity.this, view);
                    popup.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case R.id.delete:
                                mData.remove(position);
                                saveData(schedules);
                                notifyItemRemoved(position);
                                return true;
                            default:
                                return false;
                        }
                    });
                    popup.inflate(R.menu.delete_schedule_menu);
                    popup.show();
                    return true;
                });
            }
        }
    }
}
