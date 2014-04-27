package org.sjutas.dhu;

import java.util.ArrayList;

import org.sjutas.dhu.net.NetClient;

/**
 * @author wujy 一些静态全局变量
 */
public class Globe {
	public static String[] sCookieString;
	public static int sCookieStringCnt = 0;
	public static String sCookieStringAll;

	public static ArrayList<String[]> sHideParams = new ArrayList<String[]>();

	public static String sName_jw, sId_jw, sClassName_jw;
	public static String sName_lib, sId_lib, sMsg_lib, sIdentityId_lib;

	public static NetClient sNetClient;

	public static int sMenuPosX = 0, sMenuPosY = 0;

	public static boolean isLoginJW = false, isLoginLib = false;

	public static ArrayList<String[]> mFirstListItems = new ArrayList<String[]>();
	public static ArrayList<String[]> mSecondListItems = new ArrayList<String[]>();
	public static ArrayList<String[]> mThirdListItems = new ArrayList<String[]>();
	public static ArrayList<String[]> mScoreListItems = new ArrayList<String[]>();

	public static void clearAll() {
		//sNetClient.isRunning = false;
		sNetClient = null;
		sCookieString = null;
		sName_jw = sName_lib = null;
		sId_jw = sId_lib = null;
		sClassName_jw = null;
		sMsg_lib = sIdentityId_lib = null;
		sHideParams.clear();

		sCookieStringCnt = 0;
		sMenuPosX = sMenuPosY = 0;
		isLoginJW = isLoginLib = false;
		mFirstListItems.clear();
		mSecondListItems.clear();
		mThirdListItems.clear();
	}

	public static boolean news_load_when_startup = true;
	public static boolean news_detail_load_images = true;
	public static boolean news_detail_reset_style = true;
	public static int news_detail_images_width = 300;

	public static boolean autologin_jw = false;
	public static boolean autologin_lib = false;

	public static boolean monitor_auto_startup = false;
	public static int monitor_refresh_interval = 60000;
	public static int monitor_courseNo = 0;
	public static int monitor_courseId = 0;
	public static String monitor_courseName = "";
	public static boolean monitor_auto_select_when_available = false;
	public static boolean monitor_courses = false;
	
	public static boolean monitor_dhxw = true;
	public static boolean monitor_xngg = true;
	public static boolean monitor_jwxx = true;
	public static boolean monitor_scorelist = false;

	public static String cache_news_dhxw = null;
	public static String cache_news_xngg = null;
	public static String cache_news_jwxx = null;
	public static String cache_scorelist = null;
	
	public static long cache_news_dhxw_time = 0;
	public static long cache_news_xngg_time = 0;
	public static long cache_news_jwxx_time = 0;
	public static long cache_scorelist_time = 0;
}
