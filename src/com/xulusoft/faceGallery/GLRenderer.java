package com.xulusoft.faceGallery;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;

public class GLRenderer implements Renderer {
	public float angle;
	private Context context;
	public Vector<Bitmap> images=new Vector<Bitmap>();
	public Vector<imageDrawer> ids = new Vector<imageDrawer>();
	public imageDrawer id, id2;
	private float xrot;            //X Rotation ( NEW )
	private float yrot;            //Y Rotation ( NEW )
	private float zrot;     	   //z Rotation ( NEW )
	private int position=0, maxFaces=10, totalTime=0;
	private int imageSize=64;
	public int action = 0;
	private float xt = 0.0f, xstep=0.02f, xmax=1.2f,yrstep=1.2f;
	private int fc = 0, fcMax = 2, speed=1, stepDirection=1, rotateDirection=1;
	public GLRenderer(Context con)
	{
		context=con;
		//id = new imageDrawer();
		//id2 = new imageDrawer();
		DoRead();
		if (images.size()==0){
			showMessage(con.getString(R.string.noContact));
			prepare();
		}
		for (int i=0; i<images.size();i++)
		{
			imageDrawer id = new imageDrawer();
			ids.add(id);
		}
	}
	
	
	private void 		DoRead(){
		   //prepare();
		   
	    	images.clear();
	    	dbManager db = new dbManager(context,true);
	   		db.GetAll("select _id, filePath,img, lastDate, faceNum, Contact_ID from file_faces where faceNum>0 and Contact_ID>0 order by LastDate DESC;");
	    	db.close();
	    	db.clear();
	    	images = db.faces;
	    	db=null;
	     	
	    }
	

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		//id.loadGLTexture(gl, images.elementAt(0));
		//id2.loadGLTexture(gl, images.elementAt(0));
		for (int i=0; i<images.size();i++)
		{
			ids.elementAt(i).loadGLTexture(gl, images.elementAt(i));
		}
		//very important for image display
		gl.glEnable(GL10.GL_TEXTURE_2D);
		// Set the background color to black ( rgba ).
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		// Enable Smooth Shading, default not really needed.
		gl.glShadeModel(GL10.GL_SMOOTH);
		// Depth buffer setup.
		gl.glClearDepthf(1.0f);
		// Enables depth testing.
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// The type of depth testing to do.
		gl.glDepthFunc(GL10.GL_LEQUAL);
		// Really nice perspective calculations.
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}
	
	public void prepare(){
	      InputStream is = context.getResources().openRawResource(R.drawable.facesicon_free);
	      Bitmap bitmap = null;
	      try {
	         //BitmapFactory is an Android graphics utility for images
	         bitmap = BitmapFactory.decodeStream(is);
	         images.add(resize(bitmap,imageSize,imageSize));

	      } finally {
	         //Always clear and close
	         try {
	            is.close();
	            is = null;
	         } catch (IOException e) {
	         }
	      }
	}
	
	public void onDrawFrame(GL10 gl) {

		  //Clear Screen And Depth Buffer
	    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);   
	    gl.glLoadIdentity();               //Reset The Current Modelview Matrix
	      
	      //Drawing
	    gl.glTranslatef(0.0f, 0.0f, -5.0f);      //Move 5 units into the screen
	    gl.glScalef(0.7f, 0.7f, 0.7f);          //Scale the Cube to 80 percent, otherwise it would be too large for the screen
	      
	    //Rotate around the axis based on the rotation matrix (rotation, x, y, z)
	    /*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    totalTime++;
	    fc++;
	    if (action ==1 || action==2)
	    {
	    	xt += stepDirection*xstep;
			yrot += stepDirection*speed*yrstep;
	    }
	    if (fc > fcMax)
	    {
	    	fc =0;
			if (xt < -xmax)
			{
				stepDirection = 1;
				rotateDirection = 1;
			}
			if (xt > xmax){
				stepDirection = -1;
				rotateDirection = -1;
			}
	    }
		int count = position;
		gl.glTranslatef(xt, -2.5f, -.0f);
	    //gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);   //X
	    //gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);   //Y
	    //gl.glRotatef(zrot, 0.0f, 0.0f, 1.0f);   //Z
		
		for (int i=0; i<maxFaces;i++)
	    {
			if (images.size()==0) break;
		    //gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);   //X
		    gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);   //Y
		    //gl.glRotatef(zrot, 0.0f, 0.0f, 1.0f);   //Z

	    	if (count >= images.size())
	    		count=0;
	    	ids.elementAt(count).draw(gl);
	    	count++;
	    	if (totalTime > 1000 && i==(maxFaces-1))
	    	{
		    	if (count >= images.size())
		    		count=0;
	    		position = count;
	    		totalTime=0;
	    	}
		    gl.glTranslatef(0.0f, 0.5f, -0.2f);
		    /*
		    if (i % 3 ==0)
		    	gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);   //Z
		    if (i % 3 ==1)
		    	gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);   //Z
		    if (i % 3 ==2)
		    	gl.glRotatef(zrot, 0.0f, 0.0f, 1.0f);   //Z
		    	*/
	    }
	    //Draw the Cube   
	    //Change rotation factors (nice rotation)

	    //id2.draw(gl);
	    //xrot += 0.05f;
	    yrot += 0.2f;
	    zrot += 0.1f;
	}
		
	public void onSurfaceChanged(GL10 gl, int width, int height) {
			// Sets the current view port to the new size.
			gl.glViewport(0, 0, width, height);// OpenGL docs.
			// Select the projection matrix
			gl.glMatrixMode(GL10.GL_PROJECTION);// OpenGL docs.
			// Reset the projection matrix
			gl.glLoadIdentity();// OpenGL docs.
			// Calculate the aspect ratio of the window
			GLU.gluPerspective(gl, 45.0f,(float) width / (float) height, 0.1f, 100.0f);
			// Select the modelview matrix
			gl.glMatrixMode(GL10.GL_MODELVIEW);// OpenGL docs.
			// Reset the modelview matrix
			gl.glLoadIdentity();// OpenGL docs.
		}
	
	
    private Bitmap 		resize(Bitmap bt, int newW, int newH){
	 	int iW = bt.getWidth();
	 	int iH = bt.getHeight();
	 	float scale = (float)Math.sqrt((float)newW*newH/iW/iH);
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale,scale);
 	    return Bitmap.createBitmap(bt,0,0,iW,iH,matrix,false);
    }
	
    public void setAction(int acode){
    	if (action==1 && acode ==1){
    		speed++;
    	}
    	if (action==2 && acode ==2){
    		speed++;
    	}
    	if (acode==1)
    	{
    		stepDirection = -1;
    		rotateDirection = -1;
    	}
    	if (acode==2)
    	{
    		stepDirection =1;
    		rotateDirection =1;
    	}
    	if (action != acode)
    	{
    		speed=1;
    		action = acode;
    	}
    }
    
    private void 		showMessage(String str)
	{
		
	    AlertDialog.Builder aadb = new AlertDialog.Builder(context);
        aadb.setMessage(str);
        aadb.create().show();
        
		//Toast toast = Toast.makeText(this, str, 3);
		//toast.show();
	}
	

}
