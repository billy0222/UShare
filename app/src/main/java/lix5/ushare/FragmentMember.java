package lix5.ushare;

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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class FragmentMember extends Fragment {
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    private CircleImageView hostPic;
    private TextView hostName;
    private TextView hostPlate;
    private ImageView star1, star2, star3, star4, star5;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<User> myDataset;
    private ArrayList<String> myDatasetID;


    public FragmentMember() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_member, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        hostPic = view.findViewById(R.id.host_pic);
        hostName = view.findViewById(R.id.hostName);
        hostPlate = view.findViewById(R.id.hostPlate);
        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        star4 = view.findViewById(R.id.star4);
        star5 = view.findViewById(R.id.star5);
        myDataset = new ArrayList<>();
        myDatasetID = new ArrayList<>();
        mRecyclerView = view.findViewById(R.id.recyclerView_passenger);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        String event_key = getActivity().getIntent().getStringExtra("event_key");
        mDatabase.child("events/").child(event_key).child("passengers").addChildEventListener(new ChildEventListener() {        // Passengers details
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mDatabase.child("users/").child(dataSnapshot.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot2) {
                        myDataset.add(dataSnapshot2.getValue(User.class));
                        myDatasetID.add(dataSnapshot.getKey());
                        System.out.println(dataSnapshot.getKey());
                        mRecyclerView.getAdapter().notifyItemInserted(myDataset.size() - 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                System.out.println(key);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Event event = (Event) getActivity().getIntent().getSerializableExtra("event");
        mDatabase.child("users/").child(event.getHostID()).addListenerForSingleValueEvent(new ValueEventListener() {    //Host details
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Picasso.get().load(dataSnapshot.child("avatar").getValue(String.class)).into(hostPic);
                hostName.setText(dataSnapshot.child("username").getValue(String.class));
                if (dataSnapshot.hasChild("drive")) {
                    hostPlate.setText(dataSnapshot.child("drive").getValue(String.class));
                    hostPlate.setVisibility(View.VISIBLE);
                } else {
                    hostPlate.setVisibility(View.INVISIBLE);
                }
                String rating = dataSnapshot.child("rating").getValue(String.class);
                showStarFromRating(rating, star1, star2, star3, star4, star5);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showStarFromRating(String rating, ImageView star1, ImageView star2, ImageView star3, ImageView star4, ImageView star5) {
        if (rating.equals("")) {       // user hasn't received any rating now
            star1.setVisibility(View.INVISIBLE);
            star2.setVisibility(View.INVISIBLE);
            star3.setVisibility(View.INVISIBLE);
            star4.setVisibility(View.INVISIBLE);
            star5.setVisibility(View.INVISIBLE);
        } else {       // user has rating
            double user_rating = Double.parseDouble(rating);
            if (user_rating == 5.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_filled);
            } else if (user_rating > 4.0 && user_rating < 5.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_half);
            } else if (user_rating == 4.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_none);
            } else if (user_rating > 3.0 && user_rating < 4.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_half);
                star5.setImageResource(R.drawable.star_none);
            } else if (user_rating == 3.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            } else if (user_rating > 2.0 && user_rating < 3.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_half);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            } else if (user_rating == 2.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            } else if (user_rating > 1.0 && user_rating < 2.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_half);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            } else if (user_rating == 1.0) {
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_none);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            } else if (user_rating > 0 && user_rating < 1.0) {
                star1.setImageResource(R.drawable.star_half);
                star2.setImageResource(R.drawable.star_none);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            } else {
                star1.setImageResource(R.drawable.star_none);
                star2.setImageResource(R.drawable.star_none);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<User> mDataset;

        public MyAdapter(ArrayList<User> mDataset) {
            this.mDataset = mDataset;
        }

        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.passenger_card, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.passengerName.setText(mDataset.get(position).getUsername());
            Picasso.get().load(mDataset.get(position).getAvatar()).into(holder.passengerPic);
            showStarFromRating(mDataset.get(position).getRating(), holder.passengerStar1, holder.passengerStar2, holder.passengerStar3, holder.passengerStar4, holder.passengerStar5);
        }

        public int getItemCount() {
            return mDataset.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CircleImageView passengerPic;
            public TextView passengerName;
            public ImageView passengerStar1, passengerStar2, passengerStar3, passengerStar4, passengerStar5;

            public ViewHolder(View v) {
                super(v);
                passengerName = v.findViewById(R.id.passenger_name);
                passengerPic = v.findViewById(R.id.passenger_pic);
                passengerStar1 = v.findViewById(R.id.star1_passenger);
                passengerStar2 = v.findViewById(R.id.star2_passenger);
                passengerStar3 = v.findViewById(R.id.star3_passenger);
                passengerStar4 = v.findViewById(R.id.star4_passenger);
                passengerStar5 = v.findViewById(R.id.star5_passenger);
            }
        }
    }
}
