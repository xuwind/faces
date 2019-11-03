package com.xulusoft.faceGallery;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.widget.Toast;

public class AddContactQR extends Activity {
	private String content, format;
	private static final String BS_PACKAGE = "com.google.zxing.client.android";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		DoReadQR();
	}
	
    private AlertDialog       DoReadQR(){
    	Intent intent = new Intent(BS_PACKAGE+".SCAN");  
        //intent.addCategory(Intent.CATEGORY_DEFAULT);  
    	//intent.setPackage("com.google.zxing.client.android");
    	//intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
    	intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

    	String packageName = functions.findTargetAppPackage(intent, this, BS_PACKAGE);
    	if (packageName == null)
    	{
    		return functions.showDownloadDialog(this,BS_PACKAGE);
    	}
    	this.startActivityForResult(intent,0);
    	return null;
    }
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		 if (requestCode == 0) {
		    if (resultCode == RESULT_OK) {
		      content = intent.getStringExtra("SCAN_RESULT");
		      format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		         // Handle successful scan
		   } else if (resultCode == RESULT_CANCELED) {
		         // Handle cancel
			   content="cancelled";
		   }
		}
		 try {
			 if (content!="cancelled")
			AddContact(content);
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		finish();
	}
    
    private void 		AddContact(String str) throws Exception, OperationApplicationException{
    	String[] arr= str.split("\n");
    	String FullName = arr[0].trim();
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();
 
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, FullName).build());
        for (int i=1; i<arr.length; i++)
        {
        	if (arr[i].trim()=="") continue;
        	if (arr[i].indexOf("@")>0){
        	
        		String pStr = arr[i].trim();
        		int tint = 3;
            	String[] arr1=pStr.split(":");
            	if (arr1.length>1){
            		pStr = arr1[1].trim();
            		String type = arr1[0].trim();
             		if (type.contains("H"))
            			tint =1;
            		if (type.contains("W"))
            			tint =2;
        		}            	
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, pStr)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, 
                        		tint)
                        .build());
        	}
            else{
           		String pStr = arr[i].trim();
        		int tint = 7;
            	String[] arr1=pStr.split(":");
            	if (arr1.length>1){
            		pStr = arr1[1].trim();
            		String type = arr1[0].trim(); 
            		if (type.contains("H"))
            			tint =1;
            		if (type.contains( "M"))
            			tint =2;
            		if (type.contains("FW"))
            			tint =4;
            		if (type.contains("FH"))
            			tint =5;

        		}         	
            	int h=ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME;
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, pStr)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, 
                                tint)
                        .build());
            }
        	
         }
        ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        if (res!=null && res[0]!=null)
            functions.ToastMessage(this, getString(R.string.contactAdded));
        else 
        	functions.ToastMessage(this, getString(R.string.contactNotAdded));
 /*
        if(!company.equals("") && !jobTitle.equals(""))
        {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .build());
        }
        
   */ 
    
    }
}
