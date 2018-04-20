package lix5.ushare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static lix5.ushare.AddScheduleActivity.details;

public class FragmentScheduler extends PreferenceFragmentCompat {

    Preference firstLoc, firstDes, secLoc, secDes;
    String firstLocID, firstDesID, secLocID, secDesID;
    CheckBoxPreference retrace;
    ListPreference type, preference;
    EditTextPreference seats;
    OnDataPass dataPasser;


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);
        firstLoc = (Preference) findPreference("firstLoc");
        firstDes = (Preference) findPreference("firstDes");
        secLoc = (Preference) findPreference("secLoc");
        secDes = (Preference) findPreference("secDes");
        retrace = (CheckBoxPreference) findPreference("retrace");
        type = (ListPreference) findPreference("type");
        seats = (EditTextPreference) findPreference("seats");
        preference = (ListPreference) findPreference("preference");

        load();


        firstLoc.setOnPreferenceClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AutocompleteActivity.class), 0);
            return true;
        });
        firstDes.setOnPreferenceClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AutocompleteActivity.class), 1);
            return true;
        });
        secLoc.setOnPreferenceClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AutocompleteActivity.class), 2);
            return true;
        });
        secDes.setOnPreferenceClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AutocompleteActivity.class), 3);
            return true;
        });
        retrace.setChecked(false);
        retrace.setOnPreferenceClickListener(v -> {
            if (retrace.isChecked()) {
                secLoc.setEnabled(false);
                secDes.setEnabled(false);
                secLoc.setSummary(firstDes.getSummary());
                secDes.setSummary(firstLoc.getSummary());
                secLocID = firstDesID;
                secDesID = firstLocID;
                dataPasser.onDataPass(details.SECLOC, secLoc.getSummary().toString());
                dataPasser.onDataPass(details.SECDES, secDes.getSummary().toString());
                dataPasser.onDataPass(details.SECLOCID, secLocID);
                dataPasser.onDataPass(details.SECDESID, secDesID);
            }
            if (!retrace.isChecked()) {
                secLoc.setEnabled(true);
                secDes.setEnabled(true);
            }
            return true;
        });
        type.setOnPreferenceChangeListener((preference, newValue) -> {
            int index = type.findIndexOfValue(newValue.toString());

            if (index != -1) {
                type.setSummary(type.getEntries()[index]);
                dataPasser.onDataPass(details.TYPE, type.getEntries()[index].toString());
            }
            return true;
        });
        seats.setOnPreferenceChangeListener((preference, newValue) -> {
            if (isNumeric(newValue.toString())) {
                int val = Integer.parseInt(newValue.toString());
                if ((val >= 1) && (val <= 10)) {
                    seats.setSummary("" + val);
                    dataPasser.onDataPass(details.SEATS, newValue.toString());
                    return true;
                } else {
                    Toast.makeText(getContext(), "Please set number between 1 to 10 ", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(getContext(), "Please set number between 1 to 10 ", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        preference.setOnPreferenceChangeListener((p, newValue) -> {
            int index = preference.findIndexOfValue(newValue.toString());

            if (index != -1) {
                preference.setSummary(preference.getEntries()[index]);
                dataPasser.onDataPass(details.PREFERENCE, preference.getEntries()[index].toString());
            }
            return true;
        });

    }

    private void load() {
        SharedPreferences loadSchedulePreferences = getActivity().getSharedPreferences("loadSchedule", MODE_PRIVATE);
        firstLoc.setSummary(loadSchedulePreferences.getString("fl", ""));
        firstDes.setSummary(loadSchedulePreferences.getString("fd", ""));
        secLoc.setSummary(loadSchedulePreferences.getString("sl", ""));
        secDes.setSummary(loadSchedulePreferences.getString("sd", ""));
        type.setSummary(loadSchedulePreferences.getString("type", ""));
        type.setValue(loadSchedulePreferences.getString("type", ""));
        seats.setSummary(loadSchedulePreferences.getString("seats", ""));
        preference.setSummary(loadSchedulePreferences.getString("pref", ""));
        if (!loadSchedulePreferences.getBoolean("isTwice", false)) {
            secLoc.setEnabled(false);
            secDes.setEnabled(false);
            retrace.setEnabled(false);
        } else {
            secLoc.setEnabled(true);
            secDes.setEnabled(true);
            retrace.setEnabled(true);
        }
    }

    public boolean isNumeric(String str) {
        try {
            int d = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    CharSequence result = null;
                    Bundle results = data.getExtras();
                    if (results != null) {
                        result = results.getCharSequence("result");
                        firstLocID = results.getString("placeID");
                        dataPasser.onDataPass(details.FIRSTLOC, result.toString());
                        dataPasser.onDataPass(details.FIRSTLOCID, firstLocID);
                    }
                    firstLoc.setSummary(result);
                    if (retrace.isChecked()) {
                        secDes.setSummary(result);
                        secDesID = firstLocID;
                        dataPasser.onDataPass(details.SECDES, result.toString());
                        dataPasser.onDataPass(details.SECDESID, secDesID);
                    }
                    Log.i("", "Place: " + result);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(getContext(), data);
                    Log.i("", status.getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    CharSequence result = null;
                    Bundle results = data.getExtras();
                    if (results != null) {
                        result = results.getCharSequence("result");
                        firstDesID = results.getString("placeID");
                        dataPasser.onDataPass(details.FIRSTDES, result.toString());
                        dataPasser.onDataPass(details.FIRSTDESID, firstDesID);
                    }
                    firstDes.setSummary(result);
                    if (retrace.isChecked()) {
                        secLoc.setSummary(result);
                        secLocID = firstDesID;
                        dataPasser.onDataPass(details.SECLOC, result.toString());
                        dataPasser.onDataPass(details.SECLOCID, secLocID);
                    }
                    Log.i("", "Place: " + result);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(getContext(), data);
                    Log.i("", status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    CharSequence result = null;
                    Bundle results = data.getExtras();
                    if (results != null) {
                        result = results.getCharSequence("result");
                        secLocID = results.getString("placeID");
                        dataPasser.onDataPass(details.SECLOC, result.toString());
                        dataPasser.onDataPass(details.SECLOCID, secLocID);
                    }
                    secLoc.setSummary(result);
                    Log.i("", "Place: " + result);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(getContext(), data);
                    Log.i("", status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    CharSequence result = null;
                    Bundle results = data.getExtras();
                    if (results != null) {
                        result = results.getCharSequence("result");
                        secDesID = results.getString("placeID");
                        dataPasser.onDataPass(details.SECDES, result.toString());
                        dataPasser.onDataPass(details.SECDESID, secDesID);
                    }
                    secDes.setSummary(result);
                    Log.i("", "Place: " + result);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(getContext(), data);
                    Log.i("", status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public interface OnDataPass {
        // call dataPasser.onDataPass(type, "send abc") to pass data
        public void onDataPass(details dataType, String data);
    }
}