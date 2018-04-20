package lix5.ushare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

public class FragmentCar extends Fragment {
    private DatabaseReference mDatabase; //instance of Database
    private ArrayList<Event> myDataset;
    private ArrayList<String> myDatasetID;
    private RecyclerView rv;
    private ArrayList<String> placeIDNearPickUp;
    private ArrayList<String> placeIDNearDropOff;

    public FragmentCar() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myDataset = new ArrayList<>();
        myDatasetID = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rv = new RecyclerView(getContext());
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new rvAdapter(myDataset));

        mDatabase.child("events/").orderByChild("type").equalTo("Car").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Event tempEvent = dataSnapshot.getValue(Event.class);
                if (isAfterToday(tempEvent)) {
                    int positionToInsert = sorting(tempEvent);
                    myDataset.add(positionToInsert, tempEvent);
                    myDatasetID.add(positionToInsert, dataSnapshot.getKey());
                    rv.getAdapter().notifyItemInserted(positionToInsert);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // event changed
                String key = dataSnapshot.getKey();
                Event tempEvent = dataSnapshot.getValue(Event.class);
                int index = myDatasetID.indexOf(key);
                if (index > -1) {
                    myDataset.set(index, tempEvent);
                    rv.getAdapter().notifyItemChanged(index);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + index);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // event removed
                String key = dataSnapshot.getKey();
                int index = myDatasetID.indexOf(key);
                if (index > -1) {
                    myDatasetID.remove(index);
                    myDataset.remove(index);
                    rv.getAdapter().notifyItemRemoved(index);
                } else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + index);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_car, container, false);
        return rv;
    }

    public boolean isAfterToday(Event event) {
        Date eventDateTime = null;
        DateFormat formatter = new SimpleDateFormat("EE, dd MMMM, HH:mm", Locale.US);
        try {
            eventDateTime = formatter.parse(event.getDateTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar eventDateTimeToCalendar = Calendar.getInstance();
        eventDateTimeToCalendar.setTime(eventDateTime);
        eventDateTimeToCalendar.set(Calendar.YEAR, 2018);
        Calendar currentTimeToCalendar = Calendar.getInstance();
        currentTimeToCalendar.setTime(new Date());

        return eventDateTimeToCalendar.after(currentTimeToCalendar);
    }

    public int sorting(Event event) {
        int locationToInsert = 0;
        Date event1DateTime = null;
        Date event2DateTime = null;
        DateFormat formatter = new SimpleDateFormat("EE, dd MMMM, HH:mm", Locale.US);

        if (myDataset.isEmpty()) {      // no element
            return 0;
        } else {
            for (int i = 0; i < myDataset.size(); i++) {
                try {
                    event1DateTime = formatter.parse(event.getDateTime());
                    event2DateTime = formatter.parse(myDataset.get(i).getDateTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (event2DateTime.compareTo(event1DateTime) == 0 || event2DateTime.compareTo(event1DateTime) == 1) {
                    locationToInsert = i;
                    break;
                }
                if (i == myDataset.size() - 1) {      // add at the last position
                    locationToInsert = myDataset.size();
                }
            }
            return locationToInsert;
        }
    }

    public void filter(String pickUpID, String dropOffID, CharSequence time, double pickUpLat, double pickUpLng, double dropOffLat, double dropOffLng) {
        ArrayList<Event> tempEventList = new ArrayList<>();
        tempEventList.addAll(myDataset);
        Iterator<Event> itr = tempEventList.iterator();
        DateFormat formatter = new SimpleDateFormat("EE, dd MMMM, HH:mm", Locale.US);
        Date searchDateTime30minutesBefore = null;
        Date searchDateTime30minutesAfter = null;

        if (!pickUpID.equals("")) {
            placeIDNearPickUp = new ArrayList<>();      //ArrayList for PlaceID near pick up location;
            findPickUpNear(pickUpLat, pickUpLng);
        }

        if (!dropOffID.equals("")) {
            placeIDNearDropOff = new ArrayList<>();       //ArrayList for PlaceID near drop off location
            findDropOffNear(dropOffLat, dropOffLng);
        }

        if (!TextUtils.isEmpty(time)) {
            Date searchDateTime = null;
            try {
                searchDateTime = formatter.parse(time.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar searchDateTimeToCalender = Calendar.getInstance();
            searchDateTimeToCalender.setTime(searchDateTime);
            searchDateTime30minutesBefore = new Date(searchDateTimeToCalender.getTimeInMillis() - (31 * 60000));   // 30mins before
            searchDateTime30minutesAfter = new Date(searchDateTimeToCalender.getTimeInMillis() + (31 * 60000));    // 30mins after
        }

        if (!pickUpID.equals("") && dropOffID.equals("") && TextUtils.isEmpty(time)) {    // pickUp, "", ""
            while (itr.hasNext()) {
                Event event = itr.next();
                if (!(event.getPickUpID().equals(pickUpID) || placeIDNearPickUp.contains(event.getPickUpID()))) {
                    itr.remove();
                }
            }
        } else if (pickUpID.equals("") && !dropOffID.equals("") && TextUtils.isEmpty(time)) {   // "", dropOff, ""
            while (itr.hasNext()) {
                Event event = itr.next();
                if (!(event.getDropOffID().equals(dropOffID) || placeIDNearDropOff.contains(event.getDropOffID()))) {
                    itr.remove();
                }
            }
        } else if (pickUpID.equals("") && dropOffID.equals("") && !TextUtils.isEmpty(time)) {   // "", "", time
            try {
                while (itr.hasNext()) {
                    Event event = itr.next();
                    Date eventDateTime = formatter.parse(event.getDateTime());
                    if (!(eventDateTime.after(searchDateTime30minutesBefore) && eventDateTime.before(searchDateTime30minutesAfter))) {
                        itr.remove();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (!pickUpID.equals("") && !dropOffID.equals("") && TextUtils.isEmpty(time)) {  // pickUP, dropOff, ""
            while (itr.hasNext()) {
                Event event = itr.next();
                if (!((event.getPickUpID().equals(pickUpID) || placeIDNearPickUp.contains(event.getPickUpID()))
                        && (event.getDropOffID().equals(dropOffID) || placeIDNearDropOff.contains(event.getDropOffID())))) {
                    itr.remove();
                }
            }
        } else if (!pickUpID.equals("") && dropOffID.equals("") && !TextUtils.isEmpty(time)) {  // pickUp, "", time
            try {
                while (itr.hasNext()) {
                    Event event = itr.next();
                    Date eventDateTime = formatter.parse(event.getDateTime());
                    if (!(eventDateTime.after(searchDateTime30minutesBefore)
                            && eventDateTime.before(searchDateTime30minutesAfter)
                            && (event.getPickUpID().equals(pickUpID) || placeIDNearPickUp.contains(event.getPickUpID())))) {
                        itr.remove();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (pickUpID.equals("") && !dropOffID.equals("") && !TextUtils.isEmpty(time)) {   // "", dropOff, time
            try {
                while (itr.hasNext()) {
                    Event event = itr.next();
                    Date eventDateTime = formatter.parse(event.getDateTime());
                    if (!(eventDateTime.after(searchDateTime30minutesBefore)
                            && eventDateTime.before(searchDateTime30minutesAfter)
                            && (event.getDropOffID().equals(dropOffID) || placeIDNearDropOff.contains(event.getDropOffID())))) {
                        itr.remove();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (!pickUpID.equals("") && !dropOffID.equals("") && !TextUtils.isEmpty(time)) {  //  pickUP, dropOff, time
            try {
                while (itr.hasNext()) {
                    Event event = itr.next();
                    Date eventDateTime = formatter.parse(event.getDateTime());
                    if (!(eventDateTime.after(searchDateTime30minutesBefore)
                            && eventDateTime.before(searchDateTime30minutesAfter)
                            && (event.getDropOffID().equals(dropOffID) || placeIDNearDropOff.contains(event.getDropOffID()))
                            && (event.getPickUpID().equals(pickUpID) || placeIDNearPickUp.contains(event.getPickUpID())))) {
                        itr.remove();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (pickUpID.equals("") && dropOffID.equals("") && TextUtils.isEmpty(time)) {    // "", "", ""
            tempEventList = myDataset;
        }
        rv.setAdapter(new rvAdapter(tempEventList));
        rv.getAdapter().notifyDataSetChanged();
    }

    private void findPickUpNear(double lat, double lng) {
        PlaceService service = new PlaceService("AIzaSyDpZ9qPYIuA86y1EnpkFgJMOYvB4NxJcEA");
        ArrayList<String> nearPlaceID = new ArrayList<>();

        @SuppressLint("RestrictedApi")
        List<Place> findPlaces = service.findPlaces(lat, lng);
        for (int i = 0; i < findPlaces.size(); i++) {
            nearPlaceID.add(findPlaces.get(i).getId());
        }
        placeIDNearPickUp = nearPlaceID;
    }

    private void findDropOffNear(double lat, double lng) {
        PlaceService service = new PlaceService("AIzaSyDpZ9qPYIuA86y1EnpkFgJMOYvB4NxJcEA");
        ArrayList<String> nearPlaceID = new ArrayList<>();

        @SuppressLint("RestrictedApi") List<Place> findPlaces = service.findPlaces(lat, lng);
        for (int i = 0; i < findPlaces.size(); i++) {
            nearPlaceID.add(findPlaces.get(i).getId());
        }
        placeIDNearDropOff = nearPlaceID;
    }

    public class rvAdapter extends RecyclerView.Adapter<rvAdapter.ViewHolder> {
        private ArrayList<Event> mData;

        public rvAdapter(ArrayList<Event> data) {
            mData = data;
        }

        @Override
        public rvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.car_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.hostText.setText(mData.get(position).getHostName());
            holder.vacancyText.setText(mData.get(position).getNumOfSeat());
            if (mData.get(position).getBoyOnly().equals("true") && mData.get(position).getGirlOnly().equals("false")) {
                holder.gender.setImageResource(R.drawable.man);
                holder.genderText.setText(R.string.menOnly);
            } else if (mData.get(position).getGirlOnly().equals("true") && mData.get(position).getBoyOnly().equals("false")) {
                holder.gender.setImageResource(R.drawable.woman);
                holder.genderText.setText(R.string.womenOnly);
            } else {
                holder.gender.setVisibility(GONE);
                holder.genderText.setVisibility(GONE);
            }
            holder.fromText.setText(mData.get(position).getPickUp());
            holder.toText.setText(mData.get(position).getDropOff());
            holder.dateTimeText.setText(mData.get(position).getDateTime());
            holder.message.setText(mData.get(position).getMessage());
            if (mData.get(position).getIsRequest().equals("true")) {
                holder.isRequest.setImageResource(R.drawable.request);
                holder.isRequest.setVisibility(View.VISIBLE);
            } else {
                holder.isRequest.setVisibility(GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView hostText;
            public TextView vacancyText;
            public ImageView gender;
            public TextView genderText;
            public TextView fromText;
            public TextView toText;
            public TextView dateTimeText;
            public TextView message;
            public ImageView isRequest;

            public ViewHolder(View v) {
                super(v);
                hostText = v.findViewById(R.id.hostText_carItem);
                vacancyText = v.findViewById(R.id.vacancyText_carItem);
                gender = v.findViewById(R.id.gender_carItem);
                genderText = v.findViewById(R.id.genderText_carItem);
                fromText = v.findViewById(R.id.fromText_carItem);
                toText = v.findViewById(R.id.toText_carItem);
                dateTimeText = v.findViewById(R.id.dateTimeText_carItem);
                message = v.findViewById(R.id.message_carItem);
                isRequest = v.findViewById(R.id.request_carItem);

                v.setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("event", mData.get(getAdapterPosition()));
                    bundle.putString("event_key", myDatasetID.get(getAdapterPosition()));
                    startActivity(new Intent(getActivity(), EventActivity.class).putExtras(bundle));
                });
            }
        }
    }

}
