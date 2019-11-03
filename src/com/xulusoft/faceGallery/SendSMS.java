package com.xulusoft.faceGallery;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SendSMS extends Activity {


    private String phoneNumber="", contactName="";
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms);
		phoneNumber=getIntent().getStringExtra("phoneNumber");
		contactName=getIntent().getStringExtra("contactName");
		EditText etxt = (EditText)findViewById(R.id.txtPhone);
		etxt.setEnabled(false);
		etxt.setText(contactName+" ("+phoneNumber+")");
	}

 
    public void DoSendSMS(View button){
    	String sms = ((EditText)findViewById(R.id.txtSMS)).getText().toString();
    	sendSMS(phoneNumber, sms);
    	ToastMessage(getString(R.string.finishSMS));    	
    	finish();
    }
    
    private void sendSMS(String phoneNumber, String message)
    {        
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, FacesActivity.class), 0);                
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);        
    }   
    
    
	private void		Send(String phoneNum, String sms){
    	Uri smsUri = 	Uri.fromParts("sms", phoneNum, null); 
    	Intent intent = new Intent(Intent.ACTION_VIEW, smsUri); 
    	intent.putExtra("sms_body", sms); 
    	intent.setType("vnd.android-dir/mms-sms");  
    	startActivity(intent);
    }
    private void 		ToastMessage(String str)
	{
		Toast toast = Toast.makeText(this, str, 3);
		toast.show();
	}
}
