package com.bakarapp.Fragments;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bakarapp.R;
import com.bakarapp.Activities.MyBeepsActivity;
import com.bakarapp.Adapter.TrendListAdapter;
import com.bakarapp.HTTPClient.GetBeepTrendsRequest;
import com.bakarapp.HTTPClient.HttpClient;
import com.bakarapp.HTTPClient.HttpRequest;
import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.HelperClasses.Beep;
import com.bakarapp.HelperClasses.BeepList;
import com.bakarapp.Users.UserAttributes;
import com.bakarapp.Util.Logger;

public class TrendFragment extends Fragment{
	
	private String TAG = "com.bakarapp.Fragments.TrendFragment";
	
	ToggleButton daily_trend ;
	ToggleButton weekly_trend ;
	ToggleButton monthly_trend ;
	TextView showMyBeep ;
	ViewGroup trendView = null;
	ListView trendListView;
	ProgressBar progress;
	HttpResponseListener trendListener;
	TextView newBeepNumbers;
	TextView totalbeepNumbers;
	TrendListAdapter mAdapter;
	List <Beep> trendListToShow = new ArrayList<Beep>();
	int newTrendBeeps = 0;
	int totalTrendBeeps = 0;
	int trend_type_showing = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);  
        mAdapter = new TrendListAdapter(getActivity(), trendListToShow); // empty list for first time
	}
 	 
 @Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
	 
	 if(trendView!=null)
	 {
		 ViewGroup parent = (ViewGroup) trendView.getParent();
		if(parent!=null)
			parent.removeView(trendView);
		switchTrendButton(trend_type_showing);
		return trendView;		
	 }			 
	 
	 //following called only once at time of frag creation
	trendView = (ViewGroup) inflater.inflate(R.layout.trend_layout, null);
	daily_trend = (ToggleButton) trendView.findViewById(R.id.trend_daily_button);
	weekly_trend = (ToggleButton) trendView.findViewById(R.id.trend_weekly_button);
	monthly_trend = (ToggleButton) trendView.findViewById(R.id.trend_monthly_button);
	trendListView = (ListView) trendView.findViewById(R.id.trend_listview);
	trendListView.setAdapter(mAdapter);
	progress = (ProgressBar) trendView.findViewById(R.id.trend_progress);
	newBeepNumbers = (TextView) trendView.findViewById(R.id.trend_numnewbeeps);
	totalbeepNumbers = (TextView) trendView.findViewById(R.id.trend_numtotalbeeps);
	showMyBeep =(TextView) trendView.findViewById(R.id.trend_showmybeep);
	
	showMyBeep.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent mybeepIntent = new Intent(getActivity(),MyBeepsActivity.class);
			getActivity().startActivity(mybeepIntent);
		}
	});
	
	daily_trend.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			   
	        	updateTrendList(1);
	        
		}
	});
	
	weekly_trend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
		        	updateTrendList(2);
		        
			}
		});
	
	monthly_trend.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
	        	updateTrendList(3);
	        
		}
	});
	
	
	 trendListToShow = BeepList.getInstance().getTrendList(1);
     if(trendListToShow.size()==0)
     {
    	progress.setVisibility(View.VISIBLE);
     	trendListView.setVisibility(View.GONE);
     	HttpRequest getTrendReq = new GetBeepTrendsRequest(1, 10, getTrendistener());
     	HttpClient.getInstance().executeRequest(getTrendReq);
     }
     else
     {    	 
    	updateTrendList(1);
    	progress.setVisibility(View.GONE);
     	trendListView.setVisibility(View.VISIBLE);
     }
       
	
	return trendView;
 }
 
 private void allTrendButtonOff()
 {
	 daily_trend.setChecked(false);
	 weekly_trend.setChecked(false);
	 monthly_trend.setChecked(false);
 }
 
 private void switchTrendButton(int trend_type)
 {
	 switch(trend_type)
 	{
 		case 1:
 			daily_trend.setChecked(true);
 			weekly_trend.setChecked(false);
 			monthly_trend.setChecked(false);
 		break;	
 		
 		case 2:
 			daily_trend.setChecked(false);
 			weekly_trend.setChecked(true);
 			monthly_trend.setChecked(false);
 			break;	
 		
 		case 3:
 			daily_trend.setChecked(false);
 			weekly_trend.setChecked(false);
 			monthly_trend.setChecked(true);   			
 			break;
 	}    	
 }
 private void updateTrendList(int trend_type)
 {
	 
	trendListToShow = BeepList.getInstance().getTrendList(trend_type);	
    if(trendListToShow.size()==0)	        
    {
    	progress.setVisibility(View.VISIBLE);
    	trendListView.setVisibility(View.GONE);
    	allTrendButtonOff();
    	HttpRequest getTrendReq = new GetBeepTrendsRequest(trend_type, 10, getTrendistener());
    	HttpClient.getInstance().executeRequest(getTrendReq);
    }
    else
    {
    	allTrendButtonOff();
    	switchTrendButton(trend_type);
		mAdapter.updateTrends(trendListToShow);
	 	mAdapter.notifyDataSetChanged();
	 	trend_type_showing = trend_type;
	 	newTrendBeeps = BeepList.getInstance().getTrendNewBeeps(trend_type);
	 	totalTrendBeeps = BeepList.getInstance().getTrendTotalBeeps(trend_type);
	 	newBeepNumbers.setText(Integer.toString(newTrendBeeps));
	 	totalbeepNumbers.setText(Integer.toString(totalTrendBeeps));
    }
 }
 
   

	public HttpResponseListener getTrendistener()
	{
		if(trendListener == null)
			trendListener = new TrendListener();
		return  trendListener;
	}
	
	class TrendListener extends HttpResponseListener
	{

		@Override
		public void onComplete(Object trendListJsonObj) {
			Logger.i(TAG, "trend fetch complete");	
			JSONObject trendListreturn = (JSONObject)trendListJsonObj;
			int trend_type = 1;
			try {
				trend_type = trendListreturn.getInt(UserAttributes.TREND_TYPE);			
				int newBeepNum = trendListreturn.getInt(UserAttributes.NEW_BEEP_NUM);
				int totalBeepNum = trendListreturn.getInt(UserAttributes.TOTAL_BEEP_NUM);
				JSONArray beeps = trendListreturn.getJSONArray("BeepList");
				BeepList.getInstance().updateTrendBeepList(beeps, trend_type, newBeepNum, totalBeepNum);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				hasBeenCancelled= true;
			}
			
			if(!hasBeenCancelled)
			{
				progress.setVisibility(View.GONE);		
				trendListView.setVisibility(View.VISIBLE);
				updateTrendList(trend_type);
			}
		}
		
	}

}
