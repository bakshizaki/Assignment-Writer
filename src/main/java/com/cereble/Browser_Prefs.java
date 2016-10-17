package com.cereble;



import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Browser_Prefs extends PreferenceActivity{

	boolean m_view=true;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.brow_prefs);
		
		  final CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("mobile_view");
		  final SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		    checkboxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {            
		        public boolean onPreferenceChange(Preference preference, Object newValue) {
		        	//Toast.makeText(getApplicationContext(), "Preference changed", Toast.LENGTH_SHORT).show();
		        	if(!getPrefs.getBoolean("mobile_view", true))
		    		{
		    		m_view = true;
		    		Toast.makeText(getApplicationContext(), "Mobile View", Toast.LENGTH_SHORT).show();
		    		  Intent i = new Intent();
		  		    Bundle b = new Bundle();
		  		    b.putBoolean("m_view", m_view);
		  		    
		  		    i.putExtras(b);
		  		    setResult(RESULT_OK,i);
		    		}
		    		else
		    		{
		    			m_view=false;
		    			Toast.makeText(getApplicationContext(), "Desktop View", Toast.LENGTH_SHORT).show();
		    			  Intent i = new Intent();
		    			    Bundle b = new Bundle();
		    			    b.putBoolean("m_view", m_view);
		    			    
		    			    i.putExtras(b);
		    			    setResult(RESULT_OK,i);
		    		}
		            return true;
		        }
		    }); 
		  /*  Intent i = new Intent();
		    Bundle b = new Bundle();
		    b.putBoolean("m_view", m_view);
		    
		    i.putExtras(b);
		    setResult(RESULT_OK,i);*/
		   // finish();

	}


}
