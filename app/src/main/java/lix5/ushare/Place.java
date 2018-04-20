package lix5.ushare;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Place {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;

    static Place getNearPlaceJsonToPlace(JSONObject object) {
        try {
            Place result = new Place();
            JSONObject geometry = (JSONObject) object.get("geometry");
            JSONObject location = (JSONObject) geometry.get("location");
            result.setId(object.getString("place_id"));
            result.setName(object.getString("name"));
            result.setLatitude((Double) location.get("lat"));
            result.setLongitude((Double) location.get("lng"));
            return result;
        } catch (JSONException ex) {
            Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
