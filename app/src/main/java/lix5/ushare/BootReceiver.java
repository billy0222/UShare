package lix5.ushare;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class BootReceiver extends BroadcastReceiver {
    private List<Schedule> schedules;
    private Calendar alarmCalendar = Calendar.getInstance();
    private AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent thisIntent) {
        //schedules = readData();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        schedules = new ArrayList<Schedule>();
        SharedPreferences scheduledPreferences = context.getSharedPreferences("schedulers", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = scheduledPreferences.getString("Schedules", "");
        if (json.isEmpty()) {
            schedules = new ArrayList<Schedule>();
        } else {
            Type type = new TypeToken<ArrayList<Schedule>>() {
            }.getType();
            schedules = gson.fromJson(json, type);
        }
        for (int i = 0; i < schedules.size(); i++) {
            ArrayList<Integer> weekdays = schedules.get(i).getWeekdaysArray();
            if (schedules.get(i).getOn()) {
                for (int j = 0; j < weekdays.size(); j++) {
                    alarmCalendar.setTimeInMillis(System.currentTimeMillis());
                    alarmCalendar.set(Calendar.DAY_OF_WEEK, weekdays.get(j));
                    alarmCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(schedules.get(i).getDaytime().substring(0, 2)));
                    alarmCalendar.set(Calendar.MINUTE, Integer.parseInt(schedules.get(i).getDaytime().substring(3, 5)));
                    alarmCalendar.set(Calendar.SECOND, 0);
                    alarmCalendar.set(Calendar.MILLISECOND, 0);
                    if (alarmCalendar.getTimeInMillis() < System.currentTimeMillis())
                        alarmCalendar.add(Calendar.DAY_OF_YEAR, 7);

                    Bundle bundle = new Bundle();
                    bundle.putInt("scheduleID", i);
                    bundle.putBoolean("isDaytime", true);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream out = null;
                    try {
                        out = new ObjectOutputStream(bos);
                        out.writeObject(schedules.get(i));
                        out.flush();
                        byte[] data = bos.toByteArray();
                        bundle.putByteArray("schedule", data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            bos.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    Intent intent = new Intent(context, AlarmReceiver.class).putExtras(bundle).setAction(Long.toString(System.currentTimeMillis())).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                }
            }
            if (!schedules.get(i).getOn()) {
                Intent intent = new Intent(context, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(pendingIntent);
            }
        }
        int k = schedules.size();
        for (int i = 0; i < schedules.size(); i++) {
            if (schedules.get(i).getTwice())
                k++;
        }
        for (int x = 0; x < schedules.size(); x++) {
            for (int i = schedules.size(); i < k; i++) {
                if (schedules.get(x).getTwice() && schedules.get(x).getOn()) {
                    ArrayList<Integer> weekdays = schedules.get(x).getWeekdaysArray();
                    for (int j = 0; j < weekdays.size(); j++) {
                        alarmCalendar.setTimeInMillis(System.currentTimeMillis());
                        alarmCalendar.set(Calendar.DAY_OF_WEEK, weekdays.get(j));
                        alarmCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(schedules.get(x).getNighttime().substring(0, 2)));
                        alarmCalendar.set(Calendar.MINUTE, Integer.parseInt(schedules.get(x).getNighttime().substring(3, 5)));
                        alarmCalendar.set(Calendar.SECOND, 0);
                        alarmCalendar.set(Calendar.MILLISECOND, 0);
                        if (alarmCalendar.getTimeInMillis() < System.currentTimeMillis())
                            alarmCalendar.add(Calendar.DAY_OF_YEAR, 7);

                        Bundle bundle = new Bundle();
                        bundle.putInt("scheduleID", i);
                        bundle.putBoolean("isDaytime", false);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream out = null;
                        try {
                            out = new ObjectOutputStream(bos);
                            out.writeObject(schedules.get(x));
                            out.flush();
                            byte[] data = bos.toByteArray();
                            bundle.putByteArray("schedule", data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                bos.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        Intent intent = new Intent(context, AlarmReceiver.class).putExtras(bundle).setAction(Long.toString(System.currentTimeMillis())).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                    }
                }
                if (!schedules.get(x).getOn()) {
                    Intent intent = new Intent(context, AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, x, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.cancel(pendingIntent);
                }
            }
        }
    }
}
