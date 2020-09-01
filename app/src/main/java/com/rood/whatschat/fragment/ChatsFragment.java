package com.rood.whatschat.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rood.whatschat.R;
import com.rood.whatschat.UserActivity;
import com.rood.whatschat.model.Conv;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter adapter;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;


    public ChatsFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView =  inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = mMainView.findViewById(R.id.conv_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mConvDatabase.keepSynced(true);

        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        // CALL for Recycler View
        fetch();

        // Inflate the layout for this fragment
        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.startListening();
    }

    /////////////   Firebase Recycler Adapter View Holder     //////////////
    public void fetch(){
        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        // BUILDER
        FirebaseRecyclerOptions<Conv> options = new FirebaseRecyclerOptions.Builder<Conv>().setQuery(conversationQuery, new SnapshotParser<Conv>() {
            @NonNull
            @Override
            public Conv parseSnapshot(@NonNull DataSnapshot snapshot) {

                return new Conv( (Long)snapshot.child("timestamp").getValue() );
            }
        }).build();


        // ADAPTER
        adapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options){

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);
                return new ConvViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull Conv model) {

                //SET Last Message
                String user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        String textMessage = snapshot.child("message").getValue().toString();

                        holder.setMessageViewView(textMessage);

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

                // SET Name and Image
                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String textName = snapshot.child("name").getValue().toString();
                        String thumbUrl = snapshot.child("thumb_image").getValue().toString();

                        holder.setNameView(textName);
                        holder.setThumbView(thumbUrl);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        };

        mConvList.setAdapter(adapter);

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout root;

        public TextView nameView;
        public TextView messageView;
        public CircleImageView thumbView;

        public ConvViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.user_single_root);
            nameView = itemView.findViewById(R.id.user_single_name);
            messageView = itemView.findViewById(R.id.user_single_status);
            thumbView = itemView.findViewById(R.id.user_single_image);

        }

        public void setNameView(String s){
            nameView.setText(s);
        }

        public void setMessageViewView(String s) {
            messageView.setText(s);
        }

        public void setThumbView(String s){
            Picasso.get().load(s).placeholder(R.drawable.harry).networkPolicy(NetworkPolicy.OFFLINE).into(thumbView);
        }
    }
}