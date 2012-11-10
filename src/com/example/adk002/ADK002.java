package com.example.adk002;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class ADK002 extends Activity {

    static final String TAG = "ADK002";
	public static TestSocket testSocket = new TestSocket();
	public static AndroidUSB androidUSB = new AndroidUSB();
	static final int REQUEST_OPTION = 1;
	static final String DEFAULT_IP = "49.212.197.45";
	
	String ip = "";
	String port = "";
	
	private TextView textView1;
	private TextView textView2;
    
    EditText editText1;
    EditText editText2;
    
	EditText EditText_id1;
	EditText EditText_id2;
	EditText EditText_id3;
	EditText EditText_pass1;
	EditText EditText_pass2;
	EditText EditText_pass3;

	EditText EditText_ip1;
	EditText EditText_ip2;
	EditText EditText_ip3;
	EditText EditText_port1;
	EditText EditText_port2;
	EditText EditText_port3;

	int _checkedId = 0;
	int _checkedIp = 0;
	
	CheckBox checkBox_morter_lr;
	CheckBox checkBox_morter_reverse;
	CheckBox checkBox_connect;
	CheckBox checkBox_display_log;
	
    int speed = 100;
    //boolean f_registerReceiver = false;
    
	public static final String PREFS_NAME = "ADK002_PrefsFile";
	
	private InputFilter[] filters1 = { new MyFilter(), new InputFilter.LengthFilter(12) };
	class MyFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if( source.toString().matches("^[-_@\\.a-zA-Z0-9]+$") ){
                return source;
            }else{
                return "";
            }
        }
	}
	
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
	    	Log.i(TAG, "mUsbReceiver onReceive");

            if (AndroidUSB.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    // Intent からアクセサリを取得
                    UsbAccessory accessory = UsbManager.getAccessory(intent);

                    // パーミッションがあるかチェック
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // 接続を開く
                    	androidUSB.openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory " + accessory);
                    }
                    androidUSB.mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                // Intent からアクセサリを取得
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                if (accessory != null && accessory.equals(androidUSB.mAccessory)) {
                    // 接続を閉じる
                	androidUSB.closeAccessory();
                }
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    	
        super.onCreate(savedInstanceState);
		testSocket.reusableToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        speed = settings.getInt("speed", 70);
		testSocket.f_showToast = settings.getBoolean("checkBox_display_log", false);
        androidUSB.morter_lr      = settings.getBoolean("checkBox_morter_lr", false);
        androidUSB.morter_reverse = settings.getBoolean("checkBox_morter_reverse", false);

        // UsbManager のインスタンスを取得
        androidUSB.mUsbManager = UsbManager.getInstance(this);

        // パーミッション用 Broadcast Intent
        androidUSB.mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(AndroidUSB.ACTION_USB_PERMISSION), 0);
        androidUSB.init();
        
        if(!androidUSB.f_registerReceiver){
	        IntentFilter filter = new IntentFilter(AndroidUSB.ACTION_USB_PERMISSION);
	        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
	        registerReceiver(mUsbReceiver, filter);
	        androidUSB.f_registerReceiver = true;
	        Log.d(TAG, "onCreate registerReceiver true");
        }else{       	
	        Log.d(TAG, "onCreate registerReceiver false");
        }

        setContentView(R.layout.activity_adk002);

        textView1 = (TextView) findViewById(R.id.TextView01);
        textView2 = (TextView) findViewById(R.id.TextView02);
        
        textView1.setText("");
        textView2.setText("");
        
		((SeekBar) findViewById(R.id.seekBar1)).setMax(100);
		((SeekBar) findViewById(R.id.seekBar1)).setProgress(speed);
		((SeekBar) findViewById(R.id.seekBar1))
			.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {	}

				public void onStartTrackingTouch(SeekBar seekBar) {	}

				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					speed = progress;
					textView1.setText("Morter:" + speed);
				}
			});

		EditText_id1 =   (EditText) findViewById(R.id.EditText_id1);
        EditText_id2 =   (EditText) findViewById(R.id.EditText_id2);
        EditText_id3 =   (EditText) findViewById(R.id.EditText_id3);
        EditText_pass1 = (EditText) findViewById(R.id.EditText_pass1);
        EditText_pass2 = (EditText) findViewById(R.id.EditText_pass2);
        EditText_pass3 = (EditText) findViewById(R.id.EditText_pass3);
        
        EditText_id1.setFilters(filters1);
        EditText_id2.setFilters(filters1);
        EditText_id3.setFilters(filters1);
        EditText_pass1.setFilters(filters1);
        EditText_pass2.setFilters(filters1);
        EditText_pass3.setFilters(filters1);

    	EditText_ip1 =   (EditText) findViewById(R.id.EditText_ip1);
    	EditText_ip2 =   (EditText) findViewById(R.id.EditText_ip2);
    	EditText_ip3 =   (EditText) findViewById(R.id.EditText_ip3);
    	EditText_port1 = (EditText) findViewById(R.id.EditText_port1);
    	EditText_port2 = (EditText) findViewById(R.id.EditText_port2);
    	EditText_port3 = (EditText) findViewById(R.id.EditText_port3);
    	
        EditText_id1.setText(settings.getString("id1", ""));
        EditText_id2.setText(settings.getString("id2", ""));
        EditText_id3.setText(settings.getString("id3", ""));
        EditText_pass1.setText(settings.getString("pass1", ""));
        EditText_pass2.setText(settings.getString("pass2", ""));
        EditText_pass3.setText(settings.getString("pass3", ""));

        EditText_ip1.setText(settings.getString("ip1", ADK002.DEFAULT_IP));
        EditText_ip2.setText(settings.getString("ip2", ""));
        EditText_ip3.setText(settings.getString("ip3", ""));
        EditText_port1.setText(settings.getString("port1", ""));
        EditText_port2.setText(settings.getString("port2", ""));
        EditText_port3.setText(settings.getString("port3", ""));
        
        _checkedId = settings.getInt("_checkedId", R.id.radio_id1);
        _checkedIp = settings.getInt("_checkedIp", R.id.radio_ip1);
        
        RadioGroup radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
        radioGroup1.check(_checkedId);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) { 
                _checkedId = checkedId;
                ChangeSetting();
            }
        });

        RadioGroup radioGroup2 = (RadioGroup) findViewById(R.id.radioGroup2);
        radioGroup2.check(_checkedIp);
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) { 
                _checkedIp = checkedId;
                ChangeSetting();
            }
        });
        
        checkBox_morter_lr = (CheckBox) findViewById(R.id.checkBox_morter_lr);
        checkBox_morter_lr.setChecked(settings.getBoolean("checkBox_morter_lr", false));
        checkBox_morter_reverse = (CheckBox) findViewById(R.id.checkBox_morter_reverse);
        checkBox_morter_reverse.setChecked(settings.getBoolean("checkBox_morter_reverse", false));

        checkBox_connect = (CheckBox) findViewById(R.id.checkBox_connect);
        checkBox_connect.setChecked(settings.getBoolean("checkBox_connect", false));
        checkBox_display_log = (CheckBox) findViewById(R.id.checkBox_display_log);
        checkBox_display_log.setChecked(settings.getBoolean("checkBox_display_log", false));
        
        checkBox_morter_lr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { androidUSB.morter_lr = ((CheckBox) v).isChecked(); }
        });
        
        checkBox_morter_reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { androidUSB.morter_reverse = ((CheckBox) v).isChecked(); }
        });

        checkBox_display_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { testSocket.f_showToast = ((CheckBox) v).isChecked(); }
        });

        textView1.setFocusable(true);
        textView1.setFocusableInTouchMode(true);
        textView1.requestFocus();/**/

        ChangeSetting();
        
        if(settings.getBoolean("checkBox_connect", false)){
        	socket_connect();
        }
    }
    
    void ViewIp(){
    	textView1.setText("id:" + testSocket.name + " ip:" + ip + ":" + port);
    }
    
    public void btn01(View view){ androidUSB.sendCommand((byte)0x01, speed); }
    public void btn02(View view){ androidUSB.sendCommand((byte)0x02, speed); }
    public void btn03(View view){ androidUSB.sendCommand((byte)0x03, speed); }
    public void btn04(View view){ androidUSB.sendCommand((byte)0x04, speed); }
    public void btn05(View view){ androidUSB.sendCommand((byte)0x05, speed); }
    public void btn06(View view){ androidUSB.sendCommand((byte)0x06, speed); }
    public void btn07(View view){ androidUSB.sendCommand((byte)0x07, speed); }
    public void btn08(View view){ androidUSB.sendCommand((byte)0x08, speed); }
    public void btn09(View view){ androidUSB.sendCommand((byte)0x09, speed); }
    
    public void btn10(View view){
    	ChangeSetting();
    	socket_connect();
    }
    
    void socket_connect(){
    	
    	if(testSocket.name.equals("")){
    		Toast.makeText(this, "please input robo id", Toast.LENGTH_SHORT).show();
    	}else{
	    	if(!testSocket.f_connect){
	    		
		    	Log.i(TAG, "btn connect");
		    	testSocket.connect();
		    	Log.i(TAG, "btn connectSocketIO");
		    	testSocket.connectSocketIO(ip, port);
		    	
				Toast.makeText(this, "connectSocketIO", Toast.LENGTH_SHORT).show();
	    	}else{
				Toast.makeText(this, "already connect", Toast.LENGTH_SHORT).show();
	    	}
    	}
    }

    public void btn11(View view){
    	if(testSocket.f_connect){
    		testSocket.emitMessage("test message android to server");
    	}
    }

    public void btn12(View view){
    	if(testSocket.f_connect){
	    	Log.i(TAG, "btn disconnectSocketIO");
	    	testSocket.disconnectSocketIO();
	    	
			Toast.makeText(this, "disconnectSocketIO", Toast.LENGTH_SHORT).show();
    	}else{
			Toast.makeText(this, "not connect", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void btn13(View view){
    	
		Toast.makeText(this, "quit", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "quit");

    	androidUSB.closeAccessory();
    	
    	if(androidUSB.f_registerReceiver){
    		try{
    			Log.d(TAG, "unregisterReceiver");
    			
    			unregisterReceiver(mUsbReceiver);
    			androidUSB.f_registerReceiver = false;
    		} catch (Exception e) {
    			Log.d(TAG, "unregisterReceiver err " + e.toString());
    		}
    	}
    	
    	if(testSocket.f_connect){
	    	testSocket.disconnectSocketIO();
    	}
    	testSocket.mSocketManager_disconnect();
    	
    	finish();
    }
        
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ");

        if(!androidUSB.f_registerReceiver){
            Log.d(TAG, "onResume registerReceiver ");
	        IntentFilter filter = new IntentFilter(AndroidUSB.ACTION_USB_PERMISSION);
	        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
	        registerReceiver(mUsbReceiver, filter);
	        androidUSB.f_registerReceiver = true;
        }
        
        if (androidUSB.mInputStream != null && androidUSB.mOutputStream != null) {
            return;
        }
        
        // USB Accessory の一覧を取得
        UsbAccessory[] accessories = androidUSB.mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            Log.d(TAG, "onResume accessory");
            // Accessory にアクセスする権限があるかチェック
            if (androidUSB.mUsbManager.hasPermission(accessory)) {
                // 接続を開く
                Log.d(TAG, "onResume openAccessory");
            	androidUSB.openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!androidUSB.mPermissionRequestPending) {
                        // パーミッションを依頼
                        Log.d(TAG, "onResume mPermissionRequestPending");
                    	androidUSB.mUsbManager.requestPermission(accessory, androidUSB.mPermissionIntent);
                    	androidUSB.mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }
    }

    @Override
    public void onPause() {
        //Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onPause");

        super.onPause();
        //androidUSB.closeAccessory();
    }

    @Override
    public void onDestroy() {
    	//Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy");
        
    	if(androidUSB.f_registerReceiver){
    		unregisterReceiver(mUsbReceiver);
    		androidUSB.f_registerReceiver = false;
    	}/**/
    	
        super.onDestroy();
    }
    @Override
    
    protected void onStop(){
		super.onStop();
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("speed", speed);

		editor.putString("id1", EditText_id1.getText().toString());
        editor.putString("id2", EditText_id2.getText().toString());
        editor.putString("id3", EditText_id3.getText().toString());
        editor.putString("pass1", EditText_pass1.getText().toString());
        editor.putString("pass2", EditText_pass2.getText().toString());
        editor.putString("pass3", EditText_pass3.getText().toString());

        editor.putString("ip1", EditText_ip1.getText().toString());
        editor.putString("ip2", EditText_ip2.getText().toString());
        editor.putString("ip3", EditText_ip3.getText().toString());
        editor.putString("port1", EditText_port1.getText().toString());
        editor.putString("port2", EditText_port2.getText().toString());
        editor.putString("port3", EditText_port3.getText().toString());

        editor.putInt("_checkedId", _checkedId);
        editor.putInt("_checkedIp", _checkedIp);

        editor.putBoolean("checkBox_morter_lr", checkBox_morter_lr.isChecked());
        editor.putBoolean("checkBox_morter_reverse", checkBox_morter_reverse.isChecked());
        editor.putBoolean("checkBox_connect", checkBox_connect.isChecked());
        editor.putBoolean("checkBox_display_log", checkBox_display_log.isChecked());
        
		editor.commit();
    }
    
    void ChangeSetting(){
    	String id,pass;
        id = pass = ip = port = "";
        switch(_checkedId){
        	case R.id.radio_id1: id   = EditText_id1.getText().toString();
        						 pass = EditText_pass1.getText().toString(); break;
        	case R.id.radio_id2: id   = EditText_id2.getText().toString();
        						 pass = EditText_pass2.getText().toString(); break;
        	case R.id.radio_id3: id   = EditText_id3.getText().toString();
        						 pass = EditText_pass3.getText().toString(); break;
        	default:
        						 id   = "";
        						 pass = ""; break;
        }
        testSocket.name = id;
        testSocket.pass = pass;
        
        switch(_checkedIp){
	    	case R.id.radio_ip1: ip   = EditText_ip1.getText().toString();
	    						 port = EditText_port1.getText().toString(); break;
	    	case R.id.radio_ip2: ip   = EditText_ip2.getText().toString();
	    						 port = EditText_port2.getText().toString(); break;
	    	case R.id.radio_ip3: ip   = EditText_ip3.getText().toString();
	    						 port = EditText_port3.getText().toString(); break;
        	default:
        						 ip   = DEFAULT_IP;
        						 port = ""; break;
	    }
        ViewIp();
    }
}