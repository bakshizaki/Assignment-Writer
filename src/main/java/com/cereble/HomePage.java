package com.cereble;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomePage extends ListActivity{
String options[] = {"Select text from image","Select text from Web","Scan handwriting","Generate output","ScanHW Debug"};
String classes[] = {"TextImage","SimpleBrowser","ScanHW","GenOutput","ScanHWDebug"};
@Override
protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setListAdapter(new ArrayAdapter<>(HomePage.this, android.R.layout.simple_list_item_1, options));

//	Thread timer2 = new Thread() {
//		public void run()
//		{
//			try {
//				sleep(5000);
//			}
//			catch(InterruptedException e) {
//				e.printStackTrace();
//			}
//
//		}
//	};
//	timer2.start();
//	
}
@Override
protected void onListItemClick(ListView l, View v, int position, long id) {
	// TODO Auto-generated method stub
	super.onListItemClick(l, v, position, id);
	String activity_class = classes[position];
	try {
		Class ourClass = Class.forName("com.cereble."+activity_class);
		Intent ourIntent = new Intent(HomePage.this,ourClass);
		startActivity(ourIntent);
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
@Override
public boolean onCreateOptionsMenu(Menu menu) {
	// TODO Auto-generated method stub
	super.onCreateOptionsMenu(menu);
	MenuInflater mi = getMenuInflater();
	mi.inflate(R.menu.mainmenu, menu);
	return true;
}
@Override
public boolean onOptionsItemSelected(MenuItem item) {
	// TODO Auto-generated method stub
	super.onOptionsItemSelected(item);
	switch(item.getItemId())
	{
	case R.id.About_Us:
		Intent a = new Intent("android.intent.action.ABOUTUS");
		startActivity(a);
		break;
	case R.id.Preferences:
		
		break;
	case R.id.exit:
		finish();
		break;
	}
	return false;
}




}
