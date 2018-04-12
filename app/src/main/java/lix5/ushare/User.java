package lix5.ushare;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kevin on 25/3/2018.
 */

public class User {
    public String username;
    public String email;
    public String password;
    public String avatar;
    public String sex;
    public String bio;
    public String rating;
    public String phoneNum;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String password) {        // Constructor for creating user
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = "";
        this.sex = "";
        this.bio = "";
        this.rating = "";
        this.phoneNum = "";
    }

    public User(String username, String email, String password, String avatar, String sex, String bio, String rating, String phoneNum) {      //Constructor for editing profile
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.sex = sex;
        this.bio = bio;
        this.rating = rating;
        this.phoneNum = phoneNum;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getBio() {
        return bio;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getRating() {
        return rating;
    }

    public String getSex() {
        return sex;
    }

    public String getUsername() {
        return username;
    }

    public Map<String, Object> toMapHaveAvatar() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("password", password);
        result.put("avatar", avatar);
        result.put("sex", sex);
        result.put("bio", bio);
        result.put("rating", rating);
        result.put("phoneNum", phoneNum);
        return result;
    }
}

