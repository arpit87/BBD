package com.bakarapp.Activities;

import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bakarapp.R;
import com.bakarapp.Adapter.MyBeepListAdapter;
import com.bakarapp.HTTPClient.GetMyBeepListRequest;
import com.bakarapp.HTTPClient.HttpClient;
import com.bakarapp.HTTPClient.HttpRequest;
import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.HelperClasses.Beep;
import com.bakarapp.HelperClasses.BeepList;
import com.bakarapp.Util.Logger;

public class MyBeepsActivity extends Activity{
	
	private static String TAG = "com.bakarapp.Activities.MyBeepActivity";
	TextView noBeepTextView ;
	
	ListView myBeepListView;
	ProgressBar mProgress;
	private MyBeepsResponseListener instance = null;
	MyBeepListAdapter mAdapter = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mybeeps_layout);        
        noBeepTextView = (TextView) findViewById(R.id.mybeeps_nobeeps);
        myBeepListView = (ListView) findViewById(R.id.mybeeps_listview);
        mProgress = (ProgressBar) findViewById(R.id.mybeeps_progress);
        List<Beep> mybeepList = BeepList.getInstance().getMyBeepList();
        mAdapter = new MyBeepListAdapter(MyBeepsActivity.this,mybeepList);
        myBeepListView.setAdapter(mAdapter);
        
        if(mybeepList.size()==0)
        {
        	HttpRequest getMyBeepsReq = new GetMyBeepListRequest(8, getListener());
        	HttpClient.getInstance().executeRequest(getMyBeepsReq);
        }
	}
	
	public HttpResponseListener getListener()
	{
		if(instance == null)
			instance = new MyBeepsResponseListener();
		return instance;
	}
	
	 class MyBeepsResponseListener extends HttpResponseListener
	{

		
			@Override
			public void onComplete(Object beepListJsonArrayObj) {
				Logger.i(TAG, "my beep fetch complete");			
				JSONArray beepArray = (JSONArray)beepListJsonArrayObj;
				BeepList.getInstance().updateMyBeepList(beepArray);
				if(!hasBeenCancelled)
				{
					if(beepArray.length()==0)
					{
						noBeepTextView.setText("No beeps yet!!!");
						mProgress.setVisibility(View.GONE);
						return;
					}
					
					myBeepListView.setVisibility(View.VISIBLE);
					mProgress.setVisibility(View.GONE);
					noBeepTextView.setVisibility(View.GONE);						
					List<Beep> mybeepList = BeepList.getInstance().getMyBeepList();
					mAdapter.updateList(mybeepList);
					mAdapter.notifyDataSetChanged();								
				}
			}
			
		}
					
				
}


