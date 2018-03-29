package lix5.ushare;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    private TextView userName, bio, email, mobile, gender, rating;
    private ImageView edit;
    private de.hdodenhof.circleimageview.CircleImageView avatar;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users/");
        userName = findViewById(R.id.name_profile);
        bio = findViewById(R.id.bio_profile);
        email = findViewById(R.id.email_profile);
        mobile = findViewById(R.id.mobile_profile);
        gender = findViewById(R.id.gender_profile);
        rating = findViewById(R.id.rating_profile);
        avatar = findViewById(R.id.avatar_profile);
        toolbar = findViewById(R.id.toolbar);
        edit = findViewById(R.id.edit_profile);

        edit.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mDatabase.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName.setText(dataSnapshot.child("username").getValue(String.class));   //set the name
                bio.setText(dataSnapshot.child("bio").getValue(String.class));    //set the bio
                email.setText(dataSnapshot.child("email").getValue(String.class));  //set the email
                mobile.setText(dataSnapshot.child("phoneNum").getValue(String.class));  //set the mobile
                gender.setText(dataSnapshot.child("sex").getValue(String.class));   //set the gender
                rating.setText(dataSnapshot.child("rating").getValue(String.class));    //set the rating
                Picasso.get().load(dataSnapshot.child("avatar").getValue(String.class)).into(avatar);  //set the avatar
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // error in fetching data
            }
        });
    }

    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
