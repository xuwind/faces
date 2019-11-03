package com.xulusoft.faceGallery;

import android.app.AlertDialog;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;


public class MouseSurfaceView extends GLSurfaceView {
	
	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener; 
	private Context con;
	private XuluGestureListner gListner;
	
	public MouseSurfaceView(Context context) {
		super(context);
		con = context;
		mRenderer = new GLRenderer(context);
		gListner = new XuluGestureListner(context);
		gListner.mRenderer =mRenderer;
        gestureDetector = new GestureDetector(gListner);
        /*
        gestureListener = new View.OnTouchListener() {             
        	public boolean onTouch(View v, MotionEvent event) {                 
        		return gestureDetector.onTouchEvent(event);             
        		}         
        };  
        this.setOnTouchListener(gestureListener);
        */
		// TODO Auto-generated constructor stub
		setRenderer(mRenderer);        
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);    
	}
	
	
/*
	@Override public boolean onTrackballEvent(MotionEvent e) {        
		//mRenderer.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;        
		//mRenderer.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;        
		requestRender();        return true;    
	}
*/	
	@Override public boolean onTouchEvent(MotionEvent e) {
		gestureDetector.onTouchEvent(e);
		/*
		if (e.getAction()==MotionEvent.ACTION_UP)
		this.showContextMenu(); 
		float x = e.getX();        
		float y = e.getY();
		switch (e.getAction()) {        
		case MotionEvent.ACTION_MOVE:            
			float dx = x - mPreviousX;            
			float dy = y - mPreviousY;            
			//mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;            
			//mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;            
			requestRender();        
		}        
		mPreviousX = x;        
		mPreviousY = y;
		*/        
		return true;    
	}
	
	
    private void 		showMessage(String str)
	{
		
	    AlertDialog.Builder aadb = new AlertDialog.Builder(con);
        aadb.setMessage(str);
        aadb.create().show();
        
		//Toast toast = Toast.makeText(this, str, 3);
		//toast.show();
	}
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;    
    private final float TRACKBALL_SCALE_FACTOR = 36.0f;    
    private GLRenderer mRenderer;    
    private float mPreviousX;    
    private float mPreviousY;
}




