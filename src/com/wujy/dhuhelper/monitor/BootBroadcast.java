package com.wujy.dhuhelper.monitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.wujy.dhuhelper.Globe;

public class BootBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent mintent) {
		try {
			if (Intent.ACTION_BOOT_COMPLETED.equals(mintent.getAction())
					|| Intent.ACTION_USER_PRESENT.equals(mintent.getAction())
					|| ConnectivityManager.CONNECTIVITY_ACTION.equals(mintent.getAction())) {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
				Globe.monitor_auto_startup = settings.getBoolean(
						"monitor_auto_startup", false);
				if (!Globe.monitor_auto_startup)
					return;

				// 启动完成
				Intent it = new Intent(context, AlarmReceiver.class);
				it.setAction("wujy.alarm.action");
				PendingIntent sender = PendingIntent.getBroadcast(context, 0,
						it, 0);
				long firsttime = SystemClock.elapsedRealtime();
				AlarmManager am = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);

				// 10秒一个周期，不停的发送广播
//				am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firsttime,
//						10 * 1000, sender);
				am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firsttime,
						10 * 1000, sender);
				
				Log.i("MonitorService", "sendBroadcastRepeat is start");
				//Toast.makeText(context, "Start!", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}