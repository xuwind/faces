package com.xulusoft.faceGallery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.R.color;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;


/*
* Copyright (C) 2011-2012 George Yunaev
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
*/
public class fileDialog implements OnItemClickListener, OnClickListener
{
	public interface Result
	{
		void onChooseDirectory( String dir );
	}
	static public int dHeight=500;
	public boolean done =false;
	List<File> m_entries = new ArrayList< File >();
	File m_currentDir;
	Context m_context;
	AlertDialog m_alertDialog;
	ListView m_list;
	public Result m_result = null;
	
	public class DirAdapter extends ArrayAdapter< File >
	{
		public DirAdapter( int resid )
		{
			super( m_context, resid, m_entries );
		}
		// This function is called to show each view item
		@SuppressWarnings("null")
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
		
			View v = convertView;  
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item, null);  
			}        
			TextView textview = (TextView) v.findViewById(R.id.fileName); 
			
			//TextView textview = (TextView) super.getView( position, convertView, parent );
			
			if ( m_entries.get(position) == null )
			{
				textview.setText( ".." );
				//textview.setCompoundDrawablesWithIntrinsicBounds( m_context.getResources().getDrawable( R.drawable.parentIcon ), null, null, null );
			}
			else
			{
				textview.setText( m_entries.get(position).getName());
				//textview.setCompoundDrawablesWithIntrinsicBounds( m_context.getResources().getDrawable( R.drawable.dirIcon), null, null, null );
			}
			//return textview;
			return v;
		}
	}
	private void listDirs()
	{
		m_entries.clear();
		// Get files
		File[] files = m_currentDir.listFiles();
		// Add the ".." entry
		if ( m_currentDir.getParent() != null )
		m_entries.add( new File("..") );
		if ( files != null )
		{
			for ( File file : files ){
			
				if ( !file.isDirectory() )
					continue;
				m_entries.add( file );
			}
		}
		Collections.sort( m_entries, new Comparator<File>() { 
				public int compare(File f1, File f2)
				{
					return f1.getName().toLowerCase().compareTo( f2.getName().toLowerCase() );
				}
			});
	}
	public fileDialog( Context context, Result res, String startDir )
	{
		m_context = context;
		m_result = res;

		if ( startDir != null )
			m_currentDir = new File( startDir );
		else
			m_currentDir = Environment.getExternalStorageDirectory();
		listDirs();
		DirAdapter adapter = new DirAdapter( R.layout.list_item );
		AlertDialog.Builder builder = new AlertDialog.Builder( context );
		builder.setTitle(startDir);
		builder.setAdapter( adapter, this );
		builder.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if ( m_result != null )
						m_result.onChooseDirectory( m_currentDir.getAbsolutePath() );
					dialog.dismiss();
					
				}
			});
		builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					m_currentDir =null;
					dialog.cancel();
					}	
			});
		m_alertDialog = builder.create();
		m_list = m_alertDialog.getListView();
		m_list.setOnItemClickListener( this );
		m_alertDialog.show();
		m_list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, dHeight));
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View list, int pos, long id )
	{
			if ( pos < 0 || pos >= m_entries.size() )
			return;
			if ( m_entries.get( pos ).getName().equals( ".." ) )
				m_currentDir = m_currentDir.getParentFile();
			else
				m_currentDir = m_entries.get( pos );
			listDirs();
			DirAdapter adapter = new DirAdapter( R.layout.list_item);
			m_alertDialog.setTitle(m_currentDir.getAbsolutePath());
			m_list.setAdapter( adapter );
			m_list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, dHeight));

	}
	public void onClick(DialogInterface dialog, int which)
	{
	}
}
