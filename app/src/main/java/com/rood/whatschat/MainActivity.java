package com.rood.whatschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rood.whatschat.adapter.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private MaterialToolbar topAppBar;

    private TabLayout tabLayout;

    private ViewPager2 mainViewPager;
    private SectionsPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();


        // set app bar
        topAppBar = findViewById(R.id.topAppBar);

        // populate top app bar
        topAppBar.setTitle("Whatschat");
        topAppBar.setNavigationIcon(R.drawable.ic_menu);

        // to place menu menu we need to set SupportActionBar and implement onCreateOptionsMenu
        setSupportActionBar(topAppBar);


        // Tabs Titles

        // set VIEW-PAGER2 view
        mainViewPager = findViewById(R.id.main_tabPager);

        // set TAB-LAYOUT(material) view
        tabLayout = findViewById(R.id.main_tabLayout);

        // create PAGER-ADAPTER Object
        pagerAdapter = new SectionsPagerAdapter(this);

        // Set PagerADAPTER ie. bind to ViewPager
        mainViewPager.setAdapter(pagerAdapter);

        // TabLayoutMediator bind TabLayout & ViewPager2
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, mainViewPager, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText("REQUESTS");
                        break;

                    case 1:
                        tab.setText("CHATS");
                        break;

                    case 2:
                        tab.setText("FRIENDS");
                        break;
                }
            }
        });

        // Finally attach
        tabLayoutMediator.attach();


    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            sendToStart();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId()==R.id.main_logout_menu) {
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            sendToStart();
        }

        if (item.getItemId() == R.id.main_settings){
            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
        }

        if (item.getItemId() == R.id.main_all_users){
            Intent userIntent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(userIntent);
        }

        return true;
    }

    private void sendToStart() {
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}


/*
1. User first land on this activity
2. app checks whether user is logged in or not
3. if not redirect to StartActivity

 */