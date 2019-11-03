package com.xulusoft.faceGallery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import java.util.Date;
import java.util.Vector;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;


public class dbManager{
	public static final String DB_Name = "faceDB";
	public static final String DB_Table = "file_faces"; 
	public static final String[] COLS = new String[]{"_id", "filePath","img", "LastDate", "FaceNum", "Contact_ID"};
	public SQLiteDatabase db;
	private final DataBaseHelper dbH;
	public Vector<Bitmap> faces = new Vector<Bitmap>();
	public Vector<String> paths = new Vector<String>();
	public Vector<Integer> ids = new Vector<Integer>();
	public Vector<Integer> cids = new Vector<Integer>();
	public Vector<Integer> fids = new Vector<Integer>();
	public int num=0;
	public dbManager(Context context)
	{
		dbH = new DataBaseHelper(context);
        try {
 
        	dbH.createDataBase();
 
        } catch (IOException ioe) {
         	throw new Error("Unable to create database");
        }
        try {
        	dbH.openDataBase(false);
        }
        catch(SQLException sqle){
        	throw sqle;
        }
        db = dbH.db;
    }
	
	public dbManager(Context context, boolean read)
	{
		dbH = new DataBaseHelper(context);
        try {
 
        	dbH.createDataBase();
 
        } catch (IOException ioe) {
         	throw new Error("Unable to create database");
        }
        try {
        	dbH.openDataBase(read);
        }
        catch(SQLException sqle){
        	throw sqle;
        }
        db = dbH.db;
	}

	public void clear(){
		if (db != null){
			db.close();
			db=null;
		}
	}
	
	public void close(){

		try
		{
			//faces.clear();
			//ids.clear();
			//paths.clear();
			//cids.clear();
			//fids.clear();
			db.close();
			db=null;
		}
		catch(SQLException ee){};
	}
	
	public int count(){
		return count("select _id from file_faces where faceNum>0 and deleted<>1;");
	}

	public int count(String sql){
		int num=0;
		Cursor c= null;
		try
		{
			c = db.rawQuery(sql,null);
			num = c.getCount();
		}
		catch(SQLException ee){}
		finally {
			c.close();
		}
		return num;
	}

	
	
	public void delete(int id){
		ContentValues values = new ContentValues();
		values.put("deleted", 1);
		db.update(dbManager.DB_Table, values, "_id="+id, null);
		//db.delete(dbManager.DB_Table,"_id="+id, null);
	}

	public void deleteAll(){
		db.delete(dbManager.DB_Table,"_id>0", null);
	}
	
	public void delete(String filePath){
		db.delete(dbManager.DB_Table,"filePath='"+filePath+"'", null);
	}
	
	public void Execute(String sql){
		db.execSQL(sql);
	}
	
	public boolean Exist(String filePath){
		Cursor c= null;
		try
		{
			c = db.rawQuery("select _id from file_faces where filePath='"+filePath.trim()+"'"+";", null);
			if (c==null) return false;
			int num = c.getCount();
			c.close();
			if (num>0) return true;
			return false;
		}
		catch(SQLException ee){
			return false;
		}
	}
	
	public void Get(String filePath){
		faces.clear();
		paths.clear();
		ids.clear();
		Cursor c= null;
		try
		{
			c = db.query(dbManager.DB_Table, dbManager.COLS, "filePath='"+filePath+"'", null, null, null, "LastDate");
			int num = c.getCount();
			if (num==0){
				c.close();
				return;
			}
			c.moveToFirst();
			for (int i=0; i<num; i++){
				paths.add(c.getString(1));
				byte[] bb = c.getBlob(2);
				Bitmap bm = BitmapFactory.decodeByteArray(bb, 0, bb.length);
				faces.add(bm);
			}
			
		}
		catch(SQLException ee){}	
		finally {
			c.close();
		}
	}
	
	public byte[] GetRawPhoto(int id){
		byte[] bb = null;
    	Cursor c = db.rawQuery("select img from file_faces where faceNum>0 and _id=" + id+";", null);
		num = c.getCount();
		if (num==0){
			c.close();
			return bb;
		}
		c.moveToFirst();
		bb = c.getBlob(0);
		c.close();
		return bb;
	}
	
	public void GetAll(){
		faces.clear();
		paths.clear();
		ids.clear();
		Cursor c= null;
		try
		{
			c = db.query(dbManager.DB_Table, dbManager.COLS, null, null, null, null, "LastDate DESC");
			num = c.getCount();
			if (num==0){
				c.close();
				return;
			}
			c.moveToFirst();
			for (int i=0; i<num; i++){
				ids.add(c.getInt(0));
				paths.add(c.getString(1));
				try
				{
					byte[] bb = c.getBlob(2);
					Bitmap bm = BitmapFactory.decodeByteArray(bb, 0, bb.length);
					faces.add(bm);
				}
				catch(Exception ee){
					if (ids.size() > faces.size()){
						ids.remove(ids.size()-1);
						paths.remove(ids.size()-1);
					}
				}
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ee){}
		finally {
			if (c!=null) 
				c.close();
		}
	}

	public void GetAll(String sql){
		
		for (int i=0; i<faces.size();i++){
			faces.elementAt(i).recycle();
		}
		faces.clear();
		paths.clear();
		ids.clear();
		cids.clear();
		Cursor c= null;
		try
		{
			//c = db.query(dbManager.DB_Table, dbManager.COLS, null, null, null, null, "LastDate DESC");
			c = db.rawQuery(sql, null);
			if (c !=null)
			num = c.getCount();
			if (num==0){
				c.close();
				c=null;
				return;
			}
			c.moveToFirst();
			for (int i=0; i<num; i++){
				ids.add(c.getInt(0));
				paths.add(c.getString(1));
				cids.add(c.getInt(5));
				try
				{
					byte[] bb = c.getBlob(2);
					Bitmap bm = BitmapFactory.decodeByteArray(bb, 0, bb.length);
					faces.add(bm);
				}
				catch(Exception ee){
					if (ids.size() > faces.size()){
						ids.remove(ids.size()-1);
						paths.remove(ids.size()-1);
					}
				}
				c.moveToNext();
			}
			c.close();
			c=null;
		}
		catch(SQLException ee){};
	}

	public int GetContactID(int id){
		int bb=0;
	   	Cursor c = db.rawQuery("select Contact_ID from file_faces where _id=" + id+";", null);
		num = c.getCount();
		if (num==0){
			c.close();
			return bb;
		}
		c.moveToFirst();
		bb = c.getInt(0);
		c.close();
		return bb;
		
	}
	
	public String[] GetColumns(String tbl){
		Cursor c= null;
		String[] strs;
		try
		{
			c = db.query(tbl, dbManager.COLS, null, null, null, null, null);
			strs = c.getColumnNames();
			c.close();
			return strs;
		}
		catch(SQLException ee){};
		return null;
	}
	
	public Date GetFileDate(int id){
		Cursor c = db.rawQuery("select LastDate from "+dbManager.DB_Table+" where _id="+id+";", null);
		if (c.getCount()==0){
			c.close();
			return new Date();
		}
		
		c.moveToFirst();
		long ldate=c.getLong(0);
		c.close();
		return new Date(ldate*1000);
		
	}
	
	public void insert(String filePath, Bitmap bmp){
	//db.execSQL(sql, bindArgs)
		ContentValues values = new ContentValues();
		values.put("filePath",filePath.trim());
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bmp.compress(CompressFormat.JPEG, 100, bos); 
		byte[] bmpdata = bos.toByteArray(); 
		values.put("img", bmpdata);
		db.insert(dbManager.DB_Table, null, values);
	}

	public void insert(String filePath, Bitmap bmp, long lDate){
		
		ContentValues values = new ContentValues();
		values.put("filePath",filePath.trim());
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bmp.compress(CompressFormat.JPEG, 100, bos); 
		byte[] bmpdata = bos.toByteArray(); 
		values.put("img", bmpdata);
		values.put("LastDate", lDate);
		db.insert(dbManager.DB_Table, null, values);
	}
	
	public void insert(String filePath, Bitmap bmp, long lDate, int fNum, int contactID, int frameID){
		Cursor c= db.rawQuery("select _id from file_faces where filePath='from contact' and Contact_ID="+contactID+";", null);
		if (c.getCount() >0){
			c.close();
			update(bmp,lDate,contactID);
			
			return;
		}else
			c.close();
		ContentValues values = new ContentValues();
		values.put("filePath",filePath);
		byte[] bmpdata = null;
		if (bmp != null){
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			bmp.compress(CompressFormat.JPEG, 100, bos); 
			bmpdata = bos.toByteArray();
			try
			{
				bos.close();
			}
			catch(IOException ioe){};
		}
		values.put("img", bmpdata);
		values.put("LastDate", lDate);
		values.put("FaceNum", fNum);
		values.put("Contact_ID", contactID);
		values.put("FrameID", frameID);
		db.insert(dbManager.DB_Table, null, values);
	}
	
	public void insert(String filePath, Bitmap bmp, long lDate, int fNum) {
		Cursor c = db.rawQuery("select _id from file_faces where FaceNum="+fNum+" and filePath='"+filePath.trim()+"'"+";", null);
		if( c.getCount()>0){
			c.close();
			return;
		}

		ContentValues values = new ContentValues();
		values.put("filePath",filePath.trim());
		byte[] bmpdata = null;
		if (bmp != null){
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			bmp.compress(CompressFormat.JPEG, 100, bos); 
			bmpdata = bos.toByteArray();
			try
			{
				bos.close();
			}
			catch(IOException ioe){};
		}
		values.put("img", bmpdata);
		values.put("LastDate", lDate);
		values.put("FaceNum", fNum);
		db.insert(dbManager.DB_Table, null, values);
	}

	public void insert(String filePath, Bitmap bmp, long lDate, int fNum, int contactID) {
		Cursor c= db.rawQuery("select _id from file_faces where filePath='from contact' and Contact_ID="+contactID+";", null);
		if (c.getCount() >0){
			c.close();
			update(bmp,lDate,contactID);
			return;
		}
		else{
			c.close();
		}
			
		ContentValues values = new ContentValues();
		values.put("filePath",filePath);
		byte[] bmpdata = null;
		if (bmp != null){
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			bmp.compress(CompressFormat.JPEG, 100, bos); 
			bmpdata = bos.toByteArray();
			try
			{
				bos.close();
			}
			catch(IOException ioe){};
		}
		values.put("img", bmpdata);
		values.put("LastDate", lDate);
		values.put("FaceNum", fNum);
		values.put("Contact_ID", contactID);
		db.insert(dbManager.DB_Table, null, values);
	}
	
	public void Recreate(){
		dbH.onUpgrade(db,1,2);
		if (dbH !=null)
			db = dbH.getWritableDatabase();
	}
	
	public void RunCommand(String sql){
		try{
			db.execSQL(sql);
		}
		catch(SQLException ee){}
	}
	
	public void update(String filePath, Bitmap bmp, int id){
	
		ContentValues values = new ContentValues();
		values.put("filePath",filePath);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bmp.compress(CompressFormat.JPEG, 100, bos); 
		byte[] bmpdata = bos.toByteArray(); 
		values.put("img", bmpdata);
		db.update(dbManager.DB_Table, values, "_id="+id, null);
	}
	
	public void 				update(Bitmap bmp, int contactID){
	
		ContentValues values = new ContentValues();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bmp.compress(CompressFormat.JPEG, 100, bos); 
		byte[] bmpdata = bos.toByteArray(); 
		values.put("img", bmpdata);
		db.update(dbManager.DB_Table, values, "filePath='from contact' and Contact_ID="+contactID, null);
	}
	
	public void 				update(Bitmap bmp,long lDate, int contactID){
		ContentValues values = new ContentValues();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bmp.compress(CompressFormat.JPEG, 100, bos); 
		byte[] bmpdata = bos.toByteArray(); 
		values.put("img", bmpdata);
		//values.put("LastDate", lDate);
		db.update(dbManager.DB_Table, values, "filePath='from contact' and Contact_ID="+contactID, null);
	}
	
	public class 				DataBaseHelper extends SQLiteOpenHelper{
		 
	    //The Android's default system path of your application database.
	    private static final String DB_P = "/data/data/com.xulusoft.faceGallery/databases/";
	 
	    private static final String DB_N = "faceDB";
	 
	    public SQLiteDatabase db; 
	 
	    private final Context myContext;

	    public DataBaseHelper(Context context) {
	 
	    	super(context, DB_N, null, 1);
	        this.myContext = context;
	    }	
	    
	    public void createDataBase() throws IOException{
	 
	    	boolean dbExist = checkDataBase();
	    	
	    	if(!dbExist){
	    		Log.i("Database", "Not existing...");
	    		this.getReadableDatabase();
	        	try {
	 
	    			copyDataBase();
	 
	    		} catch (IOException e) {
	 
	        		throw new Error("Error copying database");
	 
	        	}
	    	}
	    }
	 
	    private boolean checkDataBase(){
    		String myPath = DB_P + DB_N;
    		File file = new File(myPath);
    		return file.isFile();
	    }
	 
	    private void copyDataBase() throws IOException{
	 
	    	Log.i("Database", "copying database...");
	    	//Open your local db as the input stream
	    	InputStream myInput = myContext.getAssets().open(DB_N);
	 
	    	// Path to the just created empty db
	    	String outFileName = DB_P + DB_N;
	    	
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	 
	    }
	    
	    public SQLiteDatabase openDataBase() throws SQLException{
	    	//Open the database
	    	return openDataBase(true);
	    }
	    
	    public SQLiteDatabase openDataBase(boolean read ) throws SQLException{
	 
	    	//Open the database
	        String myPath = DB_P + DB_N;
	        if (read)
	        	db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY|SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	        else
	        	db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE|SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	        return db;
	 
	    }
	    @Override
		public synchronized void close() {
	 
	    	    if(db != null)
	    		    db.close();
	 
	    	    super.close();
	 
		}
	    
		@Override
		public void onCreate(SQLiteDatabase db) {
			
		}
	 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	 
		}
	}
	
	public class dbHelper extends SQLiteOpenHelper {

	private static final String DB_CREATE = "CREATE Table " + dbManager.DB_Table +" (_id INTEGER PRIMARY KEY, filePath TEXT NOT NULL, LastDate INTEGER, img BOLD, Contact_ID INTEGER NOT NULL DEFAULT 0, FaceNum INTEGER NOT NULL DEFAULT 0, FrameID INTEGER);";
	
	public dbHelper(Context context, String name, int version) {
		super(context, name, null,version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Cursor c=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='file_faces')",null);
		if (c.getCount()==0)
		{
			//c.close();
			db.execSQL(DB_CREATE);
		}
		c.close();
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE if exists " + dbManager.DB_Table);
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}

}
}
