package org.bunnyblue.apkautoInstaller;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.bunnyblue.apkautoInstaller.ApkAdapter.ViewHolder;
import org.bunnyblue.apkautoInstaller.utils.ApkFinder;
import org.bunnyblue.apkautoInstaller.utils.ApkItem;
import org.bunnyblue.autoinstaller.util.AutoInstallerContext;
import org.bunnyblue.autoinstaller.util.IApkInstaller;
import org.bunnyblue.autoinstaller.util.InstallerUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class InstallerActivity extends Activity {
	ListView mListView;
	private int checkNum = 0; // 记录选中的条目数量
	LinkedList<ApkItem> apkPaths;
	ApkAdapter mApkAdapter;
	ApkItem apkPath;
	int apkIndex = 0;
	ProgressDialog mProgressDialog;
	ApkPickerReceiver mApkPickerReceiver;
	String dirPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		apkPaths = new LinkedList<ApkItem>();
		mApkPickerReceiver = new ApkPickerReceiver();
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ApkPickerReceiver.ACTION_END_PICK);
		mFilter.addAction(ApkPickerReceiver.ACTION_PICKED);
		mFilter.addAction(ApkPickerReceiver.ACTION_START_PICK);
		registerReceiver(mApkPickerReceiver, mFilter);
		mProgressDialog = new ProgressDialog(this);
		dirPath = getSharedPreferences("config", Context.MODE_PRIVATE).getString("dir", "");
		mProgressDialog.setTitle("searching apk@" + dirPath);
		mProgressDialog.setMessage("please  waiting");
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.show();
		mListView = (ListView) findViewById(R.id.listView);
		mApkAdapter = new ApkAdapter(apkPaths, this);
		mListView.setAdapter(mApkAdapter);

		initListviewClick();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_install) {
			installALl();
			return true;
		}
		if (id == R.id.action_select_all) {
			selectAll();
			return true;
		}
		if (id == R.id.action_unselect_all) {
			unselectAll();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initListviewClick() {

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
				ViewHolder holder = (ViewHolder) arg1.getTag();
				// 改变CheckBox的状态
				holder.apkCheckBox.toggle();
				// 将CheckBox的选中状况记录下来
				mApkAdapter.getIsSelected().put(arg2, holder.apkCheckBox.isChecked());
				// 调整选定条目
				if (holder.apkCheckBox.isChecked() == true) {
					checkNum++;
				} else {
					checkNum--;
				}
				// 用TextView显示
				// tv_show.setText("已选中" + checkNum + "项");

			}
		});

	}

	private void unselectAll() {

		// 遍历list的长度，将MyAdapter中的map值全部设为true
		for (int i = 0; i < apkPaths.size(); i++) {
			mApkAdapter.getIsSelected().put(i, false);
		}
		// 数量设为list的长度
		checkNum = apkPaths.size();
		// 刷新listview和TextView的显示
		dataChanged();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!InstallerUtils.isEnableAutoInstall()) {
			Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
			startActivityForResult(intent, 0);
		}

		if (apkPaths.size() == 0) {
			genAPks();
		}

	}

	private void genAPks() {
		AsyncTask<Void, Integer, Void> asyncTask = new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ApkFinder.findApks(new File(dirPath), apkPaths, getPackageManager());
				for (int i = 0; i < apkPaths.size(); i++) {
					mApkAdapter.getIsSelected().put(i, false);
				}
				Intent mIntent = new Intent(ApkPickerReceiver.ACTION_END_PICK);
				sendBroadcast(mIntent);
				return null;
			}

		};
		asyncTask.execute(new Void[] {});
		// Thread thread = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// ApkFinder.findApks(new File("/sdcard/"), apkPaths,
		// getPackageManager());
		// Intent mIntent = new Intent(ApkPickerReceiver.ACTION_END_PICK);
		// sendBroadcast(mIntent);
		// }
		// });
		// thread.start();

	}

	// 刷新listview和TextView的显示
	private void dataChanged() {
		// 通知listView刷新
		mApkAdapter.notifyDataSetChanged();
		// TextView显示最新的选中数目
		// tv_show.setText("已选中" + checkNum + "项");
	}

	private void selectAll() {

		// 遍历list的长度，将MyAdapter中的map值全部设为true
		for (int i = 0; i < apkPaths.size(); i++) {
			mApkAdapter.getIsSelected().put(i, true);
		}
		// 数量设为list的长度
		checkNum = apkPaths.size();
		// 刷新listview和TextView的显示
		dataChanged();

	}

	private void installALl() {
		HashMap<Integer, Boolean> apks = mApkAdapter.getIsSelected();
		Iterator iter = apks.entrySet().iterator();

		final LinkedList<ApkItem> apksList = new LinkedList<ApkItem>();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Integer key = (Integer) entry.getKey();
			Boolean val = (Boolean) entry.getValue();
			if (val) {
				ApkItem apkPath = mApkAdapter.getItem(key.intValue());
				System.out.println(apkPath);
				apksList.add(apkPath);
				// String fileName = "/sdcard/test.apk";

			}
		}

		apkIndex = 0;
		IApkInstaller m = new IApkInstaller() {

			@Override
			public void startInstall(String name, String path) {
				// TODO Auto-generated method stub

			}

			@Override
			public void endInstall(String name, String path) {
				if (apkIndex == apksList.size()) {
					System.out.println("InstallerActivity.installALl().new IApkInstaller() {...}.endInstall()");
					apkIndex = 0;
					return;
				}
				apkPath = apksList.get(apkIndex);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(apkPath.getPath())),
						"application/vnd.android.package-archive");

				startActivity(intent);
				apkIndex++;

			}
		};
		AutoInstallerContext.setApkInstallMonitor(m);
		m.endInstall("", "");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mApkPickerReceiver);
	}

	class ApkPickerReceiver extends BroadcastReceiver {
		public static final String ACTION_START_PICK = "ACTION_ApkPickerReceiver_START_PICK";
		public static final String ACTION_PICKED = "ACTION_ApkPickerReceiver_PICKED";
		public static final String ACTION_END_PICK = "ACTION_ApkPickerReceiver_END";

		public ApkPickerReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_PICKED)) {
				mProgressDialog.setMessage("process " + intent.getStringExtra("path"));
			} else if (intent.getAction().equals(ACTION_END_PICK)) {
				mProgressDialog.dismiss();
				dataChanged();
			}
		}
	}

}
