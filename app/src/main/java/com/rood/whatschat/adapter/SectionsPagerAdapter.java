package com.rood.whatschat.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rood.whatschat.fragment.ChatsFragment;
import com.rood.whatschat.fragment.FriendsFragment;
import com.rood.whatschat.fragment.RequestsFragment;

public class SectionsPagerAdapter extends FragmentStateAdapter {


    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                return new RequestsFragment();
            case 1:
                return new ChatsFragment();
            case 2:
                return new FriendsFragment();
            default:
                return null;
        }

    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
