package com.xulusoft.faceGallery;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;


import android.app.Activity;
import android.app.AlertDialog;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;

import android.database.Cursor;
import android.database.SQLException;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class FacesActivity extends Activity {
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}
	/** Called when the activity is first created. */
	private GestureLibrary gestureLib;
	private faceScanner runnableScan=null;
	private faceReader runnableRead=null;
	private	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private final LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	private Vector<String> sV = new Vector<String>(), phones = new Vector<String>(),emails = new Vector<String>();
	private Vector<Integer> ids = new Vector<Integer>(), cids = new Vector<Integer>(), phoneIDs= new Vector<Integer>(), emailIDs= new Vector<Integer>(),smsIDs= new Vector<Integer>();
	static private BitmapFactory.Options bmpOption = new BitmapFactory.Options();
	private Vector<Bitmap> faces = new Vector<Bitmap>();
	private int maxFaceByimage = 5, position=0;
	private String contactID, contactName;
	public int faceX = 120,ifaceX=120, maxFaces=50, rcgSize=2000000, totalFaces=0,showIndex=0, pageSize=0, selectedID=0;
	static public int colShow=0,rowShow=0;
	public int[] pix = new int[1000];
	public int borderColor= 0xff00ff00,heightS;
	private String fPath="";
	private Bitmap bmp;
	private TextView tvTotal, tvDate;
	private Thread faceRun=null, readRun=null;
	private float scaleFactor=1.6f,confidence=0.50f;
	private String[] imageExtensions = new String[]{"jpg"};
	private Handler handler=new Handler(), handler1, handler2=new Handler();
	private String cacheFolder="DCIM/.thumbnails";// 
	private String MY_AD_UNIT_ID="a14f27af435c9a4", scanFolder="";
	private GridView gv;
	private static final int PICK_CONTACT = 0;
	private boolean noStop = true,  stopScan=true;
	public boolean faceScanning=true, viewContact=false;
	public volatile boolean hasNew=true;
	private LinearLayout adLL;
	private static FacesActivity ACTIVE_INSTANCE=null;
	private static final String BS_PACKAGE = "com.google.zxing.client.android";
	private static final String CONTACT_PATH="from contact";
	private ImageAdapter faceAdapter;
	private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    SwipeReceiver swipereceiver;

    
    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case PhotoScanService.MSG_SET_INT_VALUE:
                //textIntValue.setText("Int Message: " + msg.arg1);
                break;
            case PhotoScanService.MSG_SET_STRING_VALUE:
                String str1 = msg.getData().getString("str1");
                //textStrValue.setText("Str Message: " + str1);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    
	@Override
	public void 		onCreate(Bundle savedInstanceState) {
		Log.i("event","face onCreate");
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // (NEW)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        setContentView(R.layout.main);
        InitializePix();
	   	Display display = getWindowManager().getDefaultDisplay(); 
	   	int width = display.getWidth();
	   	int heightAd=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
	   	
	   	heightS=display.getHeight()-heightAd;
	   	int hwd = width;
	   	if (width>display.getHeight()) hwd = display.getHeight();
	   	ifaceX = (int)Math.floor((double)(hwd)/5);
    	fileDialog.dHeight = (int)Math.floor((double)heightS*0.8);
        gv = (GridView)findViewById(R.id.gridView1);
        registerForContextMenu(gv);
        gv.setOnTouchListener(new GridViewOnTouchListener());
        tvTotal = (TextView)findViewById(R.id.tvTotal);
        tvDate = (TextView)findViewById(R.id.tvDate);
        
        colShow=(int)Math.floor((double)width/ifaceX);
        rowShow=(int)Math.floor((double)heightS/ifaceX);
        showIndex=0;
        pageSize =colShow*rowShow;
        readSetting();        
        gv.setNumColumns(colShow);
        bmpOption.inPreferredConfig = Bitmap.Config.RGB_565;
        
        swipereceiver = new SwipeReceiver();
        gestureDetector = new GestureDetector(new FaceGestureListner(swipereceiver));
                
        gv.setOnTouchListener(new GridViewOnTouchListener());
        if (faces.size()>0)	gv.setAdapter(new ImageAdapter(this));
                
        gv.setOnItemLongClickListener(new OnItemLongClickListener() 
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,	int pos, long arg3) {
				// TODO Auto-generated method stub
				DoSelection(pos);
				return false;
				}
			}
        );
        
        gv.setOnItemClickListener(new OnItemClickListener() 
			{
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
				{                
					DoSelection(position);
				}
			}
		);
        faceAdapter= new ImageAdapter(this);
        gv.setAdapter(faceAdapter);
        DoRead();
        ACTIVE_INSTANCE =this;
     }

	@Override
	protected void 		onRestart() {
		// TODO Auto-generated method stub
		Log.i("event","face onRestart");
		super.onRestart();

	}

	@Override
	protected void 		onStart() {
		// TODO Auto-generated method stub
		
		super.onStart();
		Log.i("event","face onStart");
	}
	
	@Override
	protected void 		onResume() {
		// TODO Auto-generated method stub
		Log.i("event","face onResume start");
		if (readRun !=null) {
			if (readRun.isAlive())
				runnableRead.onResume();
			else
			{
				ClearThread(readRun);
				DoReadRun();
			}
		}
		else
			DoReadRun();
		if (faceRun !=null) {
			if (!faceRun.isAlive()) {
				ClearThread(faceRun);
			}
		}
		if (faceScanning && faceRun ==null) DoScanRun();
		super.onResume();
		Log.i("event","face onResume");
	}
	
    @Override
	protected void 		onPause() {
		// TODO Auto-generated method stub
    	//ClearThread(readRun);
    	if (readRun !=null) {
    		if (readRun.isAlive()) {
    			runnableRead.onPause();
    		}
    	}
		super.onPause();
		Log.i("event","face onPause");
	}

	@Override
	protected void 		onStop() {
		super.onStop();
		Log.i("event","face onStop");
	}

    @Override
	protected void 		onDestroy() {
		// TODO Auto-generated method stub
    	stopScan=true;
    	ACTIVE_INSTANCE = null;  
    	ClearThread(readRun);
    	ClearThread(faceRun);
    	try
    	{
    		if (bmp != null) bmp.recycle();
    	}
    	catch(Exception eee){};
    	try
    	{
    		for(int i=0; i<faces.size(); i++) {
    			faces.elementAt(i).recycle();
    		}
    		faces.clear();
    		sV.clear();
    		ids.clear();
		}
		catch(Exception ee){};               
		super.onDestroy();
		Log.i("event","face onDestroy");
	}   
	
    @Override
    public boolean 		onContextItemSelected(MenuItem item) {
      //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    //showMessage(item.getItemId()+"");
    int itemID =item.getItemId();
   	if (phoneIDs.contains(itemID)){
    	MakeCall(item.getTitle()+"");
    	return true;
    }
   	
   	if (emailIDs.contains(itemID)){
    	DoSendEmail(item.getTitle()+"");
    	return true;
    }
   	
   	if (smsIDs.contains(itemID)){
    	DoSendSMS(item.getTitle()+"");
    	return true;
    }
      switch (item.getItemId()) {
      case R.id.delete:
        OnDeleteDialog();
        return true;
      case R.id.open:
        DoOpenImage(position);
        return true;
      case R.id.addContact:
        DoSelectContact();
        return true;
      case R.id.openContact:
          OpenContact();
          return true;   
      case R.id.QRCode:
          DoQRCode();
          return true;   
      default:
        return super.onContextItemSelected(item);
      }
    }    
    
    @Override
    public void 		onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
      //GetContact();
     //menu.setHeaderIcon(iconRes);
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.contextmenu, menu);
      String hstr=" '"+contactName+"'";
      
      if (phones.size()>0){
    	  SubMenu sMn = menu.addSubMenu(0,1000,0, getString(R.string.phoneCall) +hstr);
    	  //sMn.setIcon(R.drawable.call);
    	  for (int i=0; i<phones.size(); i++)
    	  {
    		  if ( i >=phoneIDs.size()) break;
    		  String hstr1=phones.elementAt(i);
    		  
    		  if (hstr1.contains("FW") || hstr1.contains("FH")) continue;
    		  sMn.add(0,phoneIDs.elementAt(i), 0,hstr1);
    	  }
    	  SubMenu smsMn = menu.addSubMenu(0,1000,0, getString(R.string.sendSMS));
    	  //smsMn.setIcon(R.drawable.sms);
    	  for (int i=0; i<phones.size(); i++)
    	  {
    		  if ( i >=smsIDs.size()) break;
    		  String hstr1=phones.elementAt(i);
    		  if (hstr1.contains("FW") || hstr1.contains("FH")) continue;
    		 
    		  smsMn.add(0,smsIDs.elementAt(i), 0,hstr1);
    	  }
      }
      
      boolean hasPhone = (phones.size()>0);
      if (emails.size()>0){
    	  SubMenu emailMn = menu.addSubMenu(0,1000,0, getString(R.string.sendEmail) +(hasPhone?"":hstr));
    	  //emailMn.setIcon(R.drawable.email);
    	  for (int i=0; i<emails.size(); i++)
    	  {
    		  MenuItem hi=emailMn.add(0,emailIDs.elementAt(i), 0,emails.elementAt(i));
    		  //hi.setIcon(R.drawable.email);
    	  }
      }
      if (Integer.parseInt(contactID)>0){
    	  menu.add(0,R.id.openContact,0, getString(R.string.openContact));
    	  menu.add(0,R.id.QRCode,0, getString(R.string.QRCode));
      }
      if (sV.elementAt(position).trim().indexOf(CONTACT_PATH) < 0)
      {
    	  menu.add(0,R.id.open,0,getString(R.string.open));
      	  menu.add(0,R.id.addContact,0,getString(R.string.addContact));
      }
  	  menu.add(0,R.id.delete,0,getString(R.string.delete));
    } 
   
    @Override
    public boolean 		onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        /*
        switch (rcgSize){
        case 0:
        	menu.findItem(R.id.large).setChecked(true);
        	break;
        case 2000000:
        	menu.findItem(R.id.middle).setChecked(true);
        	break;
        case 500000:
        	menu.findItem(R.id.small).setChecked(true);
        	break;
        default:
        	menu.findItem(R.id.middle).setChecked(true);
        }
        */
        if (scanFolder =="")
        	menu.findItem(R.id.cameraFolder).setChecked(true);
        else
        	menu.findItem(R.id.selectFolder).setChecked(true);
        if (viewContact)
        	menu.findItem(R.id.viewContacts).setChecked(true);
        else
        	menu.findItem(R.id.viewFaces).setChecked(true);
       	menu.findItem(R.id.start).setChecked(false);
       	menu.findItem(R.id.clear).setChecked(false);
       	menu.findItem(R.id.stop).setChecked(false);
        return true;
    }
    
	@Override
	public boolean 		onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {
               
        case R.id.selectFolder:
        	OnSelectFolder();
        	item.setChecked(true);
        	break;     
        case R.id.cameraFolder:
        	item.setChecked(true);
        	OnSelectGallery();
        	break;     
        case R.id.camera:
        	OpenCamera();
            break;
        case R.id.clear:
        	OnClearDialog();
        	break;
        	/*
        case R.id.large:
        	rcgSize=0;
          	item.setChecked(true);
        	saveSetting();
        	break;  
        	
        case R.id.middle:
        	rcgSize=2000000;
        	item.setChecked(true);
        	saveSetting();
        	break;        
        case R.id.small:
        	rcgSize=500000;
        	item.setChecked(true);
        	saveSetting();
        	break;
        */
        case R.id.start:
        	DoScanRun();
        	break;
        case R.id.stop:
        	OnStopDialog();
        	break;                   	
        case R.id.viewFaces:
        	viewContact = false;
        	item.setChecked(true);
        	saveSetting();
        	DoRead();
        	break;
        case R.id.viewContacts:
        	viewContact = true;
        	showIndex=0;
        	item.setChecked(true);
        	saveSetting();
        	DoRead();
        	break;
            	
        case R.id.QRRead:
        	DoQRRead();
        	break;        	
        default:
        	return super.onOptionsItemSelected(item);
        }
        return false;
	}
    
	@Override
	public void 		onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i("event", "onrestoreinstancestate");
	  super.onRestoreInstanceState(savedInstanceState);
	  // Restore UI state from the savedInstanceState.
	  // This bundle has also been passed to onCreate.
	  rcgSize = savedInstanceState.getInt("rcgSize");
	  faceScanning = savedInstanceState.getBoolean("faceScanning");
	  viewContact = savedInstanceState.getBoolean("viewContact");
 	}
	
	@Override
	public void 		onSaveInstanceState(Bundle savedInstanceState) {
	  savedInstanceState.putInt("rcgSize", rcgSize);
	  savedInstanceState.putBoolean("faceScanning", faceScanning);
	  savedInstanceState.putBoolean("viewContact", viewContact);
	  super.onSaveInstanceState(savedInstanceState);
	}
	

	public void			Add_ContactID(int id){
		dbManager db = new dbManager(this);
		db.RunCommand("update " +dbManager.DB_Table +" set Contact_ID=" +contactID+" where _id=" + id);
		db.close();
		db=null;
	}
	
	private void		Add_ContactPhoto(int id){
		byte[] photo = null;
		Uri rawContactUri = GetRawContactUri();
    	dbManager db = new dbManager(this,true);
    	photo=db.GetRawPhoto(id);
    	db.close();
    	db=null;
    	if (photo==null) return;
		ContentValues values = new ContentValues(); 
		int photoRow = -1; 
		String where = ContactsContract.Data.RAW_CONTACT_ID + " == " + 
		    ContentUris.parseId(rawContactUri) + " AND " + Data.MIMETYPE + "=='" + 
		    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'"; 
		Cursor cursor = managedQuery(
		        ContactsContract.Data.CONTENT_URI, 
		        null, 
		        where, 
		        null, 
		        null); 
		int idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID); 
		if(cursor.moveToFirst()){ 
		    photoRow = cursor.getInt(idIdx); 
		} 
		cursor.close(); 
		cursor = null;
		values.put(ContactsContract.Data.RAW_CONTACT_ID, 
		        ContentUris.parseId(rawContactUri)); 
		values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1); 
		values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, photo); 
		values.put(ContactsContract.Data.MIMETYPE, 
		        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE); 
		if(photoRow >= 0){ 
		    this.getContentResolver().update(ContactsContract.Data.CONTENT_URI,values,ContactsContract.Data._ID + " = " + photoRow, null); 
		} 
		else { 
		    this.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values); 
	    }
	     
	} 

	class ImageFilter   implements FileFilter {
    	public boolean accept(File folder) 
        { 
    		
            try 
            { 
                //Checking only directories, since we are checking for files within 
                //a directory 
                if(folder.isDirectory()) 
                { 
                   return true;
                } 
                else{
                    for (String ext : imageExtensions) 
                    { 
                        if (folder.getName().endsWith("." + ext)) return true; 
                    }                 
                }
                	
                return false; 
            } 
            catch (SecurityException e) 
            { 
                Log.v("debug", "Access Denied"); 
                return false; 
            } 
        } 	
    }
	
	private void 		Clear(){
		
		ClearThread(faceRun);
		ClearThread(readRun);
		dbManager cDB = new dbManager(this);
		cDB.deleteAll();
		cDB.close();
		cDB.clear();
		showIndex=0;
		saveSetting();
		totalFaces=0;
 		DoRead();
	}
	
	
	private void		ClearThread(Thread hthr){
		Log.i("event", "kill thread");
	  	try
    	{
    		Thread ht = hthr;
    		hthr = null;
    		ht.interrupt();
    	}
    	catch(Exception e5){};
	}
	
	private void		DeleteItem(int i){
		dbManager cDB = new dbManager(this);
		cDB.delete(i);
		cDB.close();
		cDB.clear();
 		handler.post(new Runnable() {
			@Override
			public void run() {
					DoRead();
				}
			}
   		);     	   	 
	}

	void doBindService() {
        bindService(new Intent(this, PhotoScanService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        
    }

	void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            //textStatus.setText("Unbinding.");
        }
    }
	
	public void			DoMenuScanRun(MenuItem view){
		showMessage("hi");
    	faceScanning = true;
		DoScanRun();
	}
	
	public void			DoNextPage(Boolean next){
		if (next){
			if(showIndex >= (totalFaces-pageSize)){
				functions.ToastMessage(this, getString(R.string.endPage));
				return;
			}
			else{
				showIndex = showIndex+pageSize;
				DoRead();
				return;
			}
		}
		if (showIndex==0){
			functions.ToastMessage(this, getString(R.string.startPage));
			return;
		}
		else{
			showIndex = showIndex-pageSize;
			DoRead();
			return;
		}
	}
	
    private void 		DoOpenImage(int pos){
    	
    	OpenImage(sV.elementAt(pos));
    }
	
    private void		DoPlayContact(){
    	Intent playContact = new Intent(this, ShotContactActivity.class);
    	//playContact.putExtra("contactName", contactName);
    	startActivity(playContact);
    }
    
    private AlertDialog		DoQRCode(){
    	Intent intent = new Intent(BS_PACKAGE+".ENCODE");  
        intent.addCategory(Intent.CATEGORY_DEFAULT);  
        intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");  
        String str = contactName +"\n";
        for (int i=0;i<phones.size(); i++){
        	str += phones.elementAt(i);
        		str+="\n";
        			
        }
        for (int i=0;i<emails.size(); i++){
        	str += emails.elementAt(i);
        	if (i!= (emails.size()-1))
        		str+="\n";
        			
        }
        intent.putExtra("ENCODE_DATA",str);  
       
        String targetAppPackage = functions.findTargetAppPackage(intent, this, BS_PACKAGE);
        if (targetAppPackage == null) {
          return functions.showDownloadDialog(this, BS_PACKAGE);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(intent);
        return null;
    }
    
    private void		DoQRRead(){
    	Intent intent = new Intent(this, AddContactQR.class);
    	startActivity(intent);     	
    }
    
    private void 		DoRead(){
		dbManager db = new dbManager(this,true);
    	try
    	{
    		if (viewContact){
    			totalFaces = db.count("select _id from file_faces where deleted<>1 and faceNum>0 and Contact_ID>0;");
    			db.GetAll("select _id, filePath,img, lastDate, faceNum, Contact_ID from file_faces where deleted<>1 and faceNum>0 and Contact_ID>0 order by LastDate DESC"+" LIMIT "+pageSize+" OFFSET "+showIndex +";" );
    		}
    		else{
    			totalFaces = db.count("select _id from file_faces where deleted<>1 and faceNum>0;");
    			db.GetAll("select _id, filePath,img, lastDate, faceNum, Contact_ID from file_faces where deleted<>1 and faceNum>0 order by LastDate DESC"+" LIMIT "+pageSize+" OFFSET "+showIndex +";" );
    		} 
    		
    		faces.clear();
    		sV.clear();
    		ids.clear();
    		faces = db.faces;
    		sV = db.paths;
    		ids = db.ids;
    		cids= db.cids;
    	}
    	catch(Exception eee) {
    		Log.i("event", "load db error"+eee.getMessage());	
    	}
    	finally
    	{
    		db.close();
        	db=null;
    	}
		hasNew = false;
    	try
    	{
    		refreshGrid();
    	}
    	catch(Exception ee){};
    }
    
    private void		DoScan(){
		dbManager db = new dbManager(this);
		Log.i("event", "scanning...");
		try
		{
			ImageFilter fImages = new ImageFilter(); 
			if (scanFolder==""){
				findFiles(fImages, db);
			}
			else{
				File startDir = new File(scanFolder);
				findFiles(startDir,fImages, db);
			}
		}
		catch(Exception eee) {}
		finally {
			db.close();
			db=null;
		}
		stopScan = false;
    }
    
	private void 		DoReadRun(){
		Log.i("event","readrun started");
		runnableRead = new faceReader();
		readRun= new Thread(runnableRead);  
		readRun.setPriority(Thread.MAX_PRIORITY);
		readRun.start();
	}
    
	private void 		DoScanRun(){
		Log.i("event","scanrun started");
		tvDate.setText(getString(R.string.inPorcess));
    	faceScanning=true;
    	saveSetting();
		runnableScan = new faceScanner();
		faceRun= new Thread(runnableScan);  
		faceRun.setPriority(Thread.NORM_PRIORITY);
		faceRun.start();
	}
    
    private void		DoSelectContact(){

    	try
    	{
    		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        	startActivityForResult(intent, PICK_CONTACT);
        }             
    	catch (ActivityNotFoundException ae) 
    	{
    		showMessage(ae.getMessage());
    	}  
    }
	
    private void		DoSelection(int pos){
    	selectedID = ids.elementAt(pos);
       	position = pos;
       	GetContact();
    	String hstr=sV.elementAt(pos);
    	//String[] arr=hstr.split("/");
    	try
    	{
    		//File hF = new File(hstr);
    		//Date hdate = new Date(hF.lastModified());
    		if (contactName != "")
    			tvDate.setText(contactName);
    		else{
    			dbManager db = new dbManager(this,true);
    			tvDate.setText(getString(R.string.dateTime) + df.format( db.GetFileDate(ids.elementAt(pos))));
    	    	db.close();
    			db=null;
    		}
    	}
    	catch(Exception e1){};
     }
    
    private void		DoSendEmail(String email){
    	String str = email.split(":")[1].trim();
    	SendEmail(str,"","");
    }

    private void		DoSendSMS(String phoneNumber){
    	String str = phoneNumber.split(":")[1].trim();
    	Intent smsIntent = new Intent(this, SendSMS.class);
    	smsIntent.putExtra("phoneNumber", str);
    	smsIntent.putExtra("contactName", contactName);
    	startActivity(smsIntent);
    }
       
	class 				faceReader implements Runnable{
		private Object lock;
	    private boolean paused;
	    public boolean completed;

	    public faceReader() {
	        lock = new Object();
	        paused = false;
	        completed = false;
	    }

	    public void onPause() {
	        synchronized (lock) {
	            paused = true;
	            Log.i("event", "try read paused");
	        }
	    }

	    public void onResume() {
	        synchronized (lock) {
	            paused = false;
	            Log.i("event", "read resumed");
	            lock.notifyAll();
	        }
	    }
		@Override
	    public void run() {
	    	while(!Thread.currentThread().isInterrupted()  && !completed){
	    		Log.i("event", "read started");
	    		try {
	    			if (hasNew)
	    			{
	    		   		if (ACTIVE_INSTANCE != null) {
	    		   			try
	    		   			{
	    		   				DoRead();
	    		   			}
	    		   			catch(Exception eeeee) {
	    		   			}
						}
	    			}
	                Thread.sleep(10000);
	            } 
	            catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	            catch(Exception e){}
	    		synchronized(lock){
	    			while(paused){
	    				try{
	    					Log.i("event", "read really paused");
	    					lock.wait();
	    				}
	    				catch(InterruptedException ea){};
	    			}
	    		}
	         }
	    }
	}
  
	class 				faceScanner implements Runnable{
		private Object lock;
	    private boolean paused;
	    public boolean completed;

	    public faceScanner() {
	        lock = new Object();
	        paused = false;
	        completed = false;
	    }

	    public void onPause() {
	        synchronized (lock) {
	            paused = true;
	            lock.notifyAll();
	        }
	    }

	    public void onResume() {
	        synchronized (lock) {
	            paused = false;
	            lock.notifyAll();
	        }
	    }

		// @Override
	    public void run() {
			try
			{
				SearchContactPhoto();
			}
			catch(Exception e2){};
	    	while(!Thread.currentThread().isInterrupted() && !completed){
	    		try {
	    			DoScan();
	    			Thread.sleep(10000);
	    		} 
	            catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	            catch(Exception e){}
	    		
	    		synchronized(lock){
	    			while(paused){
	    				try{
	    					lock.wait();
	    				}
	    				catch(InterruptedException ea){};
	    			}
	    		}
	         }
	    }
	}
    
	private void		findFiles(FileFilter ff, dbManager db){
		//String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,MediaStore.Images.ImageColumns.DATA,MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,MediaStore.Images.ImageColumns.DATE_TAKEN,MediaStore.Images.ImageColumns.MIME_TYPE};     
		String[] projection = new String[]{MediaStore.Images.ImageColumns.DATA,MediaStore.Images.ImageColumns.MIME_TYPE};
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"); 
        if(cursor != null){
            //cursor.moveToFirst();
            while (cursor.moveToNext()){
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int ind = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE);
                String mimeStr = cursor.getString(ind);
                if (mimeStr.indexOf("image")>=0){
                	String fPath = cursor.getString(index);
                	findFaces(fPath,db);
                	//Log.i("Image file path", fPath+" -"+mimeStr);
                }
            }
            cursor.close();
            cursor=null;
        }	
	}
	
    private void 		findFiles(File hFile, FileFilter ff, dbManager db){
    	if (hFile==null) return;
    	File[] ffs = null;
    	try
    	{
    		ffs =hFile.listFiles(ff);
    	}
    	catch(Exception e1){
    		return;
    	}
    	if (ffs == null || ffs.length ==0) return;
    	
    	Arrays.sort( ffs, new Comparator()
    	{
    	    public int compare(Object o1, Object o2) {

    	        if (((File)o1).lastModified() > ((File)o2).lastModified()) {
    	            return -1;
    	        } else if (((File)o1).lastModified() < ((File)o2).lastModified()) {
    	            return +1;
    	        } else {
    	            return 0;
    	        }
    	    }
    	}); 
    	
    	for (File f1 : ffs){
    		if (stopScan) return;
    		String hstr="";
    		try{
    			hstr=f1.getAbsolutePath();
    		}
    		catch(Exception e0){
    			continue;
    		};
	
    		if (f1.isDirectory()){
    			if (hstr.indexOf(cacheFolder)>=0)
    				continue;
    			else
    			{
    				if (noStop)
    					findFiles(f1,ff,db);
    			}
    		}
    		else
    		{

   				if (!db.Exist(hstr)){
  					long lDate = Math.round((double)f1.lastModified()/1000);
   					try
   					{
   						findFaces(f1,lDate,db);
   					}
   					catch(Exception ee){
    				}
   				}
    		}
    	}
    }
  
    private void 		findFaces(String filePath,dbManager db){
    		//Log.i("Scanning",filePath);
    		if (db.Exist(filePath)) return;
    		File file = new File(filePath);
    		if (file.isDirectory()) return;
			long lDate = Math.round((double)file.lastModified()/1000);
				try
				{
					findFaces(file,lDate,db);
				}
				catch(Exception ee){
				}
    }
    
    private void 		findFaces(File file, long lDate, dbManager db){
    	try
    	{
    		
    		fPath = file.getAbsolutePath();
    		bmp=Bitmap.createBitmap(resize(fPath));
    		//bmp = DecodeImage(file);
    		if (bmp != null){
    			Log.i("face scanning", "scan Path "+fPath);
    			findFaces(bmp, lDate, db);
    			bmp.recycle();
    		}
    	}
    	catch(Exception e2){};
    }
    
    private void 		findFaces(Bitmap bm,long lDate, dbManager db){
     	if (bm==null) return;
	 	int iW = bm.getWidth();
	 	int iH = bm.getHeight();
	 	Face[] myFace = new Face[maxFaceByimage];
	 	FaceDetector FD = new FaceDetector(iW, iH, maxFaceByimage);
  		int foundNum= FD.findFaces(bm, myFace);
  		Log.i("face scanning","scan found "+foundNum);
  		if (foundNum ==0 ){
  			try
  			{
  	   		   db.insert(fPath, null, lDate, foundNum);
  			}
  			catch(SQLException e6){
  			};
  		}
        float ed;
         for(int i=0; i < foundNum; i++)
        {
     	   	try
     	   	{
       		   Face face = myFace[i];
     		   //if (face.confidence()<confidence) continue;
     		   PointF mp = new PointF();
     		   face.getMidPoint(mp);
     		   ed = face.eyesDistance();
     		   float sw = 2*scaleFactor*ed/faceX;
     		   float sh = 2*scaleFactor*ed/faceX;
     		   Matrix matrix = new Matrix();
     		   matrix.postScale(1/sw, 1/sh);
     		  Log.i("face saving",mp.x+":"+mp.y+"= "+ed+" ="+bm.getWidth()+":"+bm.getHeight());
     		 Log.i("face saving",Math.round(mp.x - scaleFactor* ed)+":"+ Math.round(mp.y - scaleFactor*ed)+"-"+Math.round(2*scaleFactor*ed)+":"+(int)Math.round(2* scaleFactor*ed)+"--"+matrix);
     		  int startX =   Math.round(mp.x - scaleFactor* ed);
     		  if (startX<0) startX=0;
     		  int startY = Math.round(mp.y - scaleFactor*ed);
     		  if (startY<0) startY=0;
     		  int doWidth = Math.round(2*scaleFactor*ed);
     		  int doW=doWidth, doH=doWidth;
     		  if (startX+doWidth>bm.getWidth()) doW = bm.getWidth()-startX;
     		  if (startY+doWidth>bm.getHeight()) doH=bm.getHeight()-startY;
     		 Bitmap fbmp = Bitmap.createBitmap(bm, startX, startY,doW,doH,matrix,false);
     		   try
     	   	   {
      	   		   db.insert(fPath, fbmp, lDate, i+1);
     	   		   Log.i("face saving","scan saving...");
     	   		   hasNew=true;
     	   	   }
     	   	   catch(Exception ee){
     				Log.i("event", "insert face error "+ee.getMessage());

     	   		   continue;
     	   	   }
     	   	}
     	   	catch(Exception ee){
     	   	  Log.i("face saving","scan saving..."+ee.getMessage());
     		   continue;
     	   	};
        }
        bm.recycle();
        bm=null;
        myFace =null;
        FD = null;
    }
    
    public String 		getCameraFolder() {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        //Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        String hstr="";
        try
        {
        	int id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        	cursor.moveToFirst();
        	hstr=cursor.getString(id);
        	File hf=new File(hstr);
            if (hf.isFile()) hstr=hf.getParent();
        }
        catch(Exception ee){}
        finally
        {
        	cursor.close();
        }
        return hstr;
    }
    
    private void		GetContact(){
    	ContentResolver cr = getContentResolver();
    	contactID = ""+cids.elementAt(position);

    	contactName="";
    	phones.clear();
    	emails.clear();
    	if (contactID=="0") return;
        
    	Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,null,ContactsContract.Contacts.Data._ID+" = "+contactID,null, null);  
    	if (cursor.getCount()==0){
    		cursor.close();
    		cursor=null;
    		return;
    	}
    	while (cursor.moveToNext()) {  
    		contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
    		String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); 
    		if (Integer.parseInt(hasPhone) == 1) {
   			
    			// You know it has a number so now query it like this
    			 Cursor cp = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
    														null, 
    														ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactID,null,null); 
    			if (cp.getCount()>0){
    				while (cp.moveToNext()) {    
    					int intType=cp.getInt(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
    					String type="O: ";
    					if (intType==1)
    						type= "H: ";
    					if (intType == 2)
    						type =  "M: ";
    					if (intType==3)
    						type =  "W: ";
    					if (intType == 4)
    						type =  "FW: ";
    					if (intType==5)
    						type =  "FH: ";

    					phones.add(type+cp.getString(cp.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER)));
    				}
    			
    			}
   				cp.close();  
   				cp = null;
   			 
    		}  
    		Cursor cemail = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
    													ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactID, null, null);  
    		
    		if (cemail.getCount()>0){
    			while (cemail.moveToNext()) {     
    			// This would allow you get several email addresses     
    				int eType=cemail.getInt(cemail.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
    				String sType="O: ";
    				if (eType==1)
    					sType="H: ";
    				if (eType==2)
    					sType="W: ";

    				emails.add(sType+cemail.getString(cemail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));  
    			}
    		}
    		cemail.close();
    	}
    	
    	cursor.close();  
    	cursor =null;
    }
    
    private Uri 		GetRawContactUri(){
    	Uri rawUri=null;
		Cursor cursor =  managedQuery(
		        RawContacts.CONTENT_URI, 
		        new String[] {RawContacts._ID},
		        RawContacts.CONTACT_ID + " = " + contactID, 
		        null, 
		        null);
		if(!cursor.isAfterLast()) {
		    cursor.moveToFirst();
		    rawUri = RawContacts.CONTENT_URI.buildUpon().appendPath(""+cursor.getLong(0)).build();
		}
		cursor.close();    	
    	return rawUri;
    }
    
    public class 		ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return faces.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }
        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(ifaceX, ifaceX));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            if (faces.size()<=position)
            	{
            	imageView.setImageBitmap(null);
            	return imageView;
            	}
            Bitmap bt = faces.elementAt(position);
            
            try
            {
            	if (cids.elementAt(position) > 0){
            		
            		
            		Bitmap btp = bt.copy(Bitmap.Config.ARGB_8888, true); 
            		bt=null;
            		//showMessage(bt.isMutable()+"");
            		/*for (int i=0;i<100; i++)
            		{
            			for (int j=0; j<10;j++)
            				btp.setPixel(i,j,borderColor);
            		}
            		*/
               		int hH = btp.getHeight();
            		btp.setPixels(pix, 0, 10,0,0,10,hH);
            		bt= btp;            	}
            }
            catch(Exception e3){
            	//showMessage(e3.getMessage());
            };
            imageView.setImageBitmap(bt);
            return imageView;
        }
    }
    
    public void			InitializePix(){
    	for (int i=0;i<pix.length; i++){
    		pix[i]=borderColor;
    	}
    	phoneIDs.add(R.id.phone0);
    	phoneIDs.add(R.id.phone1);
    	phoneIDs.add(R.id.phone2);
    	phoneIDs.add(R.id.phone3);
    	phoneIDs.add(R.id.phone4);
    	emailIDs.add(R.id.email0);
    	emailIDs.add(R.id.email1);
    	emailIDs.add(R.id.email2);
    	emailIDs.add(R.id.email3);
    	emailIDs.add(R.id.email4);
       	smsIDs.add(R.id.SMS0);
    	smsIDs.add(R.id.SMS1);
    	smsIDs.add(R.id.SMS2);
    	smsIDs.add(R.id.SMS3);
    	smsIDs.add(R.id.SMS4);
    }
    
    public void			InsertPhone(String rawContactId,String phoneNum, int PhoneType){
    	ContentValues values = new ContentValues();
    	values.put(Phone.RAW_CONTACT_ID, rawContactId);
    	values.put(Phone.NUMBER, phoneNum);
    	values.put(Phone.TYPE, PhoneType);
    	getContentResolver().insert(Phone.CONTENT_URI, values);
    }
    
    
    
    private void 		MakeCall(String phoneNum) {
    	String str = phoneNum.split(":")[1].trim();
   	    try {
   	        Intent callIntent = new Intent(Intent.ACTION_CALL);
   	        callIntent.setData(Uri.parse("tel:"+str));
    	    startActivity(callIntent);
    	} 
   	    catch (ActivityNotFoundException activityException) {}
   	}
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            //textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, PhotoScanService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            //textStatus.setText("Disconnected.");
        }
	
    };
    
    @Override     
    public void 		onActivityResult(int reqCode, int resultCode, Intent data) {       
    	super.onActivityResult(reqCode, resultCode, data);       
    	switch (reqCode) {         
    	case (PICK_CONTACT) :           
    		if (resultCode == Activity.RESULT_OK) {             
    			Uri contactData = data.getData();             
    			Cursor c =  managedQuery(contactData, null, null, null, null);
    			if (c.moveToFirst()) {               
     				contactID = c.getString(c .getColumnIndex(ContactsContract.Contacts._ID));
     				try
     				{
     					Add_ContactPhoto(ids.elementAt(position));
     				}
     				catch(Exception ee1){};
    				try
     				{
     					Add_ContactID(ids.elementAt(position));
     				}
     				catch(Exception ee2){};
     				refreshGrid();
    			}
    			c.close();
    			c = null;
    		}
    	}
    }

	@Override
    public void 		onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirmmessage)
               .setCancelable(false)
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        FacesActivity.this.finish();
                   }
               })
               .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
	public void			onNextPage(View view){
		DoNextPage(true);
	}
	
	public void			onPreviousPage(View view){
		DoNextPage(false);
	}
	
    private void 		OnStopDialog(){
    	if (faceRun == null)
    	{
    		ToastMessage(getString(R.string.noThread));
    		return;
    	}
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    	 
        // Setting Dialog Title
        alertDialog.setTitle(R.string.dialogTitle);
 
        // Setting Dialog Message
        alertDialog.setMessage(R.string.stopRun);
 
        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);
 
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	faceScanning = false;
            	saveSetting();
            	StopRun();
            // Write your code here to invoke YES event
            }
        });
 
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,    int which) {
            	dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();

    }

    private void 		OnClearDialog(){
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.dialogTitle);
        alertDialog.setMessage(R.string.dialogClear);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Clear();
           }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,    int which) {
            	dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void 		OnDeleteDialog(){
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.dialogTitle);
        alertDialog.setMessage(R.string.dialogDelete);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	DeleteItem(selectedID);
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,    int which) {
            	dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void		OnSelectFolder(){
    	stopScan = true;
    	if (scanFolder=="")
    		scanFolder = getCameraFolder();
    	ResultReceiver rs = new ResultReceiver();
    	fileDialog fd = new fileDialog(this,rs,scanFolder);
    	//fd.dHeight = (int)Math.floor((double)heightS*0.8);
    }
    
    private void		OnSelectGallery(){
    	scanFolder ="";
    	stopScan = true;
		saveSetting();
		DoScanRun();
    }
    
    private void 		OpenCamera(){
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	this.startActivity(intent);
    }
    
    private void 		OpenContact(){
    	Intent intent = new Intent(Intent.ACTION_VIEW);     
    	Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));     
    	intent.setData(uri); 
    	this.startActivity(intent); 
    }
    
    private void 		OpenImage(String path){
    	ToastMessage(path);
    	Log.i("open image", path);
    	Intent intent = new Intent();
    	intent.setAction(android.content.Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
    	this.startActivity(intent);
    }

	private void 		readSetting(){
		SharedPreferences e = this.getPreferences(MODE_PRIVATE); 
		rcgSize = e.getInt("rcgSize",rcgSize);
		scanFolder = e.getString("scanFolder","");
		faceScanning =e.getBoolean("faceScanning",faceScanning);
		viewContact =e.getBoolean("viewContact",viewContact);
		if (faceScanning)
			tvDate.setText(getString(R.string.inPorcess));
		else
			tvDate.setText(getString(R.string.stopString));
			
	
	}
	
    private void 		refreshGrid(){
    	
    	try
    	{
    		this.runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				Log.i("event","refreshed GV");
    				if (faces.size()>0){
    					try {
    						faceAdapter.notifyDataSetChanged();
    						gv.invalidateViews();
    					}
    					catch(Exception eeee) {
    						Log.i("event", eeee.getMessage());
    					}
    					if (position<faces.size()) gv.setSelection(position);
    					Log.i("event","refreshed GV"+faces.size());
    					int hint = showIndex + pageSize;
    					if (hint>totalFaces) hint=totalFaces;
    					tvTotal.setText(showIndex+".."+hint+"/"+totalFaces+"");
    				}
    			}
    		});
    	}
    	catch(Exception eee){};
    }
   
    private Bitmap 		resize(String filePath){
    	BitmapFactory.Options dOption=new BitmapFactory.Options();
    	dOption.inJustDecodeBounds=true;
	 	BitmapFactory.decodeFile(filePath, dOption);
	 	int scale =1;
	 	int pixs=dOption.outWidth*dOption.outHeight;
	 	if (rcgSize > 0){
		 	while(pixs/scale/2>=rcgSize)
	            scale*=2;
	 	}
	 	
	 	dOption.inJustDecodeBounds=false;
	 	dOption.inPreferredConfig =Config.RGB_565;
	 	dOption.inSampleSize = scale;
 	    Bitmap bp= BitmapFactory.decodeFile(filePath, dOption);
 	   Log.i("scan","scan "+pixs+" "+scale+" img size: "+bp.getWidth()+":"+bp.getHeight());
 	    
 	    return bp;
    }
    
    private Bitmap 		resize(Bitmap bt, int newW, int newH){
	 	int iW = bt.getWidth();
	 	int iH = bt.getHeight();
	 	if (iW<=newW && iH<=newH)
	 		return bt;
	 	float scale = (float)newW/iW;
	 	float scaleY = (float)newH/iH;
	 	if (scale<scaleY) scale=scaleY;
	 	//float scale = (float)Math.sqrt((float)newW*newH/iW/iH);
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale,scale);
 	    return Bitmap.createBitmap(bt,0,0,iW,iH,matrix,false);
    }
    
    public void			SaveContactPhoto(int cId) {     
    	Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, cId);     
    	Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);     
    	Cursor cursor = getContentResolver().query(photoUri, new String[] {Contacts.Photo.DATA15,Contacts.DISPLAY_NAME}, null, null, null);     
    	if (cursor == null) {         
    		return;     
    	}     
    	try {         
    		if (cursor.moveToFirst()) {       
    			byte[] bb = cursor.getBlob(0); 
    			if (bb == null) return;                 
   				Bitmap bm = BitmapFactory.decodeByteArray(bb, 0, bb.length);
   				if (bm!=null){
   					bm = resize(bm,faceX,faceX);
    				Date now = new Date();
    				long lDate = Math.round(((double)now.getTime())/1000);
    				dbManager db = new dbManager(this);
    				db.insert(CONTACT_PATH, bm,lDate,1,cId);
    				hasNew=true;
    				db.close();
    				db=null;
   				}
   			}     
   		} 
    	finally {         
    		cursor.close();  
    		cursor =null;
   		}     
    }   
    
    private void 		saveSetting(){
		Editor e = this.getPreferences(MODE_PRIVATE).edit(); 
		e.putInt("rcgSize", rcgSize);
		e.putBoolean("faceScanning", faceScanning);
		e.putBoolean("viewContact", viewContact);
		e.putString("scanFolder", scanFolder);
		e.commit(); 
	}	
    
    private void		SearchContactPhoto(){
    	ContentResolver cr = getContentResolver();
    	Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,null,null,null, Contacts.DISPLAY_NAME);  
    	while (cursor.moveToNext()) {
    		
    		int contactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
    		SaveContactPhoto(contactID);
    	}
    	cursor.close();  
    	cursor=null;
    }
    
    private void 		SendEmail(String email, String subject, String body){
    	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ email,"",});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        emailIntent.setType("plain/text");
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
    
    private void 		showMessage(String str)
	{
	    AlertDialog.Builder aadb = new AlertDialog.Builder(this);
        aadb.setMessage(str);
        aadb.create().show();
	}
 	
    private void		StopRun(){
    	try
    	{
    		Thread ht = faceRun;
    		faceRun = null;
    		ht.interrupt();
    	}
    	catch(Exception e5){};
    	tvDate.setText(getString(R.string.stopString));
    }
    
    private void 		ToastMessage(String str)
	{
		Toast toast = Toast.makeText(this, str,6);
		toast.show();
	}
    
    class ResultReceiver implements fileDialog.Result
    {
		@Override
		public void onChooseDirectory(String dir) {
			if (scanFolder != dir){
				scanFolder = dir;
    			saveSetting();
    	    	Log.i("search folder", scanFolder);
    			StopRun();
    			DoScanRun();
    		}
		}
    }
    
    class SwipeReceiver implements FaceGestureListner.SwipeResult{
		@Override
		public void onSwipe(Boolean rightToLeft) {
				DoNextPage(rightToLeft);
				Log.i("Swipe", "face swipe " + (rightToLeft?"Right To Left":"Left To Right"));
    		}
	}
    
    class GridViewOnTouchListener implements OnTouchListener
    {
    	@Override
    	public boolean onTouch(View v, MotionEvent event)
    	{
    		return gestureDetector.onTouchEvent(event);
    	}
    }
}

    
