package com.BhakBhosdi.Fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.BhakBhosdi.R;
import com.BhakBhosdi.Adapter.BBDListAdapter;
import com.BhakBhosdi.HelperClasses.ActiveChat;
import com.BhakBhosdi.Platform.Platform;
import com.BhakBhosdi.Util.BBDTracker;

public class ChatListFragment extends ListFragment {
	
	private static final String TAG = "com.BhakBhosdi.Fragments.ChatListFragment";
	private ViewGroup mListViewContainer;
	private List<ActiveChat> chatUserlist = null;
	BBDListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(null);
		//update listview
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"on create list view");
        chatUserlist = ActiveChat.getActiveChats();  
        
        //set this before setadapter
        View footer = ((LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chatlist_footer, null, false);
		getListView().addFooterView(footer);
		
        if(!chatUserlist.isEmpty())
        {
			mAdapter = new BBDListAdapter(getActivity(), chatUserlist);			
			setListAdapter(mAdapter);
			if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"chatlist users:"+chatUserlist.toString());
        }
        
        footer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				
			}
		});
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView( inflater, container, null );
		if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"oncreateview chatlistview");		
		mListViewContainer = (ViewGroup) inflater.inflate(R.layout.frag_mainscreen_chatlistview, null);		
		return mListViewContainer;
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		BBDTracker.sendEvent("MyChat","ListClick","mychats:click:listitem",1L);
		ActiveChat clickedUser = chatUserlist.get(position);
		String bbdid = clickedUser.getUserId();
		String name = clickedUser.getName();
		((ChatContainerFrag)getParentFragment()).showIndividualChatView(bbdid, name);
    }	
			
}


