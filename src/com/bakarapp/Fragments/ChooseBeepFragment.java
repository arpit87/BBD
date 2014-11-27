package com.bakarapp.Fragments;

import java.util.List;

import org.json.JSONArray;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.bakarapp.R;
import com.bakarapp.HTTPClient.CreateAndSendBeepRequest;
import com.bakarapp.HTTPClient.GetBeepListRequest;
import com.bakarapp.HTTPClient.HttpClient;
import com.bakarapp.HTTPClient.HttpRequest;
import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.HelperClasses.AlertDialogBuilder;
import com.bakarapp.HelperClasses.Beep;
import com.bakarapp.HelperClasses.BeepList;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.HelperClasses.ToastTracker;
import com.bakarapp.Users.ThisUser;
import com.bakarapp.Util.Logger;
import com.bakarapp.Util.StringUtils;

public class ChooseBeepFragment extends DialogFragment{
	
	private static final String TAG = "com.BhakBhaodi.Fragments.ChooseBeepFragment";
	private List<Beep> beepListShowing = null;
	private ViewGroup mBeepContainer = null;
	private ViewGroup beepListrow1;
	private ViewGroup beepListrow2;
	//private ViewGroup beepListrow3;
	ImageButton fav_button;
	ImageButton forward_button;
	ImageButton back_button;
	ImageButton refresh_button;
	LayoutInflater layoutInflator;
	ChatWindowFrag chatWindowFragment;
	int currentBeepListShowing = -1;
	int defaultNumBeeps = 8;
	int maxEditBeepLength = 50;
	int beepList1RowLength = 0;
	int beepList2RowLength = 0;
	
	private HttpResponseListener beepListListener = null;
	ProgressBar progress = null;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(null);
        chatWindowFragment = (ChatWindowFrag)getParentFragment();
		//update listview
        Logger.i(TAG,"on created choose beep frag");
        
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView( inflater, container, savedInstanceState );
		Logger.i(TAG,"oncreateview livefeed");		
		layoutInflator = inflater;
		
		if(mBeepContainer != null)
		{
			ViewGroup parent = (ViewGroup) mBeepContainer.getParent();
			parent.removeView(mBeepContainer);
			return mBeepContainer;
		}
		
		//this called only once
		mBeepContainer = (ViewGroup) inflater.inflate(R.layout.choosebeep, null);	
		progress = (ProgressBar) mBeepContainer.findViewById(R.id.beepslist_loading);
		beepListrow1 = (ViewGroup) mBeepContainer.findViewById(R.id.choosebeep_beeprow1);
		beepListrow2 = (ViewGroup) mBeepContainer.findViewById(R.id.choosebeep_beeprow2);
		//beepListrow3 = (ViewGroup) mBeepContainer.findViewById(R.id.choosebeep_beeprow3);
		fav_button = (ImageButton) mBeepContainer.findViewById(R.id.choosebeep_fav_button);
		back_button = (ImageButton) mBeepContainer.findViewById(R.id.choosebeep_back_button);
		forward_button = (ImageButton) mBeepContainer.findViewById(R.id.choosebeep_forward_button);
		refresh_button = (ImageButton) mBeepContainer.findViewById(R.id.choosebeep_refresh_button);
		
		beepListShowing = BeepList.getInstance().getFirstBeepList();
		if(beepListShowing == null)
		{
			progress.setVisibility(View.VISIBLE);
			int level = ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL);			
			HttpRequest getBeepsReq = new GetBeepListRequest(level, defaultNumBeeps, getBeepListener());
			HttpClient.getInstance().executeRequest(getBeepsReq);
		}
		else
		{	
			List <Beep> beepList = beepListShowing;
			currentBeepListShowing=0;
			if(beepList!=null)
				showBeepList(beepList);
			else
				ToastTracker.showToast("Please refresh");
		}
		
		forward_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				List <Beep> beepList = BeepList.getInstance().getBeepListAt(currentBeepListShowing+1);
				if(beepList!=null)
				{
					showBeepList(beepList);
					currentBeepListShowing++;
				}
				else
					ToastTracker.showToast("Please refresh");				
			}
		});
		
	back_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				List <Beep> beepList = BeepList.getInstance().getBeepListAt(currentBeepListShowing-1);
				if(beepList!=null)
				{
					showBeepList(beepList);
					currentBeepListShowing--;
				}
				else
					ToastTracker.showToast("Thats it");				
			}
		});
	
	fav_button.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			List <Beep> beepList = BeepList.getInstance().getFavBeepList();
			if(beepList!=null)
			{
				showBeepList(beepList);				
			}
			else
				ToastTracker.showToast("You dont have any fav yet");				
		}
	});
	
refresh_button.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			progress.setVisibility(View.VISIBLE);
			int level = ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL);			
			HttpRequest getBeepsReq = new GetBeepListRequest(level, defaultNumBeeps, getBeepListener());
			HttpClient.getInstance().executeRequest(getBeepsReq);				
		}
	});
						
		return mBeepContainer;
	}
	
	private void cleanBeepList()
	{
		beepListrow1.removeAllViews();
		beepListrow2.removeAllViews();
		beepList1RowLength = 0;
		beepList2RowLength = 0;
		//beepListrow3.removeAllViews();
	}
		
	public void showBeepList(List<Beep> beepListToShow)
	{	
		cleanBeepList();
		beepListShowing = beepListToShow;
		if(!beepListShowing.isEmpty())
		{
			int beepsPerRow = beepListShowing.size() / 2;
			for (int i = 0; i < beepListShowing.size() ; i++ ) 
			{
				final Button beepView;
				final ViewGroup blankBeepView;
				final Beep beep = beepListShowing.get(i);
				String beepStr = beep.getBeepStr();
				final int thisBeepIndex = i;
				if(StringUtils.isBlank(beepStr))
				{
					ImageButton sendEditText;
					final EditText editableBeep;
					ViewGroup thisBeepListrow = null;
					if(i < beepsPerRow)
					{
						blankBeepView = (ViewGroup) layoutInflator.inflate(R.layout.blank_beep_layout,null);
						thisBeepListrow = beepListrow1;
						beepList1RowLength+=10;
					}
					else //if (i < 2*beepsPerRow)
					{
						blankBeepView = (ViewGroup) layoutInflator.inflate(R.layout.blank_beep_layout,null);
						thisBeepListrow = beepListrow2;
						beepList2RowLength+=10;
					}
					thisBeepListrow.addView(blankBeepView);
					editableBeep = (EditText) blankBeepView.findViewById(R.id.blank_beep_edittest);
					sendEditText = (ImageButton) blankBeepView.findViewById(R.id.blank_beep_sendButton);
					sendEditText.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							String editedBeepStr = editableBeep.getText().toString();
							if(StringUtils.isBlank(editedBeepStr))
								AlertDialogBuilder.showOKDialog(getActivity(), "Bhai", "Kuch likh to de pehle");
							else if(editedBeepStr.length()>maxEditBeepLength)
								AlertDialogBuilder.showOKDialog(getActivity(), "Sorry boss", "Max 50 char allowed");
							else
							{							
								final Beep newBeep = new Beep();
								newBeep.setBeep_str(editedBeepStr);
								String bbdid = ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID);
								newBeep.setCreator_bbdid(Integer.parseInt(bbdid));
								int level = ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL);
								newBeep.setLevel(level);
								//img comes random if not set
								chatWindowFragment.sendBeep(newBeep);
								
								//replace to button
								Button beepViewToreplace = (Button) layoutInflator.inflate(R.layout.beep_layout,null);
								beepViewToreplace.setText(editedBeepStr);
								replaceView(blankBeepView,beepViewToreplace);
								BeepList.getInstance().getBeepListAt(currentBeepListShowing).set(thisBeepIndex,newBeep);
								
								beepViewToreplace.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										chatWindowFragment.sendBeep(newBeep);
									}
								});
								
								//send beep to server
								HttpRequest createBeepReq= new CreateAndSendBeepRequest(editedBeepStr,level,chatWindowFragment.getParticipantBBDID(),bbdid,null);
								HttpClient.getInstance().executeRequest(createBeepReq);
							}
						}
					});
				}
				else
				{	
					if(i < beepsPerRow)
					{
						beepView = (Button) layoutInflator.inflate(R.layout.beep_layout,null);
						beepListrow1.addView(beepView);
						beepList1RowLength+=beepStr.length();
					}
					else //if (i < 2*beepsPerRow)
					{
						beepView = (Button) layoutInflator.inflate(R.layout.beep_layout,null);
						beepListrow2.addView(beepView);
						beepList2RowLength+=beepStr.length();
					}
					//else
					//{
					//	beepView = (Button) layoutInflator.inflate(R.layout.beep_layout,null);
					//	beepListrow3.addView(beepView);
					//}
					beepView.setText(beepStr);
					beepView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							chatWindowFragment.sendBeep(beep);
						}
					});
				}
				
				
			}
			
			
			fillblankspace();
			progress.setVisibility(View.GONE);
			Logger.i(TAG, "beep not empty,will show");	
			//Platform.getInstance().getHandler().post(showNextFeed);			
		}
		else
		{
			progress.setVisibility(View.VISIBLE);
			Logger.i(TAG, "beep empty,will call api");
			String bbdid = ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID);	
			int cutoff = ThisUser.getInstance().getBeepCutoff();
			GetBeepListRequest feedReq = new GetBeepListRequest(Integer.parseInt(bbdid), cutoff , getBeepListener());
			HttpClient.getInstance().executeRequest(feedReq);
		}
	}
	
	private void replaceView(View currentView, View newView) {
        ViewGroup parent = (ViewGroup)currentView.getParent();
        if(parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        parent.removeView(currentView);
        parent.removeView(newView);
        parent.addView(newView, index);
    }
	
	private void fillblankspace()
	{
		int beepList1width = beepList1RowLength;
		int beepList2width = beepList2RowLength;
		int diff = beepList1width - beepList2width;
		ViewGroup rowtoFill = null;
		if(diff > 3)
			rowtoFill = beepListrow2;
		else if (diff < -3){
			rowtoFill = beepListrow1;
			diff = diff * -1;
		}
		else
			return;
			
		final View blankBeepView = (ViewGroup) layoutInflator.inflate(R.layout.blank_beep_layout,null);
		final EditText editableBeep = (EditText) blankBeepView.findViewById(R.id.blank_beep_edittest);		
		ImageButton sendEditText = (ImageButton) blankBeepView.findViewById(R.id.blank_beep_sendButton);
		//Button beepView = null;
		editableBeep.setHint(StringUtils.fixedLengthString(" ", diff-3));
		sendEditText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String editedBeepStr = editableBeep.getText().toString();
				if(StringUtils.isBlank(editedBeepStr))
					AlertDialogBuilder.showOKDialog(getActivity(), "Are bhai", "Kuch likh to de pehle");
				else if(editedBeepStr.length()>maxEditBeepLength)
					AlertDialogBuilder.showOKDialog(getActivity(), "Sorry boss", "Max 50 char allowed");
				else
				{							
					final Beep newBeep = new Beep();
					newBeep.setBeep_str(editedBeepStr);
					String bbdid = ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID);
					newBeep.setCreator_bbdid(Integer.parseInt(bbdid));
					int level =ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL);
					newBeep.setLevel(level);
					//img comes random if not set
					chatWindowFragment.sendBeep(newBeep);

					//replace to button
					Button beepViewToreplace = (Button) layoutInflator.inflate(R.layout.beep_layout,null);
					beepViewToreplace.setText(editedBeepStr);
					replaceView(blankBeepView,beepViewToreplace);
					beepViewToreplace.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							chatWindowFragment.sendBeep(newBeep);
						}
					});
					
					//send beep to server to add to list
					HttpRequest createBeepReq= new CreateAndSendBeepRequest(editedBeepStr,level,chatWindowFragment.getParticipantBBDID(),bbdid,null);
					HttpClient.getInstance().executeRequest(createBeepReq);
										
				}
			}
		});
		
		
		rowtoFill.addView(blankBeepView);		
		
	}
	
	
		
	HttpResponseListener getBeepListener()
	{
		if(beepListListener == null)
			beepListListener = new BeepListener();
		return beepListListener;
	}
	
	class BeepListener extends HttpResponseListener
	{

		@Override
		public void onComplete(Object beepListJsonArrayObj) {
			Logger.i(TAG, "beep fetch complete");			
			BeepList.getInstance().updateBeepList((JSONArray)beepListJsonArrayObj);
			if(!hasBeenCancelled)
			{
				progress.setVisibility(View.GONE);		
				currentBeepListShowing = BeepList.getInstance().getBeepListCount()-1;
				showBeepList(BeepList.getInstance().getBeepListAt(currentBeepListShowing));			
			}
		}
		
	}
	
}
