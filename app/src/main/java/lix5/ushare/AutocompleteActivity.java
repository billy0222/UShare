package lix5.ushare;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;

public class AutocompleteActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PLACE_PICKER_REQUEST = 3;
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "Search";
    private TextView current;
    private Button placePicker;
    private TextView[] recommend = new TextView[4];
    private TextView[] recentTextView = new TextView[5];
    private String[] recommendID = new String[4];
    private String[] recommendName = new String[4];
    private ArrayList<String> recentPlaceID = new ArrayList<>();
    private String[] recentPlaceName = new String[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autocomplete);
        readData();
        current = (TextView) findViewById(R.id.current);
        placePicker = (Button) findViewById(R.id.placePicker);
        recentTextView[0] =(TextView) findViewById(R.id.recent1);
        recentTextView[1] =(TextView) findViewById(R.id.recent2);
        recentTextView[2] =(TextView) findViewById(R.id.recent3);
        recentTextView[3] =(TextView) findViewById(R.id.recent4);
        recentTextView[4] =(TextView) findViewById(R.id.recent5);
        recommend[0] = (TextView) findViewById(R.id.ch);
        recommend[1] = (TextView) findViewById(R.id.hh);
        recommend[2] = (TextView) findViewById(R.id.north);
        recommend[3] = (TextView) findViewById(R.id.south);
        recommendID[0] = "ChIJewo4Ss8GBDQRLP2qdWG8vuU";
        recommendID[1] = "ChIJNQDzl4oDBDQRRXXRbwWs08A";
        recommendID[2] = "ChIJs0QjWhUEBDQRQ40xo5w37iI";
        recommendID[3] = "ChIJ42FQVWsEBDQRosfxJCz0J2w";
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this,0, this)
                .build();
        mGoogleApiClient.connect();
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("HK").build();
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                returnResult(place);
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        current.setOnClickListener(v->{
            if (mGoogleApiClient.isConnected()) {
                if (ContextCompat.checkSelfPermission(AutocompleteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AutocompleteActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_CODE);
                } else
                    callPlaceDetectionApi();
            }
        });

        placePicker.setOnClickListener(v->{
                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(intentBuilder.build(AutocompleteActivity.this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        });

        // get recent place by id
        if (!recentPlaceID.isEmpty()) {
            String[] array = recentPlaceID.toArray(new String[0]);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, array);
            placeResult.setResultCallback((PlaceBuffer places) -> {
                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                    for(int i=0; i< places.getCount(); i++){
                        recentPlaceName[i] = places.get(i).getAddress().toString()+ " " + places.get(i).getName();
                        Log.i(TAG, "Recent  Place found: " + places.get(i).getName());
                    }
                    int k = 0;
                    for(int i = recentPlaceID.size()-1; i>=0;i--){
                        int j = i;
                        recentTextView[k].setOnClickListener(v->{
                            returnResult(places.get(j));
                            places.release();
                        });
                        recentTextView[k].setText(recentPlaceName[i]);
                        k++;
                    }
                } else {
                    Log.e(TAG, "Place not found");
                }
            });
        }

        // get recommend place by id
        PendingResult<PlaceBuffer> recentResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, recommendID);
        recentResult.setResultCallback(places -> {
            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                for(int i=0; i<places.getCount() ; i++){
                    int j = i;
                    recommend[i].setOnClickListener(v->{
                        returnResult(places.get(j));
                        places.release();
                    });
                    recommendName[i] = places.get(i).getAddress().toString()+ " " + places.get(i).getName();
                    recommend[i].setText(recommendName[i]);
                    //Log.i(TAG, "Place found: " + places.get(i).getName());
                }
            } else {
                Log.e(TAG, "Place not found");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                returnResult(place);
        }
    }


    public void returnResult(Place place){
        Intent intent = new Intent();
        CharSequence result = place.getAddress().toString() + " " + place.getName();
        intent.putExtra("result", result);
        setResult(RESULT_OK, intent);
        Log.i(TAG, "Place: " + place.getName());

        if(!Arrays.asList(recommendID).contains(place.getId())){
            if (recentPlaceID != null) {
                for(int i = recentPlaceID.size()-1; i>=0;i--){
                    if(recentPlaceID.get(i).equals(place.getId()))
                        recentPlaceID.remove(i);
                }
                if (recentPlaceID.size()==5) {
                    recentPlaceID.remove(0);
                }
            }
            recentPlaceID.add(place.getId());
            saveData();
        }
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());
    }

    private void callPlaceDetectionApi() throws SecurityException {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(likelyPlaces -> {
            returnResult(likelyPlaces.get(0).getPlace());
            likelyPlaces.release();
        });
    }

    private String convertToString(ArrayList<String> list) {

        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : list)
        {
            sb.append(delim);
            sb.append(s);;
            delim = ",";
        }
        return sb.toString();
    }

    private ArrayList<String> convertToArray(String string) {

        ArrayList<String> list = new ArrayList<String>(Arrays.asList(string.split(",")));
        return list;
    }

    public void saveData(){
        SharedPreferences recent = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = recent.edit();
        String s = convertToString(recentPlaceID);
        editor.putString("recentPlace",s).apply();
    }
    public void readData(){
        SharedPreferences recent = PreferenceManager.getDefaultSharedPreferences(this);
        String data = recent.getString("recentPlace", null);
        if (data != null) {
            recentPlaceID = convertToArray(data);
        }

    }
}
