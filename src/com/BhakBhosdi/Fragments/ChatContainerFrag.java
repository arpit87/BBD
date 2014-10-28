package com.BhakBhosdi.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.BhakBhosdi.R;

public class ChatContainerFrag extends Fragment{

	FragmentTransaction ft ;	
	
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(null); 
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView( inflater, container, null );
		View chatContainer = inflater.inflate(R.layout.frag_mainscreen_chatcontainer, null);	
		Fragment chatListViewFrag = new ChatListFragment();
		ft = getChildFragmentManager().beginTransaction();
		//ft.setCustomAnimations(R.anim.slide_in,R.anim.slide_out);
		ft.add(R.id.frag_mainscreen_chatcontainer ,chatListViewFrag, "chatListFrag");		
		ft.commit();
		return chatContainer;
	}
	
	public void showIndividualChatView(String fren_bbd_id,String name)
	{
		 Bundle args = new Bundle();
		 args.putString("participant", fren_bbd_id);
		 args.putString("participant_name", name);
		 Fragment chatWindowFrag = new ChatWindowFrag();
		 chatWindowFrag.setArguments(args);
		 FragmentTransaction ft;
		 ft = getChildFragmentManager().beginTransaction();
		 ft.setCustomAnimations(R.anim.slide_in,R.anim.slide_out);
		 ft.replace(R.id.frag_mainscreen_chatcontainer,chatWindowFrag,"chatwindowfrag");
		 ft.addToBackStack(null);
		 ft.commit();
	}
	
}
