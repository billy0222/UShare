package lix5.ushare;

import java.io.Serializable;
import java.util.ArrayList;

public class Schedule implements Serializable {
    private String daytime, nighttime, weekday, firstLoc, firstDes, secLoc, secDes, firstLocID, firstDesID, secLocID, secDesID, type, seats, preference;
    private boolean twice, isOn;

    public Schedule() {
    }

    public Schedule(String daytime, String nighttime, boolean twice, String weekday, String firstLoc, String firstDes, String secLoc, String secDes, String firstLocID, String firstDesID, String secLocID, String secDesID, String type, String seats, String preference, boolean isOn) {
        this.daytime = daytime;
        this.nighttime = nighttime;
        this.twice = twice;
        this.weekday = weekday;
        this.firstLoc = firstLoc;
        this.firstDes = firstDes;
        this.secLoc = secLoc;
        this.secDes = secDes;
        this.firstLocID = firstLocID;
        this.firstDesID = firstDesID;
        this.secLocID = secLocID;
        this.secDesID = secDesID;
        this.type = type;
        this.seats = seats;
        this.preference = preference;
        this.isOn = isOn;
    }

    public boolean getOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public String getDaytime() {
        return daytime;
    }

    public String getNighttime() {
        return nighttime;
    }

    public boolean getTwice() {
        return twice;
    }

    public String getWeekday() {
        return weekday;
    }

    public String getFirstLoc() {
        return firstLoc;
    }

    public String getFirstDes() {
        return firstDes;
    }

    public String getSecLoc() {
        return secLoc;
    }

    public String getSecDes() {
        return secDes;
    }

    public String getFirstLocID() {
        return firstLocID;
    }

    public String getFirstDesID() {
        return firstDesID;
    }

    public String getSecLocID() {
        return secLocID;
    }

    public String getSecDesID() {
        return secDesID;
    }

    public String getType() {
        return type;
    }

    public String getSeats() {
        return seats;
    }

    public String getPreference() {
        return preference;
    }

    public ArrayList<Integer> getWeekdaysArray() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (weekday.contains("Sunday"))
            result.add(1);
        if (weekday.contains("Monday"))
            result.add(2);
        if (weekday.contains("Tuesday"))
            result.add(3);
        if (weekday.contains("Wednesday"))
            result.add(4);
        if (weekday.contains("Thursday"))
            result.add(5);
        if (weekday.contains("Friday"))
            result.add(6);
        if (weekday.contains("Saturday"))
            result.add(7);
        return result;
    }
}
