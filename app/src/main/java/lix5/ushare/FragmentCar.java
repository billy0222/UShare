package lix5.ushare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

public class FragmentCar extends Fragment {
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database
    private ArrayList<Event> myDataset;
    private ArrayList<String> myDatasetID;

    public FragmentCar() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myDataset = new ArrayList<>();
        myDatasetID = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView rv = new RecyclerView(getContext());
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new rvAdapter(myDataset));

        mDatabase.child("events/").orderByChild("type").equalTo("Car").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Event tempEvent = dataSnapshot.getValue(Event.class);
                myDataset.add(tempEvent);
                myDatasetID.add(dataSnapshot.getKey());
                rv.getAdapter().notifyItemInserted(myDataset.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // event changed
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // event removed
                String key = dataSnapshot.getKey();
                int index = myDatasetID.indexOf(key);
                if (index > -1){
                    myDatasetID.remove(index);
                    myDataset.remove(index);
                    rv.getAdapter().notifyItemRemoved(index);
                }else{
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

    public class rvAdapter extends RecyclerView.Adapter<rvAdapter.ViewHolder>{
        private ArrayList<Event> mData;

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
            if(mData.get(position).getBoyOnly().equals("true") && mData.get(position).getGirlOnly().equals("false")){
                holder.gender.setImageResource(R.drawable.man);
                holder.genderText.setText(R.string.menOnly);
            }
            else if(mData.get(position).getGirlOnly().equals("true") && mData.get(position).getBoyOnly().equals("false")){
                holder.gender.setImageResource(R.drawable.woman);
                holder.genderText.setText(R.string.womenOnly);
            }
            else{
                holder.gender.setVisibility(GONE);
                holder.genderText.setVisibility(GONE);
            }
            holder.fromText.setText(mData.get(position).getPickUp());
            holder.toText.setText(mData.get(position).getDropOff());
            holder.dateTimeText.setText(mData.get(position).getDateTime());
            holder.message.setText(mData.get(position).getMessage());
            if(mData.get(position).getIsRequest().equals("true")){
                holder.isRequest.setImageResource(R.drawable.request);
                holder.isRequest.setVisibility(View.VISIBLE);
            }else{
                holder.isRequest.setVisibility(GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }

}
