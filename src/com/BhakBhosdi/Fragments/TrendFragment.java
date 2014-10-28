package com.BhakBhosdi.Fragments;

import com.BhakBhosdi.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TrendFragment extends Fragment{
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);	      
 }
 	 
 @Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
	ViewGroup trendView = (ViewGroup) inflater.inflate(R.layout.trend_layout, null);
	return trendView;
 }

}
