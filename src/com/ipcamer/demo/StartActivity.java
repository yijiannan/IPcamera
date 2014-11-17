package com.ipcamer.demo;

import java.util.Date;

import object.p2pipcam.nativecaller.BridgeService;
import object.p2pipcam.nativecaller.NativeCaller;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class StartActivity extends Activity {
	//运行后第一个界面
	//定时器显示公司logo
	private static final String LOG_TAG = "StartActivity";
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Intent in = new Intent(StartActivity.this, AddCameraActivity.class);//跳转添加页面
			startActivity(in);
			finish();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "StartActivity onCreate");
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.start);
		Intent intent = new Intent();
		intent.setClass(StartActivity.this, BridgeService.class);//1先开启service
		startService(intent);
		new Thread(new Runnable() {//定时器，3秒后跳转
			@Override
			public void run() {
				try {
					NativeCaller
							.PPPPInitial("EBGAEOBOKHJMHMJMENGKFIEEHBMDHNNEGNEBBCCCBIIHLHLOCIACCJOFHHLLJEKHBFMPLMCHPHMHAGDHJNNHIFBAMC");//2调jni库中的方法
					long lStartTime = new Date().getTime();
					int nRes = NativeCaller.PPPPNetworkDetect();
					long lEndTime = new Date().getTime();
					if (lEndTime - lStartTime <= 1000) {
						Thread.sleep(3000);
					}
					Message msg = new Message();
					mHandler.sendMessage(msg);
				} catch (Exception e) {

				}
			}
		}).start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;
		return super.onKeyDown(keyCode, event);
	}

}