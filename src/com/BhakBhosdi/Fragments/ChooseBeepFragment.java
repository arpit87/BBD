package com.BhakBhosdi.Fragments;

import java.util.List;

import org.json.JSONArray;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.BhakBhosdi.R;
import com.BhakBhosdi.HTTPClient.GetBeepListRequest;
import com.BhakBhosdi.HTTPClient.HttpClient;
import com.BhakBhosdi.HTTPClient.HttpRequest;
import com.BhakBhosdi.HTTPClient.HttpResponseListener;
import com.BhakBhosdi.HelperClasses.AlertDialogBuilder;
import com.BhakBhosdi.HelperClasses.Beep;
import com.BhakBhosdi.HelperClasses.BeepList;
import com.BhakBhosdi.HelperClasses.ThisUserConfig;
import com.BhakBhosdi.Users.ThisUser;
import com.BhakBhosdi.Util.Logger;
import com.BhakBhosdi.Util.StringUtils;

public class ChooseBeepFragment extends Fragment{
	
	private static final String TAG = "com.BhakBhaodi.Fragments.ChooseBeepFragment";
	private List<Beep> beepList = null;
	private ViewGroup mBeepContainer;
	private ViewGroup beepListrow1;
	private ViewGroup beepListrow2;
	private ViewGroup beepListrow3;
	LayoutInflater layoutInflator;
	ChatWindowFrag chatWindowFragment;
	
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
		mBeepContainer = (ViewGroup) inflater.inflate(R.layout.choosebeep, null);	
		progress = (ProgressBar) mBeepContainer.findViewById(R.id.beepslist_loading);
		beepListrow1 = (ViewGroup) mBeepContainer.findViewById(R.id.choosebeep_beeprow1);
		beepListrow2 = (ViewGroup) mBeepContainer.findViewById(R.id.choosebeep_beeprow2);
		beepListrow3 = (ViewGroup) mBeepContainer.findViewById(R.id.choosebeep_beeprow3);
		
		if(beepList == null)
		{
			progress.setVisibility(View.VISIBLE);
			int level = ThisUser.getInstance().getLevel();
			int numbeeps = 7;
			HttpRequest getBeepsReq = new GetBeepListRequest(level, numbeeps, getBeepListener());
			HttpClient.getInstance().executeRequest(getBeepsReq);
		}
		else
			showBeepList();
						
		return mBeepContainer;
	}
		
	public void showBeepList()
	{
		beepList = BeepList.getInstance().getBeepList();
		if(!beepList.isEmpty())
		{
			int beepsPerRow = beepList.size() / 3;
			for (int i = 0; i < beepList.size() ; i++ ) 
			{
				final Button beepView;
				final ViewGroup blankBeepView;
				Beep beep = beepList.get(i);
				String beepStr = beep.getBeepStr();
				if(StringUtils.isBlank(beepStr))
				{
					final ImageButton sendEditText;
					final EditText editableBeep;
					if(i < beepsPerRow)
					{
						blankBeepView = (ViewGroup) layoutInflator.inflate(R.layout.blank_beep_layout,null);
						beepListrow1.addView(blankBeepView);
					}
					else if (i < 2*beepsPerRow)
					{
						blankBeepView = (ViewGroup) layoutInflator.inflate(R.layout.blank_beep_layout,null);
						beepListrow2.addView(blankBeepView);
					}
					else
					{
						blankBeepView = (ViewGroup) layoutInflator.inflate(R.layout.blank_beep_layout,null);
						beepListrow3.addView(blankBeepView);
					}
					editableBeep = (EditText) getActivity().findViewById(R.id.blank_beep_edittest);
					sendEditText = (ImageButton) getActivity().findViewById(R.id.blank_beep_sendButton);
					sendEditText.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							String editedBeepStr = editableBeep.getText().toString();
							if(StringUtils.isBlank(editedBeepStr))
								AlertDialogBuilder.showOKDialog(getActivity(), "Are bhai", "Kuch likh to de pehle");
							else
								chatWindowFragment.sendMessage(editedBeepStr);
						}
					});
				}
				else
				{	
					if(i < beepsPerRow)
					{
						beepView = (Button) layoutInflator.inflate(R.layout.beep_layout,null);
						beepListrow1.addView(beepView);
					}
					else if (i < 2*beepsPerRow)
					{
						beepView = (Button) layoutInflator.inflate(R.layout.beep_layout,null);
						beepListrow2.addView(beepView);
					}
					else
					{
						beepView = (Button) layoutInflator.inflate(R.layout.beep_layout,null);
						beepListrow3.addView(beepView);
					}
					beepView.setText(beepStr);
					beepView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							chatWindowFragment.sendMessage(beepView.getText().toString());
						}
					});
				}
				
				
			}
			progress.setVisibility(View.GONE);
			Logger.i(TAG, "beep not empty,will show");	
			//Platform.getInstance().getHandler().post(showNextFeed);			
		}
		else
		{
			progress.setVisibility(View.VISIBLE);
			Logger.i(TAG, "beep empty,will call api");
			int bbdid = ThisUserConfig.getInstance().getInt(ThisUserConfig.BBD_ID);	
			int cutoff = ThisUser.getInstance().getBeepCutoff();
			GetBeepListRequest feedReq = new GetBeepListRequest(bbdid, cutoff , getReqListener());
			HttpClient.getInstance().executeRequest(feedReq);
		}
	}
	
	@Override
    public void onDestroyView() {
        super.onDestroyView();
        beepListListener.hasBeenCancelled = true;
		}
	
	
	public HttpResponseListener getReqListener()
	{
		if(beepListListener == null)
			beepListListener = new BeepListener();
		return  beepListListener;
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
				showBeepList();			
			}
		}
		
	}
	
}
