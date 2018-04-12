package lix5.ushare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentScheduler extends PreferenceFragmentCompat {

    Preference firstLoc, firstDes, secLoc, secDes;
    CheckBoxPreference retrace;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);
        firstLoc = (Preference) findPreference("firstLoc");
        firstDes = (Preference) findPreference("firstDes");
        secLoc = (Preference) findPreference("secLoc");
        secDes = (Preference) findPreference("secDes");
        retrace = (CheckBoxPreference) findPreference("retrace");
        retrace.setOnPreferenceClickListener(v -> {
            if (retrace.isChecked()) {
                secLoc.setEnabled(false);
                secDes.setEnabled(false);
                secLoc.setSummary(firstDes.getSummary());
                secDes.setSummary(firstLoc.getSummary());
            }
            if (!retrace.isChecked()) {
                secLoc.setEnabled(true);
                secDes.setEnabled(true);
            }
            return true;
        });
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

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    CharSequence result = null;
                    Bundle results = data.getExtras();
                    if (results != null)
                        result = results.getCharSequence("result");
                    firstLoc.setSummary(result);
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
                    if (results != null)
                        result = results.getCharSequence("result");
                    firstDes.setSummary(result);
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
                    if (results != null)
                        result = results.getCharSequence("result");
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
                    if (results != null)
                        result = results.getCharSequence("result");
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

}