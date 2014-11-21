package com.bakarapp.Adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bakarapp.R;
import com.bakarapp.HelperClasses.Beep;
import com.bakarapp.HelperClasses.ToastTracker;

public class TrendListAdapter extends BaseAdapter{

	List <Beep> mTrendList;
	Activity underLyingActivity;
	private static LayoutInflater inflater=null;
	public TrendListAdapter(Activity activity,List<Beep> beeps)
	{
		underLyingActivity = activity;
		mTrendList = beeps;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mTrendList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mTrendList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public void updateTrends(List<Beep> trendBeepList)
	{
		mTrendList = trendBeepList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Beep thisBeep =  mTrendList.get(position);
		View thisBeepView=convertView;
        if( thisBeepView == null)        
        	thisBeepView = inflater.inflate(R.layout.trend_listrow, null);
        
        TextView rank_view = (TextView)thisBeepView.findViewById(R.id.trend_listrow_beeprank);      
        TextView beepstr_view = (TextView)thisBeepView.findViewById(R.id.trend_listrow_beepstr);
        TextView beepby_view = (TextView)thisBeepView.findViewById(R.id.trend_listrow_beepby);
        TextView likenum_view = (TextView)thisBeepView.findViewById(R.id.trend_listrow_likes_num);
        final ImageButton like_button = (ImageButton)thisBeepView.findViewById(R.id.trend_listrow_like_button);
        final ImageButton fav_button = (ImageButton)thisBeepView.findViewById(R.id.trend_listrow_fav);
        TextView rebeepnum_view = (TextView)thisBeepView.findViewById(R.id.trend_listrow_rebeep_num);
        ImageButton rebeep_button = (ImageButton)thisBeepView.findViewById(R.id.trend_listrow_rebeep_button);
        
        /*like_button.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(!like_button.isPressed())
				{
					like_button.setPressed(true);	
					ToastTracker.showToast("Liked!!");
				}
				else
				{
					like_button.setPressed(false);	
					ToastTracker.showToast("Unliked..");
				}
				return true;
			}
		});
        
        fav_button.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(!fav_button.isPressed())
				{
					fav_button.setPressed(true);
					ToastTracker.showToast("Added to favourite");
				}
				else
				{
					fav_button.setPressed(false);	
					ToastTracker.showToast("Removed from favourite");
				}
				return true;
			}
		});*/
        
       
        rank_view.setText("#"+Integer.toString(position+1));
        beepstr_view.setText(thisBeep.getBeepStr());
        beepby_view.setText("by BA pin " + Integer.toString(thisBeep.getBeepCreator()));
        likenum_view.setText(Integer.toString(thisBeep.getLikes()));
        rebeepnum_view.setText(Integer.toString(thisBeep.getRebeeps()));
		return thisBeepView;
	}

}
