package lix5.ushare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Kevin on 31/1/2018.
 */

public class RegActivity extends AppCompatActivity {

    private EditText name, email, pw, pwCheck;
    private AppCompatButton btn;
    private TextView backToLogin;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        name = findViewById(R.id.input_name);
        email = findViewById(R.id.input_email);
        pw = findViewById(R.id.input_password);
        pwCheck = findViewById(R.id.input_password_check);
        btn = findViewById(R.id.btn_signup);
        backToLogin = findViewById(R.id.link_login);

        btn.setOnClickListener((v) -> {     //register
            if(regFormCheck(name.getText().toString(), email.getText().toString(), pw.getText().toString(), pwCheck.getText().toString())){
                createAccount(name.getText().toString(), email.getText().toString(), pw.getText().toString());
            }
            else{
                Log.i("Error", "Registration error");// Registration error
            }
        });

        backToLogin.setOnClickListener((v) -> {     //back to login page
            startActivity(new Intent(RegActivity.this, LoginActivity.class));
            finish();
        });

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null && !user.isEmailVerified()){
                // User is signed in
                sendVerificationEmail();
            }
            else{
                // user is signed out
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    protected void onStart(){
        super.onStart();
    }

    private boolean regFormCheck(String name_, String email_, String password_, String passwordCheck_){
        boolean haveError = false;
        if(TextUtils.isEmpty(name_)){
            name.setError("Please enter your name");
            name.requestFocus();
            haveError = true;
        }
        if(TextUtils.isEmpty(email_)){
            email.setError("Please enter your email");
            email.requestFocus();
            haveError = true;
        }
        if(TextUtils.isEmpty(password_)){
            pw.setError("Please enter your password");
            pw.requestFocus();
            haveError = true;
        }
        if(TextUtils.isEmpty(passwordCheck_)){
            pwCheck.setError("Please enter your password");
            pwCheck.requestFocus();
            haveError = true;
        }
        if(!password_.equals(passwordCheck_)){
            pwCheck.setError("Password does not match, please re-enter your password again");
            pwCheck.requestFocus();
            haveError = true;
        }
        if(!isValidEmail(email_)){
            email.setError("Email is not valid");
            email.requestFocus();
            haveError = true;
        }
        if(!email_.endsWith("@connect.ust.hk") && !email_.endsWith("@ust.hk")){
            email.setError("Email must end with @connect.ust.hk or @ust.hk");
            email.requestFocus();
            haveError = true;
        }
        return !haveError;
    }

    private void createAccount(String name_, String email_, String password_){
        mAuth.createUserWithEmailAndPassword(email_, password_).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                // successfully account created
                // now the AuthStateListener runs the onAuthStateChanged callback
                User user = new User(name_, email_, password_);
                mDatabase.child("users").child(mAuth.getUid()).setValue(user);
            }
            else{
                try{
                    throw task.getException();
                }
                catch(FirebaseAuthWeakPasswordException weakPW){
                    pw.setError("Password must be at least 6 characters");
                    pw.requestFocus();
                }
                catch(FirebaseAuthUserCollisionException existEmail){
                    email.setError("Email was already being used");
                    email.requestFocus();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendVerificationEmail(){
        FirebaseUser user = mAuth.getCurrentUser();

        user.sendEmailVerification().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                // email sent
                Toast.makeText(getApplicationContext(), "Verification email has been sent to your email address", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(RegActivity.this, LoginActivity.class));
                finish();
            }
            else{
                // email not sent
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
