package lix5.ushare;

/**
 * Created by Kevin on 1/4/2018.
 */

public class Event {
    private String hostID;
    private String passengers;
    private String pickUp;
    private String dropOff;
    private String dateTime;
    private String numOfSeat;
    private String type;
    private String boyOnly;
    private String girlOnly;
    private String message;
    private String isRequest;

    public Event(){

    }

    public Event(String hostID, String pickUP, String dropOff, String dateTime, String numOfSeat, String type, String boyOnly, String girlOnly, String message, String isRequest){
        this.hostID = hostID;
        this.passengers = "";
        this.pickUp = pickUP;
        this.dropOff = dropOff;
        this.dateTime = dateTime;
        this.numOfSeat = numOfSeat;
        this.type = type;
        this.boyOnly = boyOnly;
        this.girlOnly = girlOnly;
        this.message = message;
        this.isRequest = isRequest;
    }

    public String getDateTime() {
        return dateTime;
    }
    public String getBoyOnly() {
        return boyOnly;
    }

    public String getDropOff() {
        return dropOff;
    }

    public String getHostID() {
        return hostID;
    }

    public String getGirlOnly() {
        return girlOnly;
    }

    public String getIsRequest() {
        return isRequest;
    }

    public String getMessage() {
        return message;
    }

    public String getNumOfSeat() {
        return numOfSeat;
    }

    public String getPassengers() {
        return passengers;
    }

    public String getPickUp() {
        return pickUp;
    }

    public String getType() {
        return type;
    }

    public void setHostID(String hostID){
        this.hostID = hostID;
    }
}
