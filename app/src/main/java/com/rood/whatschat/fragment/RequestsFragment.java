package com.rood.whatschat.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.ServerValue;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rood.whatschat.R;
import com.rood.whatschat.model.Accept;
import com.rood.whatschat.model.Conv;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    private DatabaseReference friendRequestUser;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference userDatabase;
    private DatabaseReference mFriendDatabase;

    private FirebaseRecyclerAdapter adapter;


    private FirebaseAuth mAuth;
    private String currentUser;

    //Views
    private RecyclerView recyclerViewAccept;
    private View mRequestView;



    public RequestsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRequestView =  inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        userDatabase.keepSynced(true);

        friendRequestUser = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(currentUser);
        friendRequestUser.keepSynced(true);

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friend");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        recyclerViewAccept = mRequestView.findViewById(R.id.request_fragment_recycler_view_accept);
        recyclerViewAccept.setLayoutManager(linearLayoutManager);
        recyclerViewAccept.setHasFixedSize(true);

        fetch();

        return mRequestView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    private void fetch() {

        Query query = friendRequestUser.getRef();

        // BUILDER
        FirebaseRecyclerOptions<Accept> options = new FirebaseRecyclerOptions.Builder<Accept>().setQuery(query, new SnapshotParser<Accept>() {

            @NonNull
            @Override
            public Accept parseSnapshot(@NonNull DataSnapshot snapshot) {
                return new Accept(snapshot.child("request_type").getValue().toString());
            }
        }).build();


        adapter = new FirebaseRecyclerAdapter<Accept, AcceptViewHolder>(options){

            @NonNull
            @Override
            public AcceptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_single_layout, parent, false);
                return new AcceptViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final AcceptViewHolder holder, int position, @NonNull Accept model) {
                // Set Info using User DB
                final String user_id = getRef(position).getKey();
                final String request_type = model.getRequest_type();

                userDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String textName = snapshot.child("name").getValue().toString();
                        String textStatus = snapshot.child("status").getValue().toString();
                        String thumbUrl = snapshot.child("thumb_image").getValue().toString();

                        holder.setName(textName);
                        holder.setStatus(textStatus);
                        holder.setImage(thumbUrl);

                        if (request_type.equals("sent")){
                            holder.setButtons(request_type);
                        }

                        final String text_accept_button = holder.acceptButton.getText().toString();  // Accept or Cancel

                        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (text_accept_button.equals("Accept")){

                                    //ACCEPTED, Add Friend
                                    mFriendDatabase.child(mAuth.getUid()).child(user_id).child("date").setValue(ServerValue.TIMESTAMP);
                                    mFriendDatabase.child(user_id).child(mAuth.getUid()).child("date").setValue(ServerValue.TIMESTAMP);

                                    // Removed Requests
                                    mFriendReqDatabase.child(mAuth.getUid()).child(user_id).removeValue();
                                    mFriendReqDatabase.child(user_id).child(mAuth.getUid()).removeValue();

                                }

                                if (text_accept_button.equals("Cancel")){

                                    // USER CANCELLED THE REQUEST
                                    mFriendReqDatabase.child(mAuth.getUid()).child(user_id).child("request_type").removeValue();
                                    mFriendReqDatabase.child(user_id).child(mAuth.getUid()).child("request_type").removeValue();

                                }
                            }
                        });

                        holder.declineButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // DECLINED, REMOVE REQUESTS
                                mFriendReqDatabase.child(mAuth.getUid()).child(user_id).child("request_type").removeValue();
                                mFriendReqDatabase.child(user_id).child(mAuth.getUid()).child("request_type").removeValue();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        };

        recyclerViewAccept.setAdapter(adapter);

    }

    public static class AcceptViewHolder extends RecyclerView.ViewHolder{

        View root;
        Button acceptButton;
        Button declineButton;

        public AcceptViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView;
            declineButton = itemView.findViewById(R.id.request_fragment_button_decline);
            acceptButton = itemView.findViewById(R.id.request_fragment_button);

        }

        public void setButtons(String type) {
            declineButton.setVisibility(View.INVISIBLE);
            acceptButton.setText("Cancel");
        }
        public void setStatus(final String status) {
            TextView mStatus = itemView.findViewById(R.id.request_fragment_status);
            mStatus.setText(status);
        }
        public void setName(final String name) {
            TextView acceptName = itemView.findViewById(R.id.request_fragment_name);
            acceptName.setText(name);
        }
        public void setImage(final String image) {
            CircleImageView mImage = (CircleImageView) itemView.findViewById(R.id.request_fragment_image);
            //picasso image downloading and
            Picasso.get().load(image).placeholder(R.drawable.harry).into(mImage);
        }
    }
}