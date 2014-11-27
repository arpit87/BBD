
package com.bakarapp.beeppopup;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bakarapp.R;
import com.bakarapp.Adapter.BeepListAdapter;
import com.bakarapp.HelperClasses.Beep;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.Util.Logger;


public class BeepsGridViewLayout{
	public View rootView;
	BeepsPopup mBeepPopup;  
	BeepListAdapter mAdapter;
	List<Beep> mBeepList;
    TextView emptyListTextView;
    private static String TAG ="com.bakarapp.beeppopup.BeepsGridViewLayout";
   
    
    public BeepsGridViewLayout(Context context, List<Beep> beepList,  BeepsPopup beepPopup,int viewnum) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		mBeepPopup = beepPopup;		
		rootView = inflater.inflate(R.layout.beep_grid, null);		
		 BeepsGridView gridView = (BeepsGridView) rootView.findViewById(R.id.grid_view);
		 emptyListTextView = (TextView) rootView.findViewById(R.id.beep_gridview_textview);
		 
		 if(beepList.size()==0)
		 {
			 String noBeepText ="";
			 int userLevel = ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL);
			 if(viewnum == 0)
				 noBeepText = "No favourites yet!!";
			 else if(viewnum>0 && viewnum<=userLevel)
				 noBeepText = "No beeps fetched for this level, refresh again";
			 else
				 noBeepText = "Censored for you bacche, level badhao";
					 
			 emptyListTextView.setText(noBeepText);
			 emptyListTextView.setVisibility(View.VISIBLE);
		 }
	     mAdapter = new BeepListAdapter(beepList);
	     mAdapter.setBeepClickedListener(new OnBeepClickedListener() {
				
				@Override
				public void onBeepClicked(Beep beep) {
					if (mBeepPopup.onBeepClickedListener != null) {
			            mBeepPopup.onBeepClickedListener.onBeepClicked(beep);
			        }
				}
			});
	        gridView.setAdapter(mAdapter);
	}
    
	public void updateGridView(List<Beep> beepList)
	{
		Logger.i(TAG, "updating grid");
		if(beepList.size()>0)
			emptyListTextView.setVisibility(View.GONE);
		mAdapter.updateBeepList(beepList);
		mAdapter.notifyDataSetChanged();
	}
	

	public interface OnBeepClickedListener {
        void onBeepClicked(Beep beep);
    }
    
}
