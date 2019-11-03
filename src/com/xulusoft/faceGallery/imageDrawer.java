package com.xulusoft.faceGallery;

	import java.io.IOException;
	import java.io.InputStream;
	import java.nio.ByteBuffer;
	import java.nio.ByteOrder;
	import java.nio.FloatBuffer;
	import javax.microedition.khronos.opengles.GL10;
	import android.content.Context;
	import android.graphics.Bitmap;
	import android.graphics.BitmapFactory;
	import android.opengl.GLUtils;

	public class imageDrawer {

	   /** The buffer holding the vertices */
	   private FloatBuffer vertexBuffer;
	   /** The buffer holding the texture coordinates */
	   private FloatBuffer textureBuffer;
	   /** The buffer holding the indices */
	   private ByteBuffer indexBuffer;
	   public Bitmap bitmap;
	   /** Our texture pointer */
	   private int[] textures = new int[1];
	   //float rw = (float)128/240;
	   float rw=.25f;
	    private float vertices[] = {
	                   //Vertices according to faces
	                   -rw, -rw, rw, //Vertex 0
	                   rw, -rw, rw,  //v1
	                   -rw, rw, rw,  //v2
	                   rw, rw, rw,   //v3
	                                 };
	    
	    /** The initial texture coordinates (u, v) */
	    float imgSize=1.0f;
	    private float texture[] = {          
	                   //Mapping coordinates for the vertices
	                   0.0f, imgSize,
	                   0.0f, 0.0f,
	                   imgSize, imgSize,
	                   imgSize, 0.0f, 
	                                  };
	        
	    /** The initial indices definition */   
	    private byte indices[] = {
	                   //Faces definition
	                   0,1,3, 0,3,2,          //Face front
	                                  };

	   public imageDrawer() {
	      //
	      ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
	      byteBuf.order(ByteOrder.nativeOrder());
	      vertexBuffer = byteBuf.asFloatBuffer();
	      vertexBuffer.put(vertices);
	      vertexBuffer.position(0);
	      byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
	      byteBuf.order(ByteOrder.nativeOrder());
	      textureBuffer = byteBuf.asFloatBuffer();
	      textureBuffer.put(texture);
	      textureBuffer.position(0);
	      indexBuffer = ByteBuffer.allocateDirect(indices.length);
	      indexBuffer.put(indices);
	      indexBuffer.position(0);
	   }

	   public void draw(GL10 gl) {
	      //Bind our only previously generated texture in this case
	      gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
	      
	      //Point to our buffers
	      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	      gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

	      //Set the face rotation
	      gl.glFrontFace(GL10.GL_CCW);
	      
	      //Enable the vertex and texture state
	      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
	      gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
	      
	      //Draw the vertices as triangles, based on the Index Buffer information
	      //gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);      
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
	      
	      //Disable the client state before leaving
	      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	      gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	   }
	   
	   
	   /**
	    * Load the textures
	    * 
	    * @param gl - The GL Context
	    * @param context - The Activity context
	    */
	   public void loadGLTexture(GL10 gl, Context context) {
	      //Get the texture from the Android resource directory
		   
	      InputStream is = context.getResources().openRawResource(R.drawable.icon);
	      Bitmap bitmap = null;
	      try {
	         //BitmapFactory is an Android graphics utility for images
	         bitmap = BitmapFactory.decodeStream(is);

	      } finally {
	         //Always clear and close
	         try {
	            is.close();
	            is = null;
	         } catch (IOException e) {
	         }
	      }
	      
	      //gl.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
	      //Generate one texture pointer...
	      gl.glGenTextures(1, textures, 0);
	      //...and bind it to our array
	      gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
	      
	      //Create Nearest Filtered Texture
	      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

	      //Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
	      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
	      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
	      
	      //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
	      GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	      
	      //Clean up
	      bitmap.recycle();
	   }
	   public void loadGLTexture(GL10 gl, Bitmap bm) {
		   bitmap = bm;
		      //Get the texture from the Android resource directory
			   /*
		      InputStream is = context.getResources().openRawResource(R.drawable.faces);
		      Bitmap bitmap = null;
		      try {
		         //BitmapFactory is an Android graphics utility for images
		         bitmap = BitmapFactory.decodeStream(is);

		      } finally {
		         //Always clear and close
		         try {
		            is.close();
		            is = null;
		         } catch (IOException e) {
		         }
		      }
		      */
		      gl.glColor4f(.5f, .5f, .5f, 1.0f);
		      //Generate one texture pointer...
		      gl.glGenTextures(1, textures, 0);
		      //...and bind it to our array
		      gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		      
		      //Create Nearest Filtered Texture
		      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		      //Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		      
		      //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		      GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		      
		      //Clean up
		      bitmap.recycle();
		   }

	
	}

