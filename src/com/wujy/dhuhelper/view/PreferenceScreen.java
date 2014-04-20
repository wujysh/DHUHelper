package com.wujy.dhuhelper.view;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.wujy.dhuhelper.R;

//继承PreferenceActivity，并实现OnPreferenceChangeListener和OnPreferenceClickListener监听接口
public class PreferenceScreen extends SherlockPreferenceActivity {
//	// 定义相关变量
//	String updateSwitchKey;
//	String updateFrequencyKey;
//	CheckBoxPreference updateSwitchCheckPref;

	// ListPreference updateFrequencyListPref;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//getSupportActionBar().setDisplayUseLogoEnabled(true);
		//getSupportActionBar().setLogo(R.drawable.logo_actionbar);
		
		//getPreferenceManager().setSharedPreferencesName("dhuhelper");  
		
		// 从xml文件中添加Preference项
		addPreferencesFromResource(R.xml.preferences);


	}
	//
	// @Override
	// public boolean onPreferenceChange(Preference preference, Object newValue)
	// {
	// // TODO Auto-generated method stub
	// Log.v("SystemSetting", "preference is changed");
	// Log.v("Key_SystemSetting", preference.getKey());
	// //判断是哪个Preference改变了
	// if(preference.getKey().equals(updateSwitchKey))
	// {
	// Log.v("SystemSetting", "checkbox preference is changed");
	// }
	// else if(preference.getKey().equals(updateFrequencyKey))
	// {
	// Log.v("SystemSetting", "list preference is changed");
	// }
	// else
	// {
	// //如果返回false表示不允许被改变
	// return false;
	// }
	// //返回true表示允许改变
	// return true;
	// }
	//
	// @Override
	// public boolean onPreferenceClick(Preference preference) {
	// // TODO Auto-generated method stub
	// Log.v("SystemSetting", "preference is clicked");
	// Log.v("Key_SystemSetting", preference.getKey());
	// //判断是哪个Preference被点击了
	// if(preference.getKey().equals(updateSwitchKey))
	// {
	// Log.v("SystemSetting", "checkbox preference is clicked");
	// }
	// else if(preference.getKey().equals(updateFrequencyKey))
	// {
	// Log.v("SystemSetting", "list preference is clicked");
	// }
	// else
	// {
	// return false;
	// }
	// return true;
	// }
}