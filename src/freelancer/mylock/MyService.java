package freelancer.mylock;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class MyService extends Service implements SensorEventListener {
	AlarmManager mAlarmManager = null;
	PendingIntent mPendingIntent = null;

	private SensorManager mSensorManager;

	private boolean lock = false;
	public PowerManager pm = null;
	public WakeLock mWakelock = null;
	public KeyguardManager keyguardManager = null;
	public KeyguardLock mKeyguardLock = null;
	
	@Override
	public void onCreate() 
	{
		// start the service through alarm repeatly
		Intent intent = new Intent(getApplicationContext(), MyService.class);
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mPendingIntent = PendingIntent.getService(this, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		long now = System.currentTimeMillis();
		mAlarmManager.setInexactRepeating(AlarmManager.RTC, now,
				 10 * 1000, mPendingIntent);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 获取PowerManager的实例
		PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		// 得到一个WakeLock唤醒锁
		mWakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, "SimpleTimer");
		// 获得一个KeyguardManager的实例
		keyguardManager = (KeyguardManager) this
				.getSystemService(Context.KEYGUARD_SERVICE);
		// 得到一个键盘锁KeyguardLock
		mKeyguardLock = keyguardManager.newKeyguardLock("SimpleTimer");
		// unlockScreen();
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(this);
	}

	public boolean isServiceRunning(String strServiceName) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			String allServiceName = service.service.getClassName();
			if (allServiceName.equals(strServiceName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		float[] values = event.values;
		if (sensorType == Sensor.TYPE_ACCELEROMETER) {
			
			//System.out.println((int)values[0]+","+(int)values[1]+","+(int)values[2]);
			
			if (values[0] > 14) {
			
				if (keyguardManager != null)
					lock = keyguardManager.inKeyguardRestrictedInputMode();
				if (lock) {
						unlockScreen();
						System.out.println("unlock");
				} else {
					try {
						System.out.println("lock");
						lockScreen();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void unlockScreen() {

		if (!mWakelock.isHeld()) {
			// 唤醒屏幕
			mWakelock.acquire();
		}

		if (keyguardManager.inKeyguardRestrictedInputMode()) {
			// 解锁键盘
			// mKeyguardLock.disableKeyguard();
		}

	}

	public void lockScreen() {
		DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		policyManager.lockNow();
		/*
		 * // release screen if
		 * (!keyguardManager.inKeyguardRestrictedInputMode()) { // 锁键盘
		 * mKeyguardLock.reenableKeyguard(); }
		 */

		// 使屏幕休眠
		if (mWakelock.isHeld()) {
			mWakelock.release();
		}
	}

}
