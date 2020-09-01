package com.rood.whatschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.rood.whatschat.model.User;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {

    private MaterialToolbar topAppbar;
    private RecyclerView mUsersList;

    private FirebaseRecyclerAdapter adapter;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getUid();

        // App Bar
        topAppbar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set Recycler View
        mUsersList = findViewById(R.id.user_recycler_view);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mUsersList.setHasFixedSize(true);
        fetch();

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    // Fetch():  RecyclerView Adapter (Firebase UI)
    private void fetch(){
        Query query = FirebaseDatabase.getInstance().getReference().child("Users");

        // RecyclerOption Builder
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, new SnapshotParser<User>() {
            @NonNull
            @Override
            public User parseSnapshot(@NonNull DataSnapshot snapshot) {

                return new User(snapshot.child("name").getValue().toString(),
                        snapshot.child("status").getValue().toString(),
                        snapshot.child("image").getValue().toString(),
                        snapshot.child("thumb_image").getValue().toString());

            }
        }).build();

        // RecyclerView Adapter  (FirebaseRecyclerAdapter)
        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options){

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                // Create view from layout
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, final int position, @NonNull User model) {


                // Get key of item
                final String user_id = getRef(position).getKey();
                final String user_status = model.getStatus();
                final String user_dp_url = model.getImage();
                final String user_name = model.getName();

//                if (user_id.equals(currentUserId)){
//                    holder.root.setVisibility(View.GONE);
//                }


                holder.setNameView(model.getName());
                holder.setStatusView(model.getStatus());
                //holder.setImageView(model.getImage());
                holder.setThumbView(model.getThumb_image());

                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UserActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        profileIntent.putExtra("user_name",user_name);
                        profileIntent.putExtra("user_status", user_status);
                        profileIntent.putExtra("user_dp_url", user_dp_url);
                        startActivity(profileIntent);

                    }
                });

            }

        };
        // SET ADAPTER
        mUsersList.setAdapter(adapter);
    };

    // ViewHolder Class
    public static class UserViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout root; // for item onclick or set

        public TextView nameView;
        public TextView statusView;
        public ImageView imageView;
        public CircleImageView thumbView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.user_single_root);
            nameView = itemView.findViewById(R.id.user_single_name);
            statusView = itemView.findViewById(R.id.user_single_status);
            //imageView = itemView.findViewById(R.id.user_single_image);
            thumbView = itemView.findViewById(R.id.user_single_image);

        }

        public void setNameView(String s){
            nameView.setText(s);
        }

        public void setStatusView(String s) {
            statusView.setText(s);
        }

        public void setThumbView(String s){
            Picasso.get().load(s).placeholder(R.drawable.harry).networkPolicy(NetworkPolicy.OFFLINE).into(thumbView);
        }
    }
}


