package com.rood.whatschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private MaterialToolbar topAppbar;
    private TextInputLayout mStatusView;
    private Button mStatusBtn;

    DatabaseReference mDatabase;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        topAppbar = findViewById(R.id.topAppBar);  // app_bar_layout
        setSupportActionBar(topAppbar);

        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("status");

        mStatusView = findViewById(R.id.status_update_status);
        mStatusBtn = findViewById(R.id.status_update_btn);

        // Populate Text
        String currentStatus = getIntent().getStringExtra("current_status");
        mStatusView.getEditText().setText(currentStatus);
        // place cursor at end
        int curStatusSize = mStatusView.getEditText().getText().length();
        mStatusView.getEditText().setSelection(curStatusSize);

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pd = new ProgressDialog(StatusActivity.this);
                pd.setTitle("Updating Status");
                pd.setMessage("Please wait while status is updating..");
                pd.show();

                String new_status = mStatusView.getEditText().getText().toString();
                mDatabase.setValue(new_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        if (task.isSuccessful())
                            Toast.makeText(StatusActivity.this, "Status Updated", Toast.LENGTH_LONG).show();
                        else{
                            pd.hide();
                            Toast.makeText(StatusActivity.this, "Sorry something went wrong!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });



    }
}