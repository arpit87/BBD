package com.bakarapp.Adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakarapp.R;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.HelperClasses.Beep;
import com.bakarapp.HelperClasses.SBImageLoader;

public class MyBeepListAdapter extends BaseAdapter{

	List<Beep> mMyList;
	Activity underLyingActivity;
	private static LayoutInflater inflater=null;
	public MyBeepListAdapter(Activity activity,List<Beep> beeps)
	{
		underLyingActivity = activity;
		mMyList = beeps;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mMyList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mMyList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public void updateList(List<Beep> beepList)
	{
		mMyList = beepList;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Beep thisBeep = (Beep) mMyList.get(position);
		View thisBeepView=convertView;
        if( thisBeepView == null)        
        	thisBeepView = inflater.inflate(R.layout.mybeep_row1, null);        
             
        TextView beepstr_view = (TextView)thisBeepView.findViewById(R.id.mybeep_row_text);
        ImageView beep_img = (ImageView)thisBeepView.findViewById(R.id.mybeep_row_img);
        TextView likesnum_view = (TextView)thisBeepView.findViewById(R.id.mybeep_row_numlike);
        TextView rebeepnum_view = (TextView)thisBeepView.findViewById(R.id.mybeep_row_numrebeep);
        
        String imageURL = ServerConstants.TROLLIMGLOC +"my/" + thisBeep.getBeepImg();
        SBImageLoader.getInstance().displayImageElseStub(imageURL, beep_img, R.drawable.loading);
        beepstr_view.setText(thisBeep.getBeepStr());
        likesnum_view.setText(Integer.toString(thisBeep.getLikes()));
        rebeepnum_view.setText(Integer.toString(thisBeep.getRebeeps()));
		return thisBeepView;
	}

}
