package edu.xidian.beacons.rssi;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.logging.LogManager;
import org.altbeacon.beacon.logging.Loggers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.xidian.FindBeacons.FindBeacons;
import edu.xidian.FindBeacons.FindBeacons.OnBeaconsListener;
import edu.xidian.logtofile.LogcatHelper;

/**
 * 查找附近beacons,记录日志文件和屏幕显示beacon的rssi
 */
public class rssiActivity extends Activity {
	private final static String TAG = rssiActivity.class.getSimpleName();
    private FindBeacons mFindBeacons;
    
    // 每次扫描周期结束，执行此回调，获取附近beacons信息
    private OnBeaconsListener mBeaconsListener = new OnBeaconsListener() {
		@Override
		public void getBeacons(Collection<Beacon> beacons) {
			// 日志记录和屏幕显示Beacon信息
			String str = "beacons=" + beacons.size();
			LogManager.d(TAG,str);
			logToDisplay(str);
			for (Beacon beacon : beacons) {
				// 记录becaon的两个id，rssi和平均rssi
				str = beacon.getId2()+":"+beacon.getId3()+"="+beacon.getRssi()+","+beacon.getRunningAverageRssi();
				LogManager.d(TAG, str);
				logToDisplay(str);
			}	
		}
    	
    }; 
    
    private static LogcatHelper loghelper;  //日志文件
    private Button start_logfile; // 开始记录日志文件
    private Button end_logfile;   // 停止日志文件
    private String Logformat = "";  // 日志拟制符格式
    
    private Button mStart_btn;  // 开始监控(查找)beacons
    private Button mStop_btn;   // 停止监控(查找)beacons
    
    private EditText ScanPeriod_edit;  // 前台扫描周期
    
    /** rssi采样周期,即，计算该时间段内的平均RSSI（首末各去掉10%）*/
    private EditText SamplePeriod_edit;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 建议使用org.altbeacon.beacon.logging.LogManager.javaLogManager输出日志，altbeacon就是使用这种机制，便于发布版本时，减少输出日志信息。
		// 输出所有ERROR(Log.e()), WARN(Log.w()), INFO(Log.i()), DEBUG(Log.d()), VERBOSE(Log.v())
		// 对应日志级别由高到低
        LogManager.setLogger(Loggers.verboseLogger());
		
        // 全部不输出，在release版本中设置
        //LogManager.setLogger(Loggers.empty());
		
        // 输出ERROR(Log.e()), WARN(Log.w()),缺省状态，仅输出错误和警告信息，即输出警告级别以上的日志
        //LogManager.setLogger(Loggers.warningLogger());
        
        // 试验日志输出
//        LogManager.e(TAG,"Error");
//        LogManager.w(TAG,"Warn");
//        LogManager.i(TAG,"info");
//        LogManager.d(TAG,"debug");
//        LogManager.v(TAG,"verbose");

		LogManager.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// 日志文件
		start_logfile = (Button)findViewById(R.id.start_log);
		end_logfile = (Button)findViewById(R.id.end_log);
		
		// 设置SD卡中的日志文件,sd卡根目录/rssiRecord/mydistance.log
		//loghelper = LogcatHelper.getInstance(this,"rssiRecord","mydistance.log");
		// 设置SD卡中的日志文件,sd卡根目录/mydistance.log
		loghelper = LogcatHelper.getInstance(this,"","mydistance.log");
		
		// 打印D级以上(包括D,I,W,E,F)的TAG，其它tag不打印
		//Logformat = TAG + ":D *:S";
		
		// 打印D级以上的TAG，和LogcatHelper全部，其它tag不打印
		//Logformat = TAG + ":D LogcatHelper:V *:S";
		
		// 打印D以上的TAG和RunningAverageRssiFilter，其他tag不打印(*:S)
		Logformat = TAG + ":D RunningAverageRssiFilter:D *:S";
		
		// 打印D以上的FindBeacons，其他tag不打印(*:S)
		// Logformat = "FindBeacons:D *:S";
		
		//Logformat = "RangedBeacon:V *:S";
		
		// 打印所有日志， priority=V | D | I | W | E ,级别由低到高
		// Logformat = "";
		
		// 日志文件
		loghelper.start(Logformat);  
		
		// "开始记录日志"按钮失效,此时已经开始记录日志
		start_logfile.setEnabled(false);
		
		// 开始/停止监控（查找）beacons
		mStart_btn = (Button)findViewById(R.id.Mstart);
		mStop_btn = (Button)findViewById(R.id.Mstop);
		mStop_btn.setEnabled(false);
				
		// 获取FindBeacons唯一实例
		mFindBeacons = FindBeacons.getInstance(this);
                
    	// 设置默认前台扫描周期,default 1.1s
		ScanPeriod_edit = (EditText)findViewById(R.id.ScanPeriod_edit);
        ScanPeriod_edit.setText("1.1");
        onForegroundScanPeriod(null);
        
        // rssi采样周期,即，计算该时间段内的平均RSSI（首末各去掉10%）,缺省是20秒(20000毫秒)
        SamplePeriod_edit = (EditText)findViewById(R.id.SamplePeriod_edit);
        SamplePeriod_edit.setText("20");
        onSamplePeriod(null);
        
        // 设置获取附近所有beacons的监听对象，在每个扫描周期结束，通过该接口获取找到的所有beacons
        mFindBeacons.setBeaconsListener(mBeaconsListener);
             
    	// 查看手机蓝牙是否可用,若当前状态为不可用，则默认调用意图请求打开系统蓝牙
    	mFindBeacons.checkBLEEnable();

        logToDisplay("Mstart,Mstop分别代表查找beacon的开始和结束");
	}
	
    @Override 
    protected void onDestroy() {
    	LogManager.d(TAG,"onDestroy()");
        super.onDestroy();
        
        mFindBeacons.closeSearcher(); 
        loghelper.stop();
    }
    
    /** 
     * 连续两次按回退键，退出程序。
     * 使得下次程序执行从onCreate()开始，避免数据库初始化等问题的出现。
     */
    private long mPressedTime = 0;
    @Override
	public void onBackPressed() {
    	long mNowTime = System.currentTimeMillis();//获取第一次按键时间
    	if((mNowTime - mPressedTime) > 2000){//比较两次按键时间差
    	Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
    	mPressedTime = mNowTime;
    	}
    	else{//退出程序
    	   this.finish();
    	   System.exit(0);
    	}

		// super.onBackPressed();
	}
    
    /** 开始记录日志文件 */
    public void onStartLog(View view) {
    	loghelper.start(Logformat);  
    	start_logfile.setEnabled(false);
    	end_logfile.setEnabled(true);
    }
    
    /** 结束记录日志文件 */
    public void onEndLog(View view) {
    	loghelper.stop();
    	start_logfile.setEnabled(true);
    	end_logfile.setEnabled(false);
    }
    
    /** 删除日志文件文件 */
    public void onDelLog(View view) {
    	loghelper.delLogDir();
    }
       
    /** 开始查找附近beacons */
    public void onMonitoringStart(View view) {
    	logToDisplay("onMonitoringStart(),startMonitoringBeaconsInRegion");
    	LogManager.d(TAG,"onMonitoringStart(),startMonitoringBeaconsInRegion");
    	
    	mFindBeacons.openSearcher();
    	mStart_btn.setEnabled(false);
    	mStop_btn.setEnabled(true);
    }
    
    /** 停止查找beacons */
    public void onMonitoringStop(View view) {
    	logToDisplay("onMonitoringStop(),stopMonitoringBeaconsInRegion");
    	LogManager.d(TAG,"onMonitoringStop(),stopMonitoringBeaconsInRegion");
    	mFindBeacons.closeSearcher();
    	mStart_btn.setEnabled(true);
    	mStop_btn.setEnabled(false);
    }
    
    /** 设置前台扫描周期 */
    public void onForegroundScanPeriod(View view) {
    	String period_str = ScanPeriod_edit.getText().toString();
        long period = (long)(Double.parseDouble(period_str) * 1000.0D);
        mFindBeacons.setForegroundScanPeriod(period);   
    }
    
    /** 
     * 设置rssi采样周期,即，计算该时间段内的平均RSSI（首末各去掉10%）,缺省是20秒(20000毫秒)
     */
    public void onSamplePeriod(View view) {
    	String period_str = SamplePeriod_edit.getText().toString();
        long period = (long)(Double.parseDouble(period_str) * 1000.0D);
        FindBeacons.setSampleExpirationMilliseconds(period);   
    }
     
    public void logToDisplay(final String line) {
    	runOnUiThread(new Runnable() {
    		Date date = new Date(System.currentTimeMillis());
    		SimpleDateFormat sfd = new SimpleDateFormat("HH:mm:ss.SSS",Locale.CHINA);
	    	String dateStr = sfd.format(date);
    	    public void run() {
    	    	TextView editText = (TextView)rssiActivity.this.findViewById(R.id.monitoringText);
       	    	editText.append(dateStr+"=="+line+"\n");            	    	    		
    	    }
    	});
    }
    
}
