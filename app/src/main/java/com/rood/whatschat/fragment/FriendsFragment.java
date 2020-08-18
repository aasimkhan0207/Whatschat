package com.rood.whatschat.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rood.whatschat.ChatActivity;
import com.rood.whatschat.ProfileActivity;
import com.rood.whatschat.R;
import com.rood.whatschat.UserActivity;
import com.rood.whatschat.model.Friend;
import com.rood.whatschat.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private View mMainView;
    private RecyclerView mFriendsList;
    private FirebaseRecyclerAdapter adapter;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        // FRAGMENT VIEW
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = mMainView.findViewById(R.id.friends_list_recycler_view);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mFriendsList.setHasFixedSize(true);

        fetch();

        return mMainView;
    }

    private void fetch() {
        Query query_friend = FirebaseDatabase.getInstance().getReference().child("Friend").child(mAuth.getUid());

        final DatabaseReference users_ref = FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>().setQuery(query_friend, new SnapshotParser<Friend>() {
            @NonNull
            @Override
            public Friend parseSnapshot(@NonNull DataSnapshot snapshot) {

                String date = snapshot.child("date").getValue().toString();

                return new Friend(snapshot.child("date").getValue().toString());
            }
        }).build();


        adapter = new FirebaseRecyclerAdapter<Friend, FriendViewHolder>(options){

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_single_layout, parent, false);

                return new FriendViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder holder, int position, @NonNull Friend model) {

                holder.setDateView(model.getDate());

                // SET NAME AND IMAGE
                final String user_id = getRef(position).getKey();

                users_ref.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String friend_name = snapshot.child("name").getValue().toString();
                        String friend_thumb = snapshot.child("thumb_image").getValue().toString();

                        holder.setNameView(friend_name);
                        holder.setImageView(friend_thumb);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // Friend ONCLICK
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("CLICKED",user_id);

                        CharSequence[] options = {"Profile, Chat"};

                        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getContext());

                        dialogBuilder.setTitle("Select Option");

                        dialogBuilder.setNegativeButton("Profile", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sendToProfile(user_id);
                            }
                        });

                        dialogBuilder.setPositiveButton("Chat", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", user_id);
                                startActivity(chatIntent);
                            }
                        });

                        dialogBuilder.show();
                    }
                });

            }
        };

        mFriendsList.setAdapter(adapter);
    }

    private void sendToProfile(final String user_id) {

        String user_name = null;
        String user_status = null;
        String user_dp_url = null;

        mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String user_name = snapshot.child("name").getValue().toString();
                String user_status = snapshot.child("status").getValue().toString();
                String user_dp_url = snapshot.child("thumb_image").getValue().toString();

                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                profileIntent.putExtra("user_id",user_id);
                profileIntent.putExtra("user_name",user_name);
                profileIntent.putExtra("user_status", user_status);
                profileIntent.putExtra("user_dp_url", user_dp_url);

                startActivity(profileIntent);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

    // ViewHolder Class
    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout root; // for item onclick or set

        public TextView nameView;
        public TextView dateView;
        public CircleImageView imageView;


        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.friend_single_root);

            dateView = itemView.findViewById(R.id.friend_single_date);
            nameView = itemView.findViewById(R.id.friend_single_name);
            imageView = itemView.findViewById(R.id.friend_single_image);

        }

        public void setDateView(String s){
            dateView.setText(s);
        }

        public void setNameView(String s){
            nameView.setText(s);
        }

        public void setImageView(String s){
            Picasso.get().load(s).into(imageView);
        }

    }
}