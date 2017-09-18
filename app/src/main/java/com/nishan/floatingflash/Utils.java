package com.nishan.floatingflash;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
/**
 * Created by Nishan Chathuranga on 9/17/2017.
 */
public class Utils {
	public static String LogTag = "TEST LOG";
	public static boolean canDrawOverlays(Context context){
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}else{
			return Settings.canDrawOverlays(context);
		}
	}
}