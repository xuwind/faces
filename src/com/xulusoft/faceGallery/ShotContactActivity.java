package com.xulusoft.faceGallery;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
public class ShotContactActivity extends Activity {
  
	private MouseSurfaceView view;
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        view = new MouseSurfaceView(this);
        //registerForContextMenu(view); 
        setContentView(view);
        //view.requestFocus();        
        //view.setFocusableInTouchMode(true);
    }
 
    @Override    protected void onResume() {        
    	// Ideally a game should implement onResume() and onPause()        
    	// to take appropriate action when the activity looses focus        
    	super.onResume();        
    	//view.onResume();    
    }    
    @Override    
    protected void onPause() {        
    	// Ideally a game should implement onResume() and onPause()        
    	// to take appropriate action when the activity looses focus        
    	super.onPause();        
    	//view.onPause();    
    }

}