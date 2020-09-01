package com.rood.whatschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ServerValue;
import com.rood.whatschat.adapter.MessageAdapter;
import com.rood.whatschat.model.Message;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private MaterialToolbar topAppbar;
    private String mChatUserId;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private ImageButton mSendBtn;
    private EditText mMessageView;
    private Calendar calendar;
    private RecyclerView mMessagesList;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLinearLayout;

    private int itemPos;
    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        calendar = Calendar.getInstance();

        mSendBtn = findViewById(R.id.chat_send_btn);
        mMessageView = findViewById(R.id.chat_message_view);
        mMessagesList = findViewById(R.id.chat_recycler_view);
        mSwipeRefreshLayout = findViewById(R.id.chat_swipe_refresh);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mChatUserId = getIntent().getStringExtra("user_id");

        topAppbar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppbar);

        final ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle(mChatUserId);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        //custom bar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View custom_action_bar = inflater.inflate(R.layout.chat_custom_bar, null);
        
        // Set
        actionBar.setCustomView(custom_action_bar);

        ////////////////////////////   Chat Firebase  /////////////////
        mDatabase.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if ( !snapshot.hasChild(mChatUserId)){

                    // key : val = content
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    // path : content-map
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+ mCurrentUserId + "/" + mChatUserId, chatAddMap);
                    chatUserMap.put("Chat/"+ mChatUserId + "/" + mCurrentUserId, chatAddMap);

                    mDatabase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if(error != null){

                                Log.d("CHAT_LOG", error.getMessage().toString());

                            }

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //////////////////////////////  MESSAGE RETRIEVAL   /////////////////////////////////////////////////////

        /* Recycler View / Adapter   */

        // Message ArrayList
        messages = new ArrayList<>();

        // Set Layout Manager
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setLayoutManager(mLinearLayout);

        // Message Adapter
        messageAdapter = new MessageAdapter(messages);

        // Set Adapter
        mMessagesList.setAdapter(messageAdapter);

        // Load Messages
        loadMessages();


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


        //Get Name
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("Users").child(mChatUserId).child("name").getValue().toString();
                String status = snapshot.child("Users").child(mChatUserId).child("status").getValue().toString();
                String imageUrl = snapshot.child("Users").child(mChatUserId).child("thumb_image").getValue().toString();

                TextView custom_bar_title_view = custom_action_bar.findViewById(R.id.custom_bar_title);
                CircleImageView custom_bar_img_view = custom_action_bar.findViewById(R.id.chat_custom_image);

                custom_bar_title_view.setText(name);
                Picasso.get().load(imageUrl).into(custom_bar_img_view);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // SEND
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mMessageView.getText().toString();
                if (!TextUtils.isEmpty(message))
                    sendMessage(message);
            }
        });

        mMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = mMessageView.getText().toString();

                if (!TextUtils.isEmpty(text))
                    mSendBtn.setAlpha((float) 1.0);
                else
                    mSendBtn.setAlpha((float) 0.5);
            }
        });


        // Swipe Refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // inc. pageCount
                mCurrentPage++;

                itemPos = 0;

                // Load Message
                loadMoreMessages();

            }
        });
    }

    // called on swipe refresh
    private void loadMoreMessages() {

        DatabaseReference messageRef = mDatabase.child("Messages").child(mCurrentUserId).child(mChatUserId);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Message message = snapshot.getValue(Message.class);
                String messageKey = snapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messages.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }

                if (itemPos == 1){

                    mLastKey = messageKey;
                }

                messageAdapter.notifyDataSetChanged();

                // Scroll to Bottom/Last
                //mMessagesList.scrollToPosition(messages.size()-1);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadMessages() {

        DatabaseReference messageRef = mDatabase.child("Messages").child(mCurrentUserId).child(mChatUserId);

        Query messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD * mCurrentPage);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Called when New child is Added wiz new Message (also called once initially for each child node)
                // Snapshot = new child node
                Message message = snapshot.getValue(Message.class);

                itemPos++;

                if (itemPos == 1){
                    String messageKey = snapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messages.add(message);

                messageAdapter.notifyDataSetChanged();

                // Scroll to Bottom/Last
                mMessagesList.scrollToPosition(messages.size()-1);

                mSwipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(String message) {

        mMessageView.setText("");
        hideKeyboard();

        String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUserId;
        String chat_user_ref = "Messages/" + mChatUserId + "/" + mCurrentUserId;

        // Get Push ID
        DatabaseReference messagePushRef = mDatabase.child("Messages").child(mCurrentUserId).child(mChatUserId).push();
        String pushId = messagePushRef.getKey();

        String time = calendar.getTime().toString();

        // HashMap of the Message
        Map<String, Object> messageMap = new HashMap();
        messageMap.put("time", ServerValue.TIMESTAMP);
        messageMap.put("message", message);
        messageMap.put("from",mCurrentUserId);

        // HashMap , put message in the Firebase,    .put(Reference-path, hash-map)
        Map<String, Object> messageUserMap = new HashMap();

        messageUserMap.put(current_user_ref + "/" + pushId, messageMap);
        messageUserMap.put(chat_user_ref + "/" + pushId, messageMap);

        mDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null)
                    Log.e("CHAT LOG", error.getMessage().toString());
            }
        });


        // Update Chat TimeStamp in Firebase
        mDatabase.child("Chat").child(mCurrentUserId).child(mChatUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);
        mDatabase.child("Chat").child(mChatUserId).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}