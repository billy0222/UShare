package lix5.ushare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class FragmentPresentEvent extends Fragment {
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    private RecyclerView mRecyclerView;
    private ArrayList<Event> myDataset;
    private ArrayList<String> myDatasetID;

    public FragmentPresentEvent() {

    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myevent_present_event, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        myDataset = new ArrayList<>();
        myDatasetID = new ArrayList<>();

        mRecyclerView = view.findViewById(R.id.recyclerView_myEvent_presentEvent);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new MyAdapter(myDataset));

        mDatabase.child("events").orderByChild("hostID").equalTo(mAuth.getUid()).addChildEventListener(new ChildEventListener() {   // User as host
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    Event tempEvent = dataSnapshot.getValue(Event.class);
                    Date eventDateTime = null;
                    DateFormat formatter = new SimpleDateFormat("EE, dd MMMM, HH:mm", Locale.US);
                    try {
                        eventDateTime = formatter.parse(tempEvent.getDateTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar eventDateTimeToCalendar = Calendar.getInstance();
                    eventDateTimeToCalendar.setTime(eventDateTime);
                    eventDateTimeToCalendar.set(Calendar.YEAR, 2018);
                    Calendar currentTimeToCalendar = Calendar.getInstance();
                    currentTimeToCalendar.setTime(new Date());

                    if (eventDateTimeToCalendar.after(currentTimeToCalendar) || eventDateTimeToCalendar.equals(currentTimeToCalendar)) {
                        int positionToInsert = sorting(tempEvent);
                        myDataset.add(positionToInsert, tempEvent);
                        myDatasetID.add(positionToInsert, dataSnapshot.getKey());
                        mRecyclerView.getAdapter().notifyItemInserted(positionToInsert);
                    }
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
                    mRecyclerView.getAdapter().notifyItemChanged(index);
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
                    mRecyclerView.getAdapter().notifyItemRemoved(index);
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

        mDatabase.child("events").orderByChild("passengers").addChildEventListener(new ChildEventListener() {   // User as passenger
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    Event tempEvent = dataSnapshot.getValue(Event.class);
                    Date eventDateTime = null;
                    DateFormat formatter = new SimpleDateFormat("EE, dd MMMM, HH:mm", Locale.US);
                    try {
                        eventDateTime = formatter.parse(tempEvent.getDateTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar eventDateTimeToCalendar = Calendar.getInstance();
                    eventDateTimeToCalendar.setTime(eventDateTime);
                    eventDateTimeToCalendar.set(Calendar.YEAR, 2018);
                    Calendar currentTimeToCalendar = Calendar.getInstance();
                    currentTimeToCalendar.setTime(new Date());

                    if ((eventDateTimeToCalendar.after(currentTimeToCalendar) || eventDateTimeToCalendar.equals(currentTimeToCalendar))
                            && tempEvent.getPassengers().contains(mAuth.getUid())) {
                        int positionToInsert = sorting(tempEvent);
                        myDataset.add(positionToInsert, tempEvent);
                        myDatasetID.add(positionToInsert, dataSnapshot.getKey());
                        mRecyclerView.getAdapter().notifyItemInserted(positionToInsert);
                    }
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
                    mRecyclerView.getAdapter().notifyItemChanged(index);
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
                    mRecyclerView.getAdapter().notifyItemRemoved(index);
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
        return view;
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

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<Event> mDataset;

        public MyAdapter(ArrayList<Event> mDataset) {
            this.mDataset = mDataset;
        }

        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.taxi_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @SuppressLint("RestrictedApi")
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mDataset.get(position).getType().equals("Taxi")) {
                Picasso.get().load(R.drawable.taxi_item).into(holder.taxiCar_img);
            } else if (mDataset.get(position).getType().equals("Car")) {
                Picasso.get().load(R.drawable.car_item).into(holder.taxiCar_img);
            }
            holder.hostName.setText(mDataset.get(position).getHostName());
            holder.vacancy.setText(mDataset.get(position).getNumOfSeat());
            if (mDataset.get(position).getBoyOnly().equals("true") && mDataset.get(position).getGirlOnly().equals("false")) {
                holder.gender.setImageResource(R.drawable.man);
                holder.gender_text.setText(R.string.men_only);
            } else if (mDataset.get(position).getGirlOnly().equals("true") && mDataset.get(position).getBoyOnly().equals("false")) {
                holder.gender.setImageResource(R.drawable.woman);
                holder.gender_text.setText(R.string.womenOnly);
            } else {
                holder.gender.setVisibility(View.INVISIBLE);
                holder.gender_text.setVisibility(View.INVISIBLE);
            }
            holder.from_text.setText(mDataset.get(position).getPickUp());
            holder.to_text.setText(mDataset.get(position).getDropOff());
            holder.dateTime_text.setText(mDataset.get(position).getDateTime());
            holder.message.setText(mDataset.get(position).getMessage());
            if (mDataset.get(position).getIsRequest().equals("true")) {
                holder.request.setImageResource(R.drawable.request);
                holder.request.setVisibility(View.VISIBLE);
            } else {
                holder.request.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView cardView;
            public ImageView taxiCar_img;
            public TextView hostName;
            public TextView vacancy;
            public ImageView gender;
            public TextView gender_text;
            public TextView from_text;
            public TextView to_text;
            public TextView dateTime_text;
            public TextView message;
            public ImageView request;

            public ViewHolder(View v) {
                super(v);
                taxiCar_img = v.findViewById(R.id.img_taxiItem);
                hostName = v.findViewById(R.id.hostText_taxiItem);
                vacancy = v.findViewById(R.id.vacancyText_taxiItem);
                gender = v.findViewById(R.id.gender_taxiItem);
                gender_text = v.findViewById(R.id.genderText_taxiItem);
                from_text = v.findViewById(R.id.fromText_taxiItem);
                to_text = v.findViewById(R.id.toText_taxiItem);
                dateTime_text = v.findViewById(R.id.dateTimeText_taxiItem);
                message = v.findViewById(R.id.message_taxiItem);
                request = v.findViewById(R.id.request_taxiItem);
                cardView = v.findViewById(R.id.card_view);

                v.setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("event", mDataset.get(getAdapterPosition()));
                    bundle.putString("event_key", myDatasetID.get(getAdapterPosition()));
                    startActivity(new Intent(getActivity(), EventActivity.class).putExtras(bundle));
                });
            }
        }
    }
}