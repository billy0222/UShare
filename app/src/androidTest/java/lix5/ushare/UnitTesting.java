package lix5.ushare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class UnitTesting {
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    @Test
    public void databaseInsertionTest() throws Exception{
        mDatabase = FirebaseDatabase.getInstance().getReference();
        User user = new User("Test001", "test001@connect.ust.hk", "123456");    // Make a fake user

        mDatabase.child("users").child("111111").setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {  // Insert user into database
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDatabase.child("users").child("111111").child("username").addListenerForSingleValueEvent(new ValueEventListener() {    // Read the user name
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        assertEquals("Test001", dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Test
    public void databaseUpdateTest() throws Exception{
        mDatabase = FirebaseDatabase.getInstance().getReference();
        User user = new User("Test002", "test002@connect.ust.hk", "123456");    // Make a fake user

        mDatabase.child("users").child("222222").setValue(user).addOnCompleteListener(task -> { // Insert user into database
            mDatabase.child("users").child("222222").child("username").setValue("NewTest002");  // Rename user to "NewTest002"
            mDatabase.child("users").child("222222").child("username").addListenerForSingleValueEvent(new ValueEventListener() {    // Read the user name
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    assertEquals("NewTest002", dataSnapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });
    }

    @Test
    public void databaseRemovalTest() throws Exception{
        mDatabase = FirebaseDatabase.getInstance().getReference();
        User user = new User("Test003", "test003@connect.ust.hk", "123456");    // Make a fake user

        mDatabase.child("users").child("333333").setValue(user).addOnCompleteListener(task -> { // Insert user into database
            mDatabase.child("users").child("333333").removeValue(); // Remove that user from database
            mDatabase.child("users").child("333333").addListenerForSingleValueEvent(new ValueEventListener() {  //find the user
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    assertEquals(false, dataSnapshot.exists());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });
    }
}
