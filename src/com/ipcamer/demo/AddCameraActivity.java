package com.ipcamer.demo;



//拷贝来的

import java.util.Map;
import java.util.TimerTask;
import object.p2pipcam.nativecaller.BridgeService;
import object.p2pipcam.nativecaller.BridgeService.p2pAddCamerLitener;
import object.p2pipcam.nativecaller.BridgeService.p2pIpcamClientLitener;
import object.p2pipcam.nativecaller.NativeCaller;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AddCameraActivity extends Activity implements OnClickListener,
		p2pAddCamerLitener, OnItemSelectedListener, p2pIpcamClientLitener {
	//实现jar包接口
	//第二个页面添加寻找局域网内的摄像机
	private EditText userEdit = null;
	private EditText pwdEdit = null;
	private EditText didEdit = null;
	private TextView textView_top_show = null;
	private Button done;
	private static final int SEARCH_TIME = 3000;
	private int option = ContentCommon.INVALID_OPTION;//引用别的类的常量 无效选项
	private int CameraType = ContentCommon.CAMERA_TYPE_MJPEG;
	private Button btnSearchCamera;
	private SearchListAdapter listAdapter = null;//搜索列表适配器
	private ProgressDialog progressdlg = null;
	private boolean isSearched;
	private MyBroadCast receiver;//内部类的广播
	private WifiManager manager = null;
	private ProgressBar progressBar = null;
	private static final String STR_DID = "did";
	private static final String STR_MSG_PARAM = "msgparam";
	private MyWifiThread myWifiThread = null;//内部类wifi线程
	private boolean blagg = false;
	private Intent intentbrod = null;
	private WifiInfo info = null;
	boolean bthread = true;
	private Button button_play = null;
	private Button button_setting = null;
	private int tag = 0;

	class MyTimerTask extends TimerTask {//定时器类

		public void run() {
			updateListHandler.sendEmptyMessage(100000);//Handler添加东西
		}
	};

	class MyWifiThread extends Thread {
		@Override
		public void run() {
			while (blagg == true) {
				super.run();

				updateListHandler.sendEmptyMessage(100000);//Handler添加东西
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private class MyBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			AddCameraActivity.this.finish();
			Log.d("tag", "AddCameraActivity.this.finish()");
		}

	}

	class StartPPPPThread implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(100);
				StartCameraPPPP();//下边的方法
			} catch (Exception e) {

			}
		}
	}

	private void StartCameraPPPP() {
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		NativeCaller.StartPPPP(SystemValue.deviceId, SystemValue.deviceName,
				SystemValue.devicePass);//直接调用提供接口内函数//利用SystemValue
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_camera);
		Intent in = getIntent();
		progressdlg = new ProgressDialog(this);
		progressdlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdlg.setMessage(getString(R.string.searching_tip));
		listAdapter = new SearchListAdapter(this);
		findView();
		manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);//WIFI
		InitParams();

		BridgeService.setP2pAddCamerLitener(this);//直接调用提供接口内函数
		receiver = new MyBroadCast();//实例化receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction("finish");
		registerReceiver(receiver, filter);
		intentbrod = new Intent("drop");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		blagg = true;
	}

	private void InitParams() {//设置监听并且初始化

		done.setOnClickListener(this);
		btnSearchCamera.setOnClickListener(this);
	}

	@Override
	protected void onStop() {
		if (myWifiThread != null) {
			blagg = false;
		}
		progressdlg.dismiss();
		NativeCaller.StopSearch();//直接调用提供接口内函数
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		NativeCaller.Free();//直接调用提供接口内函数
		Intent intent = new Intent();
		intent.setClass(this, BridgeService.class);
		stopService(intent);
		tag = 0;
	}

	Runnable updateThread = new Runnable() {

		public void run() {
			NativeCaller.StopSearch();//直接调用提供接口内函数
			progressdlg.dismiss();
			Message msg = updateListHandler.obtainMessage();//Handler添加东西
			msg.what = 1;
			updateListHandler.sendMessage(msg);//Handler添加东西
		}
	};
	// 15576341699
	//定时器类调用
	Handler updateListHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				listAdapter.notifyDataSetChanged();
				if (listAdapter.getCount() > 0) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							AddCameraActivity.this);
					dialog.setTitle(getResources().getString(
							R.string.add_search_result));//搜索结果
					dialog.setPositiveButton(
							getResources().getString(R.string.refresh),//刷新
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startSearch();//刷新按钮的响应操作
								}
							});
					dialog.setNegativeButton(
							getResources().getString(R.string.str_cancel), null);
					dialog.setAdapter(listAdapter,//dialog添加搜索列表适配器
							new DialogInterface.OnClickListener() {//添加点击相应

								@Override
								public void onClick(DialogInterface dialog,
										int arg2) {
									Map<String, Object> mapItem = (Map<String, Object>) listAdapter
											.getItemContent(arg2);
									if (mapItem == null) {
										return;
									}

									String strName = (String) mapItem
											.get(ContentCommon.STR_CAMERA_NAME);
									String strDID = (String) mapItem
											.get(ContentCommon.STR_CAMERA_ID);
									String strUser = ContentCommon.DEFAULT_USER_NAME;
									String strPwd = ContentCommon.DEFAULT_USER_PWD;
									userEdit.setText(strUser);
									pwdEdit.setText(strPwd);
									didEdit.setText(strDID);//搜索后点击结果，把搜索结果赋值到相应控件

								}
							});

					dialog.show();
				} else {
					Toast.makeText(AddCameraActivity.this,
							getResources().getString(R.string.add_search_no),
							Toast.LENGTH_LONG).show();
					isSearched = false;// 如果没有搜索到，可以再次搜索
				}
			}
		}
	};

//	public static String int2ip(long ipInt) {//此函数功能未知
//		StringBuilder sb = new StringBuilder();
//		sb.append(ipInt & 0xFF).append(".");
//		sb.append((ipInt >> 8) & 0xFF).append(".");
//		sb.append((ipInt >> 16) & 0xFF).append(".");
//		sb.append((ipInt >> 24) & 0xFF);
//		return sb.toString();
//	}

	//描述：局域网内搜索ID;
	//刷新按钮和搜索的响应操作 
	private void startSearch() {
		listAdapter.ClearAll();
		progressdlg.setMessage(getString(R.string.searching_tip));//进度条的提示语
		progressdlg.show();
		new Thread(new SearchThread()).start();
		updateListHandler.postDelayed(updateThread, SEARCH_TIME);//Handler延时3000操作
	}

	private class SearchThread implements Runnable {
		@Override
		public void run() {
			Log.d("tag", "startSearch");
			NativeCaller.StartSearch();//直接调用提供接口内函数
		}
	}

	private void findView() {
		progressBar = (ProgressBar) findViewById(R.id.main_model_progressBar1);
		textView_top_show = (TextView) findViewById(R.id.login_textView1);
		button_play = (Button) findViewById(R.id.play);
		//button_setting = (Button) findViewById(R.id.setting);
		done = (Button) findViewById(R.id.done);
		done.setText("   连       接       ");
		userEdit = (EditText) findViewById(R.id.editUser);
		pwdEdit = (EditText) findViewById(R.id.editPwd);
		didEdit = (EditText) findViewById(R.id.editDID);
		btnSearchCamera = (Button) findViewById(R.id.btn_searchCamera);
		button_play.setOnClickListener(this);
		//button_setting.setOnClickListener(this);
	}

	// 点击事件做相应的操作
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play:
			Intent intent = new Intent(AddCameraActivity.this,
					PlayActivity.class);// 跳转到播放页面
			startActivity(intent);
			break;
	
		case R.id.done:
			if (tag == 1) {
				Toast.makeText(AddCameraActivity.this, "摄像机已经处于在线状态...", 0)
						.show();
			} else if (tag == 2) {
				Toast.makeText(AddCameraActivity.this, "正在连接，请稍后...", 0).show();
			} else {
				done();
			}

			break;
		case R.id.btn_searchCamera:
			searchCamera();//搜索按钮相应操作
			break;

		default:
			break;
		}
	}
	
	
//查找在线的摄像机
	private void searchCamera() {
		if (!isSearched) {//boolean进行判断
			isSearched = true;
			startSearch();
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					AddCameraActivity.this);
			dialog.setTitle(getResources()
					.getString(R.string.add_search_result));
			dialog.setPositiveButton(
					getResources().getString(R.string.refresh),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							startSearch();

						}
					});
			dialog.setNegativeButton(
					getResources().getString(R.string.str_cancel), null);
			dialog.setAdapter(listAdapter,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int arg2) {
							Map<String, Object> mapItem = (Map<String, Object>) listAdapter
									.getItemContent(arg2);
							if (mapItem == null) {
								return;
							}

							String strName = (String) mapItem
									.get(ContentCommon.STR_CAMERA_NAME);
							String strDID = (String) mapItem
									.get(ContentCommon.STR_CAMERA_ID);
							String strUser = ContentCommon.DEFAULT_USER_NAME;
							String strPwd = ContentCommon.DEFAULT_USER_PWD;
							userEdit.setText(strUser);
							pwdEdit.setText(strPwd);
							didEdit.setText(strDID);

						}
					});
			dialog.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			AddCameraActivity.this.finish();
			return false;
		}
		return false;
	}

	
	//连接
	private void done() {
		Intent in = new Intent();
		String strUser = userEdit.getText().toString();
		String strPwd = pwdEdit.getText().toString();
		String strDID = didEdit.getText().toString();

		if (strDID.length() == 0) {
			Toast.makeText(AddCameraActivity.this,
					getResources().getString(R.string.input_camera_id), 0)
					.show();
			return;
		}

		if (strUser.length() == 0) {
			Toast.makeText(AddCameraActivity.this,
					getResources().getString(R.string.input_camera_user), 0)
					.show();
			return;
		}
		// in.setAction(ContentCommon.STR_CAMERA_INFO_RECEIVER);
		if (option == ContentCommon.INVALID_OPTION) {//设置选项
			option = ContentCommon.ADD_CAMERA;
		}
		in.putExtra(ContentCommon.CAMERA_OPTION, option);
		in.putExtra(ContentCommon.STR_CAMERA_ID, strDID);
		in.putExtra(ContentCommon.STR_CAMERA_USER, strUser);
		in.putExtra(ContentCommon.STR_CAMERA_PWD, strPwd);
		in.putExtra(ContentCommon.STR_CAMERA_TYPE, CameraType);
		progressBar.setVisibility(View.VISIBLE);
		// sendBroadcast(in);
		SystemValue.deviceName = strUser;//利用SystemValue
		SystemValue.deviceId = strDID;//利用SystemValue
		SystemValue.devicePass = strPwd;//利用SystemValue
		BridgeService.setP2pIpcamClientLitener(this);//直接调用提供接口内函数
		NativeCaller.Init();//直接调用提供接口内函数
		new Thread(new StartPPPPThread()).start();//开线程经行连接
		// overridePendingTransition(R.anim.in_from_right,
		// R.anim.out_to_left);// 进入动画
		// finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			didEdit.setText(scanResult);
		}
	}

	/**
	 * BridgeService callback
	 * **/
	@Override
	public void callBackSearchResultData(int cameraType, String strMac,
			String strName, String strDeviceID, String strIpAddr, int port) {
		if (!listAdapter.AddCamera(strMac, strName, strDeviceID)) {
			return;
		}
	}

	public String getInfoSSID() {

		info = manager.getConnectionInfo();
		String ssid = info.getSSID();
		return ssid;
	}

	public int getInfoIp() {

		info = manager.getConnectionInfo();
		int ip = info.getIpAddress();
		return ip;
	}

	private Handler PPPPMsgHandler = new Handler() {
		public void handleMessage(Message msg) {

			Bundle bd = msg.getData();
			int msgParam = bd.getInt(STR_MSG_PARAM);
			int msgType = msg.what;
			String did = bd.getString(STR_DID);
			switch (msgType) {
			case ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS:
				int resid;
				switch (msgParam) {
				case ContentCommon.PPPP_STATUS_CONNECTING:
					resid = R.string.pppp_status_connecting;
					progressBar.setVisibility(View.VISIBLE);
					tag = 2;
					break;
				case ContentCommon.PPPP_STATUS_CONNECT_FAILED:
					resid = R.string.pppp_status_connect_failed;
					progressBar.setVisibility(View.GONE);
					tag = 0;
					break;
				case ContentCommon.PPPP_STATUS_DISCONNECT:
					resid = R.string.pppp_status_disconnect;
					progressBar.setVisibility(View.GONE);
					tag = 0;
					break;
				case ContentCommon.PPPP_STATUS_INITIALING:
					resid = R.string.pppp_status_initialing;
					progressBar.setVisibility(View.VISIBLE);
					tag = 2;
					break;
				case ContentCommon.PPPP_STATUS_INVALID_ID:
					resid = R.string.pppp_status_invalid_id;
					progressBar.setVisibility(View.GONE);
					tag = 0;
					break;
				case ContentCommon.PPPP_STATUS_ON_LINE:
					resid = R.string.pppp_status_online;
					progressBar.setVisibility(View.GONE);
					tag = 1;
					break;
				case ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE:
					resid = R.string.device_not_on_line;
					progressBar.setVisibility(View.GONE);
					tag = 0;
					break;
				case ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT:
					resid = R.string.pppp_status_connect_timeout;
					progressBar.setVisibility(View.GONE);
					tag = 0;
					break;
				default:
					resid = R.string.pppp_status_unknown;
				}
				textView_top_show.setText(getResources().getString(resid));
				if (msgParam == ContentCommon.PPPP_STATUS_ON_LINE) {
					NativeCaller.PPPPGetSystemParams(did,
							ContentCommon.MSG_TYPE_GET_PARAMS);//直接调用提供接口内函数
				}
				if (msgParam == ContentCommon.PPPP_STATUS_INVALID_ID
						|| msgParam == ContentCommon.PPPP_STATUS_CONNECT_FAILED
						|| msgParam == ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE
						|| msgParam == ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT) {
					NativeCaller.StopPPPP(did);//直接调用提供接口内函数
				}
				break;
			case ContentCommon.PPPP_MSG_TYPE_PPPP_MODE:
				break;

			}

		}
	};

	@Override
	public void BSMsgNotifyData(String did, int type, int param) {
		Log.d("测试", "type:" + type + " param:" + param);
		Bundle bd = new Bundle();
		Message msg = PPPPMsgHandler.obtainMessage();
		msg.what = type;
		bd.putInt(STR_MSG_PARAM, param);
		bd.putString(STR_DID, did);
		msg.setData(bd);
		PPPPMsgHandler.sendMessage(msg);
		if (type == ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS) {
			intentbrod.putExtra("ifdrop", param);
			sendBroadcast(intentbrod);
		}

	}

	@Override
	public void BSSnapshotNotify(String did, byte[] bImage, int len) {
		// TODO Auto-generated method stub

	}

	@Override
	public void callBackUserParams(String did, String user1, String pwd1,
			String user2, String pwd2, String user3, String pwd3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
