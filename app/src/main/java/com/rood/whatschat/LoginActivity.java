package com.rood.whatschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout email;
    private TextInputLayout password;
    private Button loginBtn;

    private FirebaseAuth mAuth;
    private MaterialToolbar topAppBar;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.log_email);
        password = findViewById(R.id.log_password);
        loginBtn = findViewById(R.id.log_login_btn);
        mAuth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(this);

        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for back to parent activity

        //topAppBar.setTitle("Login");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textEmail = email.getEditText().getText().toString();
                String textPassword = password.getEditText().getText().toString();

                pd.setTitle("Logging in");
                pd.setMessage("Please wait while we check your credentials..");
                pd.setCanceledOnTouchOutside(false);
                pd.show();

                login_user(textEmail, textPassword);
            }
        });
    }

    private void login_user(String textEmail, String textPassword) {
        mAuth.signInWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LOGIN", "signInWithEmail:success");

                    pd.dismiss();

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);

                    // Don't want user to go back to Start Activity (NOTE: Login Activity will be popped)  when click back button
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(mainIntent);
                    finish();


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LOGIN", "signInWithEmail:failure", task.getException());

                    pd.hide();

                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}