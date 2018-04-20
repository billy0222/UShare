package lix5.ushare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tuyenmonkey.AutoFillEditText;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private AppCompatButton login;
    private AutoFillEditText email;
    private EditText pw;
    private TextView register;
    private SharedPreferences mPrefs;

    private FirebaseAuth mAuth; //instance of FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        if (mPrefs.getBoolean("is_logged_in", false)) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        email = (AutoFillEditText) findViewById(R.id.email);
        pw = (EditText) findViewById(R.id.password);
        login = (AppCompatButton) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register);
        email.addSuggestions(Arrays.asList("connect.ust.hk", "ust.hk"));

        login.setOnClickListener((v) -> {  //login
            if (loginCheck(email.getText().toString(), pw.getText().toString())) {
                signIn(email.getText().toString(), pw.getText().toString());
            }
        });

        register.setOnClickListener((v) -> {  //register
            startActivity(new Intent(LoginActivity.this, RegActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
    }

    private void signIn(String email, String password) {
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getInstance().getCurrentUser();
                        if (user.isEmailVerified()) {
                            Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            getSharedPreferences("login", MODE_PRIVATE).edit().putBoolean("is_logged_in", true).apply();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Email is not verified, please check your email", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            finish();
                            Intent intent = getIntent();
                            startActivity(intent);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
        // [END sign_in_with_email]
    }

    private boolean loginCheck(String email_, String pw_) {
        boolean haveError = false;

        if (TextUtils.isEmpty(email_)) {
            email.setError("Please enter your email");
            email.requestFocus();
            haveError = true;
        }

        if (TextUtils.isEmpty(pw_)) {
            pw.setError("Please enter your password");
            pw.requestFocus();
            haveError = true;
        }
        return !haveError;
    }
}