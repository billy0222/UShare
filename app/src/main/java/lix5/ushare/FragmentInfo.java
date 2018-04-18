package lix5.ushare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FragmentInfo extends Fragment {
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    private TextView pickUp;
    private TextView dropOff;
    private TextView dateTime;
    private TextView remark, remarkTitle;
    private TextView distance, duration, taxiFare, taxiFare_text;


    public FragmentInfo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_info, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        pickUp = view.findViewById(R.id.info_pick_up);
        dropOff = view.findViewById(R.id.info_drop_off);
        dateTime = view.findViewById(R.id.info_time);
        remark = view.findViewById(R.id.remarks_input);
        remarkTitle = view.findViewById(R.id.remark);
        distance = view.findViewById(R.id.placeInfo_distance_input);
        duration = view.findViewById(R.id.placeInfo_duration_input);
        taxiFare = view.findViewById(R.id.placeInfo_taxiFare_input);
        taxiFare_text = view.findViewById(R.id.placeInfo_taxiFare);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Event event = (Event) getActivity().getIntent().getSerializableExtra("event");
        String eventKey = getActivity().getIntent().getStringExtra("event_key");
        pickUp.setText(event.getPickUp());
        dropOff.setText(event.getDropOff());
        dateTime.setText(event.getDateTime());
        if(event.getMessage().equals(""))
            remarkTitle.setVisibility(View.GONE);
        else
            remark.setText(event.getMessage());

        PlaceDistance result = findPlaceInfo(event.getPickUpID(), event.getDropOffID());
        distance.setText(String.valueOf(result.getDistance()) + " meters");
        duration.setText(String.valueOf(Math.round(result.getDuration() / 60)) + " minutes");

        if(event.getType().equals("Taxi")) {
            double fare = taxiFareCalculation(result.getDistance());
            taxiFare.setText("$" + fare + ", " + "$" + Math.round(fare) + " per person");

            mDatabase.child("events").child(eventKey).child("passengers").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int size = (int)dataSnapshot.getChildrenCount() + 1;
                    taxiFare.setText("$" + fare + ", " + "$" + Math.round(fare / size) + " per person");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else{
            taxiFare_text.setVisibility(View.INVISIBLE);
            taxiFare.setVisibility(View.INVISIBLE);
        }
    }

    public PlaceDistance findPlaceInfo(String pickUpPlaceID, String dropOffPlaceID){
        PlaceService service = new PlaceService("AIzaSyDpZ9qPYIuA86y1EnpkFgJMOYvB4NxJcEA");
        return service.findPlacesInfo(pickUpPlaceID, dropOffPlaceID);
    }

    public double taxiFareCalculation(int distance){
        double totalFare;
        if(distance <= 2000){       // First 2 kilometers or any part thereof => $24
            totalFare = 24;
        }
        else if(distance <= 8882){      // $1.7 for every subsequent 200 meters or part thereof before $83.5
            totalFare = 24 + (distance - 2000) / 200 * 1.7;
        }
        else{       // $1.2 for for every subsequent 200 meters or part thereof after $83.5
            totalFare = 83.5 + (distance - 8882) / 200 * 1.2;
        }
        return totalFare;
    }
}
