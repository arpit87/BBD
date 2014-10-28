package com.BhakBhosdi.Activities;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.BhakBhosdi.R;
import com.BhakBhosdi.HTTPClient.CreateUserRequest;
import com.BhakBhosdi.HTTPClient.HttpClient;
import com.BhakBhosdi.HTTPClient.HttpResponseListener;
import com.BhakBhosdi.HelperClasses.AlertDialogBuilder;
import com.BhakBhosdi.HelperClasses.ProgressHandler;
import com.BhakBhosdi.HelperClasses.ThisAppConfig;
import com.BhakBhosdi.Util.StringUtils;

public class LoginActivity extends Activity{
	
	TextView appTitle ;
	EditText nickname ;
	Button startBackChodiButton;
	private AddUserResponseListener instance = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        
        appTitle = (TextView) findViewById(R.id.login_apptitle);
        nickname = (EditText) findViewById(R.id.login_nickname);
        startBackChodiButton = (Button) findViewById(R.id.login_startbutton);
        
        final SpannableStringBuilder bakchodapp = new SpannableStringBuilder("bakchodapp");
        final ForegroundColorSpan light_brown = new ForegroundColorSpan(Color.rgb(238, 228, 117));
        final ForegroundColorSpan dark_brown = new ForegroundColorSpan(Color.rgb(81, 58, 50));       // Span to set text color to some RGB value
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);        // Span to make text bold
        bakchodapp.setSpan(light_brown, 0, 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE);  // Set the text color for first 4 characters
        bakchodapp.setSpan(dark_brown, 7, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
        bakchodapp.setSpan(bss, 0, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);    // make them also bold
        appTitle.setText(bakchodapp);
        
        nickname.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				nickname.setHintTextColor(getResources().getColor(android.R.color.transparent));
				
			}
		});
        
        startBackChodiButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String uuid = ThisAppConfig.getInstance().getString(ThisAppConfig.APPUUID);
				String nicknameStr = nickname.getText().toString();
				
				if(StringUtils.isBlank(nicknameStr))
					AlertDialogBuilder.showOKDialog(LoginActivity.this, "Oyy #$%@#", "Nick name to daal de bhai...");
				else
				{
					ProgressHandler.showInfiniteProgressDialoge(LoginActivity.this, nicknameStr + " bhai", "1 min bas phir bahut backchodi karna", getListener());
					CreateUserRequest addUsrReq = new CreateUserRequest(uuid, "", nicknameStr, getListener());
					HttpClient.getInstance().executeRequest(addUsrReq);
				}
				
			}
		});
        
	}
	
	public HttpResponseListener getListener()
	{
		if(instance == null)
			instance = new AddUserResponseListener();
		return instance;
	}
	
	 class AddUserResponseListener extends HttpResponseListener
	{

		@Override
		public void onComplete(Object json) {
			finish();			
		}		
	}

}
