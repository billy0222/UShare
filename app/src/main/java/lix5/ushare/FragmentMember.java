package lix5.ushare;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentMember extends Fragment {
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    private CircleImageView hostPic;
    private TextView hostName;
    private TextView hostPlate;
    private ImageView star1,star2,star3,star4,star5;

    

    public FragmentMember(){}

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
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Event event = (Event)getActivity().getIntent().getSerializableExtra("event");
        mDatabase.child("users/").child(event.getHostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Picasso.get().load(dataSnapshot.child("avatar").getValue(String.class)).into(hostPic);
                hostName.setText(dataSnapshot.child("username").getValue(String.class));
                if(dataSnapshot.hasChild("drive")){
                    hostPlate.setText(dataSnapshot.child("drive").getValue(String.class));
                    hostPlate.setVisibility(View.VISIBLE);
                }
                else{
                    hostPlate.setVisibility(View.INVISIBLE);
                }
                String rating = dataSnapshot.child("rating").getValue(String.class);
                showStarFromRating(rating);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void showStarFromRating(String rating){
        if(rating.equals("")){       // user hasn't received any rating now
            star1.setVisibility(View.INVISIBLE);
            star2.setVisibility(View.INVISIBLE);
            star3.setVisibility(View.INVISIBLE);
            star4.setVisibility(View.INVISIBLE);
            star5.setVisibility(View.INVISIBLE);
        }
        else{       // user has rating
            int user_rating = Integer.parseInt(rating);
            if(user_rating == 5){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_filled);
            }
            else if(user_rating > 4 && user_rating < 5){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_half);
            }
            else if(user_rating == 4){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating > 3 && user_rating < 4){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_half);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating == 3){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating > 2 && user_rating < 3){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_half);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating == 2){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating > 1 && user_rating < 2){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_half);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating == 1){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_none);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating > 0 && user_rating < 1){
                star1.setImageResource(R.drawable.star_half);
                star2.setImageResource(R.drawable.star_none);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else{
                star1.setImageResource(R.drawable.star_none);
                star2.setImageResource(R.drawable.star_none);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
        }
    }
}
