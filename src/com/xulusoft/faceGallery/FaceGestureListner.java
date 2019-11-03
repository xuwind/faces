package com.xulusoft.faceGallery;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;


public class FaceGestureListner extends SimpleOnGestureListener {
    private static final int SWIPE_MIN_DISTANCE = 120;     
    private static final int SWIPE_MAX_OFF_PATH = 250;     
    private static final int SWIPE_THRESHOLD_VELOCITY = 200; 
    public int action = 0;
    GLRenderer mRenderer=null;
    public SwipeResult swiperesult = null;
    
    public FaceGestureListner(SwipeResult rs){
    	swiperesult = rs;
    }
    @Override         
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	try {                 
    		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)                     
    			return false;                
    		// right to left swipe                 
    		if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
    			swiperesult.onSwipe(true);
    		}  
    		//left to right swipe
    		else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
    			swiperesult.onSwipe(false);
    		}
    		return true;
    	}
    	catch (Exception e) 
    	{
    		// nothing             
    	}             
    	return false;         
   	}
    
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		setGesture(3);
		return super.onDoubleTap(e);
	} 
	
	private void setGesture(int action)
	{
		if (mRenderer != null)
		mRenderer.setAction(action);
	}
	
	public interface SwipeResult
	{
		void onSwipe(Boolean rightToLeft);
	}
	
 }
