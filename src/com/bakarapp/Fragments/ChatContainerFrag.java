package com.bakarapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakarapp.R;

public class ChatContainerFrag extends Fragment{

	FragmentManager fm ;	
	Fragment chatListViewFrag ;
	ChatWindowFrag chatWindowFrag ;
	
	View chatContainer ;
	LayoutInflater inflater ;
	@Override
	public void onCreate(Bundle savedState) {
		chatListViewFrag = new ChatListFragment();
		chatWindowFrag = new ChatWindowFrag();
		inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		chatContainer = inflater.inflate(R.layout.frag_mainscreen_chatcontainer, null);
		fm = getChildFragmentManager();
        super.onCreate(null); 
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView( inflater, container, null );		
		ViewGroup parent = (ViewGroup) chatContainer.getParent();
		if(parent!=null)
			parent.removeView(chatContainer);	
		else
		{
			FragmentTransaction ft;
			ft = fm.beginTransaction();
			//ft.setCustomAnimations(R.anim.slide_in,R.anim.slide_out);
			ft.add(R.id.frag_mainscreen_chatcontainer ,chatListViewFrag, "chatListFrag");		
			ft.commit();
		}
		return chatContainer;
	}
	
	public void showIndividualChatView(String fren_bbd_id,String name)
	{
		 Bundle args = new Bundle();
		 args.putString("participant", fren_bbd_id);
		 args.putString("participant_name", name);	
		 chatWindowFrag = new ChatWindowFrag();
		 chatWindowFrag.setArguments(args);
		 FragmentTransaction ft;
		 ft = fm.beginTransaction();
		 //ft.setCustomAnimations(R.anim.slide_in,R.anim.slide_out);
		 ft.replace(R.id.frag_mainscreen_chatcontainer,chatWindowFrag,"chatWindowFrag");
		 //ft.addToBackStack(null);
		 ft.commit();
	}
	
	public void handleBack()
	{
		FragmentTransaction ft;
		ft = fm.beginTransaction();
		if(chatListViewFrag.isVisible())
		{
			 getActivity().finish();
		}
		else
		{
			ft.replace(R.id.frag_mainscreen_chatcontainer ,chatListViewFrag, "chatListFrag");		
			ft.commit();
		}
	}
	
}
