package lix5.ushare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database
    private StorageReference mStorage;   //instance of Storage

    private TextView name, phone, gender;
    private EditText bio;
    private Button btnChoose, btnSubmit;
    private ImageView uploaded_avatar;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_profile_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        name = findViewById(R.id.name_profileEdit);
        bio = findViewById(R.id.bio_profileEdit);
        phone = findViewById(R.id.phone_profileEdit);
        gender = findViewById(R.id.gender_profileEdit);
        btnChoose = findViewById(R.id.btnUploadAvatar_profileEdit);
        btnSubmit = findViewById(R.id.btnSubmit_profileEdit);
        uploaded_avatar = findViewById(R.id.uploaded_avatar_profileEdit);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users/");
        mStorage = FirebaseStorage.getInstance().getReference();

        mDatabase.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child("username").getValue(String.class));
                bio.setText(dataSnapshot.child("bio").getValue(String.class));
                phone.setText(dataSnapshot.child("phoneNum").getValue(String.class));
                gender.setText(dataSnapshot.child("sex").getValue(String.class));
                Picasso.get().load(dataSnapshot.child("avatar").getValue(String.class)).into(uploaded_avatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //data fetch error
            }
        });
    }

    public void chooseImage(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                uploaded_avatar.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void submit(View view){
        if(filePath != null) {          //Have Avatar uploaded
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Please wait...");
            progressDialog.show();

            StorageReference ref = mStorage.child("images/" + mAuth.getUid());
            Task upload = ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        if(progressDialog.getOwnerActivity() != null && !progressDialog.getOwnerActivity().isFinishing())
                            progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Your profile has edited", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        if(progressDialog.getOwnerActivity() != null && !progressDialog.getOwnerActivity().isFinishing())
                            progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploading avatar..." + (int) progress + "%");
                    });
            while(!upload.isComplete()) {
            }
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    mDatabase.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User temp = new User(name.getText().toString(), dataSnapshot.child("email").getValue(String.class),
                                    dataSnapshot.child("password").getValue(String.class),
                                    uri.toString(), gender.getText().toString(), bio.getText().toString(),
                                    dataSnapshot.child("rating").getValue(String.class), phone.getText().toString());
                            Map<String, Object> postValues = temp.toMapHaveAvatar();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(mAuth.getUid(), postValues);
                            mDatabase.updateChildren(childUpdates);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // data fetch error
                        }
                    });
            });
        }
        else{       // No Avatar Uploaded
            mDatabase.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User temp = new User(name.getText().toString(), dataSnapshot.child("email").getValue(String.class),
                            dataSnapshot.child("password").getValue(String.class), dataSnapshot.child("avatar").getValue(String.class),
                            gender.getText().toString(), bio.getText().toString(),
                            dataSnapshot.child("rating").getValue(String.class), phone.getText().toString());
                    Map<String, Object> postValues = temp.toMapHaveAvatar();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(mAuth.getUid(), postValues);
                    mDatabase.updateChildren(childUpdates);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // data fetch error
                }
            });
            Toast.makeText(ProfileEditActivity.this, "Your profile has edited", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
