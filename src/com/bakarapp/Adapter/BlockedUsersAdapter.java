package com.bakarapp.Adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bakarapp.HelperClasses.BlockedUser;

public class BlockedUsersAdapter extends BaseAdapter {

    private List<BlockedUser> blockedUsers;
    private LayoutInflater inflater;

    public BlockedUsersAdapter(Activity activity, List<BlockedUser> blockedUsers){
        this.blockedUsers = blockedUsers;
        this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return blockedUsers.size();
    }

    @Override
    public BlockedUser getItem(int i) {
        return blockedUsers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
		return parent;
        
    }
}
