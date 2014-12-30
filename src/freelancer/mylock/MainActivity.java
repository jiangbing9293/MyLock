package freelancer.mylock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = new Intent(MainActivity.this, MyService.class);
		startService(intent);
		TextView myTxt = (TextView)findViewById(R.id.mytxt);
		myTxt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					finish();
			}
		});
		DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName componentName = new ComponentName(getApplicationContext(),
				LockReceiver.class);
		// 判断是否有权限(激活了设备管理器)
		if (policyManager.isAdminActive(componentName)) {
			// 直接锁屏
			policyManager.lockNow();
			// android.os.Process.killProcess(android.os.Process.myPid());

		} else {
			// 激活设备管理器获取权限
			activeManager(componentName);
		}

	}
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		// mSensorManager.unregisterListener(this);
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	private void activeManager(ComponentName componentName) {
		// 使用隐式意图调用系统方法来激活指定的设备管理器
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "锁屏");
		startActivity(intent);
	}
}
