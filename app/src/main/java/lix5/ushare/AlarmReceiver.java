package lix5.ushare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ByteArrayInputStream bis = new ByteArrayInputStream(intent.getByteArrayExtra("schedule"));
        ObjectInput in = null;
        Schedule loadSchedule = null;
        try {
            in = new ObjectInputStream(bis);
            loadSchedule = (Schedule) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        int id = intent.getIntExtra("scheduleID", 0);
        boolean isDay = intent.getBooleanExtra("isDaytime", false);
        Bundle bundle = new Bundle();
        bundle.putSerializable("schedule", loadSchedule);
        bundle.putBoolean("isDaytime", isDay);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent call = new Intent(context, CreateActivity.class).putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, call, PendingIntent.FLAG_UPDATE_CURRENT);

        int icon = R.drawable.schedule;
        String ticker = "UShare schedule";
        long when = System.currentTimeMillis();
        String title = "UShare";
        String desc = "Click here to create scheduled sharing event.";
        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(desc)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setWhen(when)
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_VIBRATE;
        notificationManager.notify(id, notification);
    }

}
