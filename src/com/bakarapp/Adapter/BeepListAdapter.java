package com.bakarapp.Adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.bakarapp.R;
import com.bakarapp.HelperClasses.Beep;
import com.bakarapp.Platform.Platform;
import com.bakarapp.beeppopup.BeepsGridViewLayout.OnBeepClickedListener;

public class BeepListAdapter extends BaseAdapter {
	
	List mBeepList;
	LayoutInflater inflater;
	Context mContext;
	OnBeepClickedListener mBeepClickListener;
	
	public BeepListAdapter(List<Beep> beeplist){
		mBeepList = beeplist;
		mContext = Platform.getInstance().getContext();
		inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mBeepList.size();
	}

	@Override
	public Object getItem(int i) {
		// TODO Auto-generated method stub
		return mBeepList.get(i);
	}

	@Override
	public long getItemId(int i) {
		// TODO Auto-generated method stub
		return i;
	}
	
	public void updateBeepList(List<Beep> beeplist)
	{
		mBeepList = beeplist;
	}
	
	public void setBeepClickedListener(OnBeepClickedListener listener)
	{
		mBeepClickListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Beep thisBeep = (Beep) mBeepList.get(position);
		Button thisBeepView;
	
        if( convertView == null)        
        	thisBeepView = (Button)inflater.inflate(R.layout.beep_layout, null); 
        else
        	thisBeepView = (Button)convertView;
    
        thisBeepView.setText(thisBeep.getBeepStr());
        thisBeepView.setHint("Tap to send your own beep");
        thisBeepView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mBeepClickListener.onBeepClicked(thisBeep);
				}
			});
		
        
		return thisBeepView;
	}

}
