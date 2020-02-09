package com.example.bolopakistan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {


    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int i)
    {
        switch (i)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
//
//            case 1:
//                GroupsFragment groupsFragment = new GroupsFragment();
//                return groupsFragment;

            case 1:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            default:
                return null;
        }
    }


    @Override
    public int getCount()
    {
        return 3;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Chats";
//
//            case 1:
//                return "Groups";

            case 1:
                return "Contacts";

            case 2:
                return "Requests";

            default:
                return null;
        }
    }



}
