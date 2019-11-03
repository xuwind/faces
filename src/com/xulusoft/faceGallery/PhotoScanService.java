package com.xulusoft.faceGallery;

import java.io.File;
import java.io.FileFilter;

import java.util.ArrayList;
import java.util.Date;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.util.Log;

public class PhotoScanService extends Service {
    private NotificationManager nm;
    private Timer timer = new Timer();
    private int counter = 0, incrementby = 1;
    private static boolean isRunning = false;

    private int faceX=120;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;
    static public String scanFolder=""; 
    private boolean noStop = true;
    private static final String CONTACT_PATH="from contact";
	private String[] imageExtensions = new String[]{"jpg"};
	private String cacheFolder="DCIM/.thumbnails";
    Thread faceRun;
    public Runnable runnable;
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
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
    
    public class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_SET_INT_VALUE:
                incrementby = msg.arg1;
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    private void sendMessageToUI(int intvaluetosend) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));

                //Send data as a String
                Bundle b = new Bundle();
                b.putString("str1", "ab" + intvaluetosend + "cd");
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyService", "Service Started.");
        showNotification();
        timer.scheduleAtFixedRate(new TimerTask(){ public void run() {onTimerTick();}}, 0, 100L);
        isRunning = true;
    }
    
    private void showNotification() {
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_started);
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, FacesActivity.class), 0);
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.service_label), text, contentIntent);
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        nm.notify(R.string.service_started, notification);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyService", "Received start id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }

    public static boolean isRunning()
    {
        return isRunning;
    }


    private void onTimerTick() {
        Log.i("TimerTick", "Timer doing work." + counter);
        try {
            counter += incrementby;
            sendMessageToUI(counter);

        } catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
            Log.e("TimerTick", "Timer Tick Failed.", t);            
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {timer.cancel();}
        counter=0;
        nm.cancel(R.string.service_started); // Cancel the persistent notification.
        Log.i("MyService", "Service Stopped.");
        isRunning = false;
    }
    
    
    
    private void		DoScan(){
 		dbManager db = new dbManager(this);
     	Log.i("Scan Thread","face I am still running...");
     	ImageFilter fImages = new ImageFilter(); 
     	if (scanFolder==""){
     		Log.i("Scan Folder", "Gallery");
     		findFiles(fImages, db);
     	}
     	else{
     		Log.i("Scan Folder", scanFolder);
     		File startDir = new File(scanFolder);
     		findFiles(startDir,fImages, db);
     	}
     	/*
       	if (hasNew){
       		try
       		{
       			handler2.post(new Runnable() {
       			@Override
 					public void run() {	
    								if (ACTIVE_INSTANCE != null) DoRead();
    						}
 					}
       				);
       		}
       		catch(Exception e7){};
       		hasNew = false;
       	}*/
 		db.close();
 		db=null;
// 		stopScan = false;
 		
     }
     
 	private void 		DoScanRun(){
 		runnable = new faceScanner();
 		faceRun= new Thread(runnable);  
 		faceRun.setPriority(Thread.NORM_PRIORITY);
 		faceRun.start();
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
        Context ctx = getApplicationContext();
		//String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,MediaStore.Images.ImageColumns.DATA,MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,MediaStore.Images.ImageColumns.DATE_TAKEN,MediaStore.Images.ImageColumns.MIME_TYPE};     
		String[] projection = new String[]{MediaStore.Images.ImageColumns.DATA,MediaStore.Images.ImageColumns.MIME_TYPE};
        final Cursor cursor = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"); 
        if(cursor != null){
            //cursor.moveToFirst();
            while (cursor.moveToNext()){
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int ind = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE);
                String mimeStr = cursor.getString(ind);
                if (mimeStr.indexOf("image")>=0){
                	String fPath = cursor.getString(index);
                	findFaces(fPath,db);
                	Log.i("Image file path", fPath+" -"+mimeStr);
                }

            }
            cursor.close();
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
    	for (File f1 : ffs){
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
         	if (ACTIVE_INSTANCE==null)
        		return;
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
     		   Bitmap fbmp = Bitmap.createBitmap(bm, Math.round(mp.x - scaleFactor* ed), Math.round(mp.y - scaleFactor*ed),Math.round(2*scaleFactor*ed),(int)Math.round(2* scaleFactor*ed),matrix,false);
     		   try
     	   	   {
     			   if (ACTIVE_INSTANCE==null)
     		    		return;
     	   		   db.insert(fPath, fbmp, lDate, i+1);
     	   		   Log.i("face saving","scan saving...");
     	   		   num ++;
    	   		   //totalValid++;
     	   	   }
     	   	   catch(Exception ee){
     	   		   continue;
     	   	   }
     	   	}
     	   	catch(Exception ee){
     		   continue;
     	   	};
        }
   
        bm.recycle();
        bm=null;
        myFace =null;
        FD = null;
    }
    
    public String getCameraFolder() {
        String[] projection = { MediaStore.Images.Media.DATA };
        Context   ctx = getApplicationContext();
        Cursor cursor = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        int id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String hstr=cursor.getString(id);
        cursor.close();
        File hf=new File(hstr);
        if (hf.isFile())
        	hstr=hf.getParent();
        return hstr;
    }
 
    
    private void		SearchContactPhoto(){
    	ContentResolver cr = getContentResolver();
    	Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,null,null,null, null);  
    	while (cursor.moveToNext()) {
    		
    		int contactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
    		SaveContactPhoto(contactID);
    	}
    	cursor.close();  
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
    				new Date(lDate);
    				//showMessage(hdate+"");
    				dbManager db = new dbManager(this);
    				db.insert(CONTACT_PATH, bm,lDate,1,cId);
    				db.close();
    				db=null;
   				}
   			}     
   		} 
    	finally {         
    		cursor.close();     
   		}     
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
	private Bitmap bmp;
	private String fPath; 
	

	private int maxFaceByimage = 5;
	public int  maxFaces=50, rcgSize=2000000, totalFaces=0,showIndex=0, pageSize=0, selectedID=0;
	public int num=0, updateNum=5;
	static public int colShow=0,rowShow=0;
	public int[] pix = new int[1000];
	public int borderColor= 0xff00ff00,heightS;
	private float scaleFactor=1.6f;
	public boolean faceScanning=false, viewContact=false;
	private static FacesActivity ACTIVE_INSTANCE=null;
}
