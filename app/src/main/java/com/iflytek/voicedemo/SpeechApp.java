package com.iflytek.voicedemo;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;
import com.iflytek.voicedemo.R;

public class SpeechApp extends Application{
  
	@Override
	public void onCreate() {
		// 应用程序入口处调用,避免手机内存过小，杀死后台进程,造成SpeechUtility对象为null
		// 设置你申请的应用appid
		SpeechUtility.createUtility(SpeechApp.this, "appid="+getString(R.string.app_id));
		super.onCreate();
	}
}
