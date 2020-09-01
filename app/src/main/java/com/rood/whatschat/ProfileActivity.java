package com.rood.whatschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TimeUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private StorageReference mStorage;
    private FirebaseUser mAuth;

    private ImageView mProfileImage;
    private TextView mDisplayName;
    private TextView mTotalFriends;
    private TextView mStatus;
    private Button mSendFriendReqBtn;
    private Button mDeclineReqBtn;
    DateFormat dateFormat;
    Calendar calendar;

    String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance().getCurrentUser();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friend");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        //mStorage = FirebaseStorage.getInstance().getReference().child("profile_image");

        dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        calendar = Calendar.getInstance();

        //String dateTime = calendar.getTime().toString();

        mProfileImage = findViewById(R.id.profile_image);
        mDisplayName = findViewById(R.id.profile_display_name);
        mStatus = findViewById(R.id.profile_status);
        mTotalFriends = findViewById(R.id.profile_total_friends);
        mSendFriendReqBtn = findViewById(R.id.profile_send_request_btn);
        mDeclineReqBtn = findViewById(R.id.profile_decline_request_btn);

        final String user_id = getIntent().getStringExtra("user_id");
        String user_name = getIntent().getStringExtra("user_name");
        String user_status = getIntent().getStringExtra("user_status");
        String user_dp_url = getIntent().getStringExtra("user_dp_url");

        mDisplayName.setText(user_name);
        mStatus.setText(user_status);
        Picasso.get().load(user_dp_url).placeholder(R.drawable.harry).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage);

        current_state = "not friend";

        // Friend_req ->
        mFriendReqDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // REQUEST TYPE NOT EXISTS
                if (!snapshot.child(mAuth.getUid()).child(user_id).child("request_type").exists()){

                    // 2 possibilities, either both are FRIEND or NOT FRIEND.
                    mFriendDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            // CASE: FRIEND object of THESE TWO USERS EXISTS
                            if (snapshot.child(mAuth.getUid()).child(user_id).exists()){
                                current_state = "friends";
                                // change button to REMOVE
                                mSendFriendReqBtn.setText("Remove Friend");

                                // HIDE DECLINE BUTTON
                                mDeclineReqBtn.setVisibility(View.INVISIBLE);
                            }

                            // CASE: NOT FRIEND
                            else {
                                current_state = "not friend";
                                mSendFriendReqBtn.setText("Send Request");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }


                // REQUEST TYPE EXISTS
                if ( snapshot.child(mAuth.getUid()).child(user_id).child("request_type").exists() ){

                    // REQ. TYPE SENT
                    if (snapshot.child(mAuth.getUid()).child(user_id).child("request_type").getValue().equals("sent")) {
                        current_state = "request sent";
                        mSendFriendReqBtn.setText("Cancel Request");
                    }

                    // REQ. TYPE RECEIVED
                    if (snapshot.child(mAuth.getUid()).child(user_id).child("request_type").getValue().equals("received")){
                        current_state = "request received";
                        mSendFriendReqBtn.setText("Accept Request");
                        mDeclineReqBtn.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSendFriendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (current_state.equals("not friend")){
                    // USER HAS SENT REQUEST

                    mFriendReqDatabase.child(mAuth.getUid()).child(user_id).child("request_type").setValue("sent");
                    mFriendReqDatabase.child(user_id).child(mAuth.getUid()).child("request_type").setValue("received");

                    mDeclineReqBtn.setVisibility(View.INVISIBLE);

                    // CREATE NOTIFICATION IN FIREBASE
                    HashMap<String, String> notifMap = new HashMap<>();
                    notifMap.put("type", "request");
                    notifMap.put("from", mAuth.getUid());

                    mNotificationDatabase.child(user_id).push().setValue(notifMap);

                }

                if (current_state.equals("request sent")) {
                    // USER CANCELLED THE REQUEST

                    mFriendReqDatabase.child(mAuth.getUid()).child(user_id).child("request_type").removeValue();
                    mFriendReqDatabase.child(user_id).child(mAuth.getUid()).child("request_type").removeValue();
                }

                if (current_state.equals("request received")){

                    String time = calendar.getTime().toString();

                    // Add Friend
                    mFriendDatabase.child(mAuth.getUid()).child(user_id).child("date").setValue(ServerValue.TIMESTAMP);
                    mFriendDatabase.child(user_id).child(mAuth.getUid()).child("date").setValue(ServerValue.TIMESTAMP);

                    // Removed Requests
                    mFriendReqDatabase.child(mAuth.getUid()).child(user_id).removeValue();
                    mFriendReqDatabase.child(user_id).child(mAuth.getUid()).removeValue();

                }

                if (current_state.equals("friends")){
                    // USER CLICKED ON REMOVE FRIEND
                    mFriendDatabase.child(mAuth.getUid()).child(user_id).removeValue();
                    mFriendDatabase.child(user_id).child(mAuth.getUid()).removeValue();

                }
            }
        });

        // DECLINE REQUEST
        mDeclineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // REMOVE REQUESTS
                mFriendReqDatabase.child(mAuth.getUid()).child(user_id).child("request_type").removeValue();
                mFriendReqDatabase.child(user_id).child(mAuth.getUid()).child("request_type").removeValue();
            }
        });

        mSendFriendReqBtn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("Cancel Request")){
                    mSendFriendReqBtn.setBackgroundColor(getResources().getColor(android.R.color.white));
                    mSendFriendReqBtn.setTextColor(getResources().getColor(android.R.color.black));
                } else {
                    mSendFriendReqBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mSendFriendReqBtn.setTextColor(getResources().getColor(android.R.color.white));
                }
            }
        });

    }
}