package com.wujy.dhuhelper.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wujy.dhuhelper.Globe;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (intent.getAction().equals("wujy.alarm.action")) {
				if (!Globe.monitor_auto_startup)
					return;
				Log.i("MonitorService", "Alarm Received");
				Intent it = new Intent();
				it.setClass(context, MonitorService.class);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// 启动service
				// 多次调用startService并不会启动多个service 而是会多次调用onStart
				context.startService(it);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}