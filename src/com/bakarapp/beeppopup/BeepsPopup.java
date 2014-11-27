
package com.bakarapp.beeppopup;


import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.bakarapp.R;
import com.bakarapp.HTTPClient.GetBeepListRequest;
import com.bakarapp.HTTPClient.HttpClient;
import com.bakarapp.HTTPClient.HttpRequest;
import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.HelperClasses.BeepList;
import com.bakarapp.HelperClasses.FavBeeps;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.Util.Logger;
import com.bakarapp.beeppopup.BeepsGridViewLayout.OnBeepClickedListener;



public class BeepsPopup extends PopupWindow implements ViewPager.OnPageChangeListener {
	static String TAG = "com.bakarapp.BeepsPopup";
	private int mBeepTabLastSelectedIndex = -1;
	private View[] mBeepTabs;
	private BeepsPagerAdapter mBeepAdapter;
	private int keyBoardHeight = 0;
	private Boolean pendingOpen = false;
	private Boolean isOpened = false;
	OnBeepClickedListener onBeepClickedListener;
	OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;
	View rootView;
	Context mContext;	
	ProgressBar progress;
	int defaultNumBeeps = 15;
	HttpResponseListener beepListListener;
	
	private ViewPager beepsPager;
	/**
	 * Constructor
	 * @param rootView	The top most layout in your view hierarchy. The difference of this view and the screen height will be used to calculate the keyboard height.
	 * @param mContext The context of current activity.
	 */
	public BeepsPopup(View rootView, Context mContext){
		super(mContext);
		this.mContext = mContext;
		this.rootView = rootView;
		View customView = createCustomView();
		setContentView(customView);		
		setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		//default size 
		setSize((int) mContext.getResources().getDimension(R.dimen.keyboard_height), LayoutParams.MATCH_PARENT);
	}
	/**
	 * Set the listener for the event of keyboard opening or closing.
	 */
	public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener){
		this.onSoftKeyboardOpenCloseListener = listener; 
	}

	/**
	 * Set the listener for the event when any of the beep is clicked
	 */
	public void setOnBeepClickedListener(OnBeepClickedListener listener){
		this.onBeepClickedListener = listener;
	}

	
	/**
	 * Use this function to show the emoji popup.
	 * NOTE: Since, the soft keyboard sizes are variable on different android devices, the 
	 * library needs you to open the soft keyboard atleast once before calling this function.
	 * If that is not possible see showAtBottomPending() function.
	 * 
	 */
	public void showAtBottom(){
		showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
	}
	/**
	 * Use this function when the soft keyboard has not been opened yet. This 
	 * will show the emoji popup after the keyboard is up next time.
	 * Generally, you will be calling InputMethodManager.showSoftInput function after 
	 * calling this function.
	 */ 
	public void showAtBottomPending(){
		if(isKeyBoardOpen())
			showAtBottom();
		else
			pendingOpen = true;
	}

	/**
	 * 
	 * @return Returns true if the soft keyboard is open, false otherwise.
	 */
	public Boolean isKeyBoardOpen(){
		return isOpened;
	}

	/**
	 * Dismiss the popup
	 */
	@Override
	public void dismiss() {
		super.dismiss();		
	}

	/**
	 * Call this function to resize the emoji popup according to your soft keyboard size
	 */
	public void setSizeForSoftKeyboard(){
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				rootView.getWindowVisibleDisplayFrame(r);

				int screenHeight = rootView.getRootView()
						.getHeight();
				int heightDifference = screenHeight
						- (r.bottom - r.top);
				int resourceId = mContext.getResources()
						.getIdentifier("status_bar_height",
								"dimen", "android");
				if (resourceId > 0) {
					heightDifference -= mContext.getResources()
							.getDimensionPixelSize(resourceId);
				}
				if (heightDifference > 100) {
					keyBoardHeight = heightDifference;
					setSize(LayoutParams.MATCH_PARENT, keyBoardHeight);
					if(isOpened == false){
						if(onSoftKeyboardOpenCloseListener!=null)
							onSoftKeyboardOpenCloseListener.onKeyboardOpen(keyBoardHeight);
					}
					isOpened = true;
					if(pendingOpen){
						showAtBottom();
						pendingOpen = false;
					}
				}
				else{
					isOpened = false;
					if(onSoftKeyboardOpenCloseListener!=null)
						onSoftKeyboardOpenCloseListener.onKeyboardClose();
				}
			}
		});
	}

	/**
	 * Manually set the popup window size
	 * @param width Width of the popup
	 * @param height Height of the popup
	 */
	public void setSize(int width, int height){
		setWidth(width);
		setHeight(height);
	}

	private View createCustomView() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.choosebeep_layout, null, false);
		beepsPager = (ViewPager) view.findViewById(R.id.beeps_pager);
		progress = (ProgressBar) view.findViewById(R.id.beepslist_loading);
		beepsPager.setOnPageChangeListener(this);
		
		if(BeepList.getInstance().getBeepListCount()==0)
		{
			progress.setVisibility(View.VISIBLE);
			int level = ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL);			
			HttpRequest getBeepsReq = new GetBeepListRequest(level, defaultNumBeeps, getBeepListener());
			HttpClient.getInstance().executeRequest(getBeepsReq);			
		}
		
		mBeepAdapter = new BeepsPagerAdapter(
				Arrays.asList(
						new BeepsGridViewLayout(mContext, FavBeeps.getFavBeeps(),  this,0),
						new BeepsGridViewLayout(mContext, BeepList.getInstance().getBeepListAt(0),  this,1),
						new BeepsGridViewLayout(mContext, BeepList.getInstance().getBeepListAt(1),  this,2),
						new BeepsGridViewLayout(mContext, BeepList.getInstance().getBeepListAt(2),  this,3),
						new BeepsGridViewLayout(mContext, BeepList.getInstance().getBeepListAt(3),  this,4),
						new BeepsGridViewLayout(mContext, BeepList.getInstance().getBeepListAt(4),  this,5)
						)
				);
		beepsPager.setAdapter(mBeepAdapter);
		mBeepTabs = new View[6];
		mBeepTabs[0] = view.findViewById(R.id.beeps_tab_0_recents);
		mBeepTabs[1] = view.findViewById(R.id.beeps_tab_1_people);
		mBeepTabs[2] = view.findViewById(R.id.beeps_tab_2_nature);
		mBeepTabs[3] = view.findViewById(R.id.beeps_tab_3_objects);
		mBeepTabs[4] = view.findViewById(R.id.beeps_tab_4_cars);
		mBeepTabs[5] = view.findViewById(R.id.beeps_tab_5_punctuation);
		for (int i = 0; i < mBeepTabs.length; i++) {
			final int position = i;
			mBeepTabs[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					beepsPager.setCurrentItem(position);
				}
			});
		}	
	
		beepsPager.setCurrentItem(1, false);
		
		view.findViewById(R.id.beeps_refresh).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				progress.setVisibility(View.VISIBLE);
				int level = ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL);			
				HttpRequest getBeepsReq = new GetBeepListRequest(level, defaultNumBeeps, getBeepListener());
				HttpClient.getInstance().executeRequest(getBeepsReq);				
			}
		});
		return view;
	}

	
	@Override
	public void onPageScrolled(int i, float v, int i2) {
	}

	@Override
	public void onPageSelected(int i) {
		if (mBeepTabLastSelectedIndex == i) {
			return;
		}
		switch (i) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			if (mBeepTabLastSelectedIndex >= 0 && mBeepTabLastSelectedIndex < mBeepTabs.length) {
				mBeepTabs[mBeepTabLastSelectedIndex].setSelected(false);
			}
			mBeepTabs[i].setSelected(true);
			mBeepTabLastSelectedIndex = i;			
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int i) {
	}

	private static class BeepsPagerAdapter extends PagerAdapter {
		private List<BeepsGridViewLayout> views;
		
		public BeepsPagerAdapter(List<BeepsGridViewLayout> views) {
			super();
			this.views = views;
		}
		
		public void updateItems()
		{
			Logger.i(TAG, "update grid items");
			for(int i=1;i<views.size();i++) //dont update fav
			{
				views.get(i).updateGridView(BeepList.getInstance().getBeepListAt(i));
			}
		}

		@Override
		public int getCount() {
			return views.size();
		}


		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = views.get(position).rootView;
			((ViewPager)container).addView(v, 0);
			return v;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			((ViewPager)container).removeView((View)view);
		}

		@Override
		public boolean isViewFromObject(View view, Object key) {
			return key == view;
		}
	}

	/**
	 * A class, that can be used as a TouchListener on any view (e.g. a Button).
	 * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
	 * click is fired immediately, next before initialInterval, and subsequent before
	 * normalInterval.
	 * <p/>
	 * <p>Interval is scheduled before the onClick completes, so it has to run fast.
	 * If it runs slow, it does not generate skipped onClicks.
	 */
	public static class RepeatListener implements View.OnTouchListener {

		private Handler handler = new Handler();

		private int initialInterval;
		private final int normalInterval;
		private final View.OnClickListener clickListener;

		private Runnable handlerRunnable = new Runnable() {
			@Override
			public void run() {
				if (downView == null) {
					return;
				}
				handler.removeCallbacksAndMessages(downView);
				handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
				clickListener.onClick(downView);
			}
		};

		private View downView;

		/**
		 * @param initialInterval The interval before first click event
		 * @param normalInterval  The interval before second and subsequent click
		 *                        events
		 * @param clickListener   The OnClickListener, that will be called
		 *                        periodically
		 */
		public RepeatListener(int initialInterval, int normalInterval, View.OnClickListener clickListener) {
			if (clickListener == null)
				throw new IllegalArgumentException("null runnable");
			if (initialInterval < 0 || normalInterval < 0)
				throw new IllegalArgumentException("negative interval");

			this.initialInterval = initialInterval;
			this.normalInterval = normalInterval;
			this.clickListener = clickListener;
		}

		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				downView = view;
				handler.removeCallbacks(handlerRunnable);
				handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
				clickListener.onClick(view);
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
				handler.removeCallbacksAndMessages(downView);
				downView = null;
				return true;
			}
			return false;
		}
	}

	public interface OnSoftKeyboardOpenCloseListener{
		void onKeyboardOpen(int keyBoardHeight);
		void onKeyboardClose();
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
				mBeepAdapter.updateItems();		
			}
		}
		
	}
	
}
