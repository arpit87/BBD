package com.bakarapp.Activities;



import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.bakarapp.R;
import com.bakarapp.ChatService.ChatService;
import com.bakarapp.ChatService.IChatManager;
import com.bakarapp.ChatService.IXMPPAPIs;
import com.bakarapp.Fragments.ChatContainerFrag;
import com.bakarapp.Fragments.SettingsFragment;
import com.bakarapp.Fragments.TrendFragment;
import com.bakarapp.HelperClasses.ThisAppConfig;
import com.bakarapp.HelperClasses.ThisAppInstallation;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;
import com.bakarapp.Util.StringUtils;

public class BhakBhosdiActivity extends FragmentActivity{

	private static String TAG = "com.bakarapp.MainActivity";
	private ViewPager mPager;
    private MyPageAdapter mPagerAdapter;
    private ImageButton button1;
    private ImageButton button2;
    private ImageButton button3;
    
    private IXMPPAPIs xmppApis = null;
    IChatManager chatManager = null;    
       
    private final ChatServiceConnection mChatServiceConnection = new ChatServiceConnection();
   
    private boolean mBinded = false;
    private String mThiUserChatUserName = "";
    private String mThisUserChatPassword = "";
    ChatContainerFrag bbdFrag ;
	Fragment trendsFrag ;
	Fragment settingsFrag ;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check for first run
        if(StringUtils.isBlank(ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID)))
        {
        	firstRun();
        	return;
        }           

    }
    
    
    private void firstRun() {
    	Logger.i(TAG, "first run") ;
 		String uuid = ThisAppInstallation.id(this.getBaseContext());
 		ThisAppConfig.getInstance().putString(ThisAppConfig.APPUUID,uuid);  
 		ThisUserConfig.getInstance().putInt(ThisUserConfig.LEVEL, 3);
 		ThisAppConfig.getInstance().putInt(ThisAppConfig.NOTIFICATION_SETTINGS,1);
 		ThisAppConfig.getInstance().putInt(ThisAppConfig.SOUND_SETTINGS,1);
    	String deviceId = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
		ThisAppConfig.getInstance().putString(ThisAppConfig.DEVICEID,deviceId);
    	Intent show_login = new Intent(this,LoginActivity.class);
    	show_login.putExtra("uuid", uuid);
    	startActivity(show_login);
    	finish();   	
    	
	}
    
    public void initializeChatListView()
    {
    	setContentView(R.layout.mainscreen);        
        
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.mainscreen_viewpager);
        List<Fragment> fragments = getFragments();       
        mPagerAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);      
        mPager.setAdapter(mPagerAdapter); 
        
     // Watch for button clicks.
        button1 = (ImageButton)findViewById(R.id.mainscreen_bdd);
        button2 = (ImageButton)findViewById(R.id.mainscreen_trends);
        button3 = (ImageButton)findViewById(R.id.mainscreen_settings);
        button1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	bbdSelected();
            	mPager.setCurrentItem(0);
            }
        });        
        
        button2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	trendsSelected();
            	mPager.setCurrentItem(1);
            }
        });
        
        button3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	settingsSelected();
            	mPager.setCurrentItem(2);
            }
        });
        
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            	switch(position)
        		{
        			case 0: //about button selected
        				bbdSelected();
        			break;
        			case 1:
        				trendsSelected();
        			break;
        			case 2:
        				settingsSelected();
        			break;
        			default:
        				bbdSelected();    		
        		}
            }

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
        });
        
        bbdSelected(); // initially select "about" view
        mPager.setCurrentItem(0);
        
    }

	public void bbdSelected()
	{		
    	button1.setSelected(true);
    	button2.setSelected(false);    
    	button3.setSelected(false);
	}
	
	public void trendsSelected()
	{		
    	button1.setSelected(false);    	   
    	button2.setSelected(true);
    	button3.setSelected(false);
	}
	
	public void settingsSelected()
	{		
    	button1.setSelected(false);    	   
    	button2.setSelected(false);
    	button3.setSelected(true);
	}
    
    private List<Fragment> getFragments() {
		List<Fragment> frag_list= new ArrayList<Fragment>(); 
		bbdFrag = new ChatContainerFrag();
		trendsFrag = new TrendFragment();
		settingsFrag = new SettingsFragment();		
		frag_list.add(bbdFrag);
		frag_list.add(trendsFrag);
		frag_list.add(settingsFrag);
		return frag_list;
	}


    @Override
    public void onResume() {
    	super.onResume();
    	if(!mBinded)
    		bindToService();
    }
    
    @Override
    public void onDestroy() {
    	
    	super.onDestroy();
    	if (mBinded) {
    		releaseService();
    	    mBinded = false;
    	}
    	xmppApis = null;	
     	chatManager = null;
    }   
    
    
	@Override
    public void onBackPressed() {
		BBDTracker.sendEvent("Profile","BackButton","click:back",1L);
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            bbdFrag.handleBack();            
            
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
    
 class MyPageAdapter extends FragmentPagerAdapter {
    	
    	private List<Fragment> fragments;
    	public MyPageAdapter(FragmentManager fm, List<Fragment> fragments) {    
    	super(fm);
    
    	this.fragments = fragments;    	
    	}
    	
    	@Override    
    	public Fragment getItem(int position) {     	
    	return this.fragments.get(position);    	
    	}
    
    	 
    
    	@Override    
    	public int getCount() {    
    	return this.fragments.size();   
    	}
    
    	}
 
 
 /////////////Chat related methods
 public IChatManager getChatManager()
 { 
	if(chatManager == null)
	{
	   try {
		  if(xmppApis != null) 
			  chatManager = xmppApis.getChatManager();
		  else if(!mBinded)
			  bindToService();
	} catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	}
    
   return chatManager;
 }
 
 private void bindToService() {
         if (Platform.getInstance().isLoggingEnabled()) Log.d( TAG, "binding chat to service" );        
     	
        Intent i = new Intent(getApplicationContext(),ChatService.class);
       
        getApplicationContext().bindService(i, mChatServiceConnection, BIND_AUTO_CREATE);	  
        
     
  }
 
	    
	private void releaseService() {
		if(mChatServiceConnection != null) {
			getApplicationContext().unbindService(mChatServiceConnection);
			mBinded = false;
			if (Platform.getInstance().isLoggingEnabled()) Log.d( TAG, "chat Service released from chatwindow" );
		} else {
			//ToastTracker.showToast("Cannot unbind - service not bound", Toast.LENGTH_SHORT);
		}
	}

	
    private final class ChatServiceConnection implements ServiceConnection{
    	
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder boundService) {
    		//ToastTracker.showToast("onServiceConnected called", Toast.LENGTH_SHORT);
    		if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG,"onServiceConnected called");
    		xmppApis = IXMPPAPIs.Stub.asInterface((IBinder)boundService);
    		try {
				chatManager = xmppApis.getChatManager();
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		try {
    	        mThiUserChatUserName = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATUSERID);
    	        mThisUserChatPassword = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATPASSWORD);
				xmppApis.loginAsync(mThiUserChatUserName, mThisUserChatPassword);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		mBinded = true;
    		initializeChatListView();    	
    		if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG,"service connected");
    	}

    	@Override
    	public void onServiceDisconnected(ComponentName arg0) {
    		//ToastTracker.showToast("onService disconnected", Toast.LENGTH_SHORT);
    		xmppApis = null;   		
    	    
    		if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG,"service disconnected");
    	}

    }	
	
	
 
 
}
	



