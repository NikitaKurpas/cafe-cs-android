package com.bnsoft.cafe;

import android.util.Log;

public class Logger {

	private boolean ENABLED = true;
	public final String TAG_DBG = "BNSOFT_DBG";
	public final String TAG_INF = "BNSOFT_INF";

	public Logger() {

	}

	public void l(String tag, String msg) {
		if (ENABLED)
			Log.d(tag, msg);
	}

}
