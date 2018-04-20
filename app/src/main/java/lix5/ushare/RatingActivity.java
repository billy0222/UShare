package lix5.ushare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

public class RatingActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    private TextView rating;
    private ImageView star1,star2,star3,star4,star5;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Rating> myDataset;
    private ArrayList<String> myDatasetID;
    private String rating_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        rating = findViewById(R.id.rating_rating);
        star1 = findViewById(R.id.star1_rating);
        star2 = findViewById(R.id.star2_rating);
        star3 = findViewById(R.id.star3_rating);
        star4 = findViewById(R.id.star4_rating);
        star5 = findViewById(R.id.star5_rating);
        myDataset = new ArrayList<>();
        myDatasetID = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recyclerView_rating);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        rating_user_id = getIntent().getStringExtra("rating_user_id");
        mDatabase.child("users").child(rating_user_id).child("username").addListenerForSingleValueEvent(new ValueEventListener() {  //set app bar title
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setTitle(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("users").child(rating_user_id).child("rating").addValueEventListener(new ValueEventListener() {     // listen for rating change
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userRating = dataSnapshot.getValue(String.class);
                rating.setText(userRating);
                showStarFromRating(userRating, star1, star2, star3, star4, star5);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("rating").child(rating_user_id).addChildEventListener(new ChildEventListener() {        // get the rating from other user
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                myDatasetID.add(dataSnapshot.getKey());     // raterID
                myDataset.add(dataSnapshot.getValue(Rating.class)); // rating detail
                mRecyclerView.getAdapter().notifyItemInserted(myDataset.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<RatingActivity.MyAdapter.ViewHolder>{
        private ArrayList<Rating> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public CircleImageView raterPic;
            public TextView raterName, raterMessage;
            public ImageView star1_rater, star2_rater, star3_rater, star4_rater, star5_rater;

            public ViewHolder(View v){
                super(v);
                raterName = v.findViewById(R.id.rater_name);
                raterMessage = v.findViewById(R.id.rater_message);
                raterPic = v.findViewById(R.id.rater_pic);
                star1_rater = v.findViewById(R.id.star1_rater);
                star2_rater = v.findViewById(R.id.star2_rater);
                star3_rater = v.findViewById(R.id.star3_rater);
                star4_rater = v.findViewById(R.id.star4_rater);
                star5_rater = v.findViewById(R.id.star5_rater);
            }
        }

        public MyAdapter(ArrayList<Rating> mDataset){
            this.mDataset = mDataset;
        }

        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rating_card, parent, false);
            MyAdapter.ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        public void onBindViewHolder(RatingActivity.MyAdapter.ViewHolder holder, int position){
            mDatabase.child("users").child(myDatasetID.get(position)).addListenerForSingleValueEvent(new ValueEventListener() { // set up recyclerView
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User rater = dataSnapshot.getValue(User.class);
                    holder.raterName.setText(rater.getUsername());
                    Picasso.get().load(rater.getAvatar()).into(holder.raterPic);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            holder.raterMessage.setText(mDataset.get(position).getMessage());
            showStarFromRater(myDataset.get(position).getStar(), holder.star1_rater, holder.star2_rater, holder.star3_rater, holder.star4_rater, holder.star5_rater);
        }

        public int getItemCount(){
            return mDataset.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rating, menu);
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.rate:
                mDatabase.child("rating").child(rating_user_id).orderByKey().equalTo(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            new AlertDialog.Builder(RatingActivity.this).setMessage("You have given rating to this user!").show();
                        }
                        else if(rating_user_id.equals(mAuth.getUid())){
                            new AlertDialog.Builder(RatingActivity.this).setMessage("You can't give rating to yourself!").show();
                        }
                        else{
                            startActivity(new Intent(RatingActivity.this, RateUserActivity.class).putExtra("rating_user_id", rating_user_id));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void showStarFromRating(String currentUserRating, ImageView star1, ImageView star2, ImageView star3, ImageView star4, ImageView star5){
        if(currentUserRating.equals("")){       // user hasn't received any rating now
            star1.setVisibility(View.INVISIBLE);
            star2.setVisibility(View.INVISIBLE);
            star3.setVisibility(View.INVISIBLE);
            star4.setVisibility(View.INVISIBLE);
            star5.setVisibility(View.INVISIBLE);
            rating.setText("No rating given");
        }
        else{       // user has rating
            double user_rating = Double.parseDouble(currentUserRating);
            if(user_rating == 5.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_filled);
            }
            else if(user_rating > 4.0 && user_rating < 5.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_half);
            }
            else if(user_rating == 4.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_filled);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating > 3.0 && user_rating < 4.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_half);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating == 3.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_filled);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating > 2.0 && user_rating < 3.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_half);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating == 2.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_filled);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating > 1.0 && user_rating < 2.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_half);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating == 1.0){
                star1.setImageResource(R.drawable.star_filled);
                star2.setImageResource(R.drawable.star_none);
                star3.setImageResource(R.drawable.star_none);
                star4.setImageResource(R.drawable.star_none);
                star5.setImageResource(R.drawable.star_none);
            }
            else if(user_rating > 0 && user_rating < 1.0){
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

    public void showStarFromRater(String rating, ImageView star1, ImageView star2, ImageView star3, ImageView star4, ImageView star5){
        double user_rating = Double.parseDouble(rating);
        if(user_rating == 5.0){
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_filled);
            star3.setImageResource(R.drawable.star_filled);
            star4.setImageResource(R.drawable.star_filled);
            star5.setImageResource(R.drawable.star_filled);
        }
        else if(user_rating == 4.0){
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_filled);
            star3.setImageResource(R.drawable.star_filled);
            star4.setImageResource(R.drawable.star_filled);
            star5.setImageResource(R.drawable.star_none);
        }
        else if(user_rating == 3.0){
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_filled);
            star3.setImageResource(R.drawable.star_filled);
            star4.setImageResource(R.drawable.star_none);
            star5.setImageResource(R.drawable.star_none);
        }
        else if(user_rating == 2.0){
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_filled);
            star3.setImageResource(R.drawable.star_none);
            star4.setImageResource(R.drawable.star_none);
            star5.setImageResource(R.drawable.star_none);
        }
        else if(user_rating == 1.0){
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_none);
            star3.setImageResource(R.drawable.star_none);
            star4.setImageResource(R.drawable.star_none);
            star5.setImageResource(R.drawable.star_none);
        }
    }
}
