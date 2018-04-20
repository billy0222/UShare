package lix5.ushare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RateUserActivity extends AppCompatActivity {

    private ImageView star1, star2, star3, star4, star5;
    private EditText comment;
    private Button submit;
    private int rating = 0;
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database
    private String rating_user_id;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        star1 = findViewById(R.id.star1_rate_user);
        star2 = findViewById(R.id.star2_rate_user);
        star3 = findViewById(R.id.star3_rate_user);
        star4 = findViewById(R.id.star4_rate_user);
        star5 = findViewById(R.id.star5_rate_user);
        comment = findViewById(R.id.comment_rate_user);
        submit = findViewById(R.id.submit_rate_user);
        rating_user_id = getIntent().getStringExtra("rating_user_id");

        star1.setOnClickListener(view -> {
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_none);
            star3.setImageResource(R.drawable.star_none);
            star4.setImageResource(R.drawable.star_none);
            star5.setImageResource(R.drawable.star_none);
            rating = 1;
        });

        star2.setOnClickListener(view -> {
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_filled);
            star3.setImageResource(R.drawable.star_none);
            star4.setImageResource(R.drawable.star_none);
            star5.setImageResource(R.drawable.star_none);
            rating = 2;
        });

        star3.setOnClickListener(view -> {
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_filled);
            star3.setImageResource(R.drawable.star_filled);
            star4.setImageResource(R.drawable.star_none);
            star5.setImageResource(R.drawable.star_none);
            rating = 3;
        });

        star4.setOnClickListener(view -> {
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_filled);
            star3.setImageResource(R.drawable.star_filled);
            star4.setImageResource(R.drawable.star_filled);
            star5.setImageResource(R.drawable.star_none);
            rating = 4;
        });

        star5.setOnClickListener(view -> {
            star1.setImageResource(R.drawable.star_filled);
            star2.setImageResource(R.drawable.star_filled);
            star3.setImageResource(R.drawable.star_filled);
            star4.setImageResource(R.drawable.star_filled);
            star5.setImageResource(R.drawable.star_filled);
            rating = 5;
        });

        submit.setOnClickListener(view -> {
            if (rating == 0) {
                new AlertDialog.Builder(RateUserActivity.this).setMessage("Please rate the user! [1 star - 5 stars]").show();
            } else {
                Rating newRating = new Rating(String.valueOf(rating), comment.getText().toString());
                mDatabase.child("rating").child(rating_user_id).child(mAuth.getUid()).setValue(newRating);
                mDatabase.child("rating").child(rating_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int numOfRating = (int) dataSnapshot.getChildrenCount();
                        mDatabase.child("users").child(rating_user_id).child("rating").addListenerForSingleValueEvent(new ValueEventListener() {
                            double ratingAfter = 0;

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                if (dataSnapshot2.getValue(String.class).equals("")) {
                                    ratingAfter = (0.0 + rating);
                                } else {
                                    ratingAfter = ((Double.valueOf(dataSnapshot2.getValue(String.class)) * (numOfRating - 1) + rating) / numOfRating);
                                }
                                mDatabase.child("users").child(rating_user_id).child("rating").setValue(String.valueOf(ratingAfter));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Toast.makeText(getApplicationContext(), "Rate user successful", Toast.LENGTH_SHORT).show();
                finish();
            }

        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

}
