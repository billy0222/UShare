package lix5.ushare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaceDistance {
    private int distance;
    private int duration;

    static PlaceDistance getDistanceJsonToPlace(JSONObject object) {
        try {
            PlaceDistance result = new PlaceDistance();
            JSONArray elements = object.getJSONArray("elements");
            JSONObject temp = elements.getJSONObject(0);
            JSONObject distance = (JSONObject) temp.get("distance");
            JSONObject duration = (JSONObject) temp.get("duration");
            result.setDistance((int) distance.get("value"));
            result.setDuration((int) duration.get("value"));
            return result;
        } catch (JSONException ex) {
            Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
