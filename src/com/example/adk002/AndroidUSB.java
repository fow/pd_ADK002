package com.example.adk002;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;


/*
arduino に送るコマンド

sendCommand(5byte)
0x00 停止
0x01 モーターABC制御　 morter_a_speed, morter_b_speed, morter_c_speed, delay
0x02　プログラム呼出       program_id

*/
public class AndroidUSB implements Runnable{

	public static final String TAG = "AndroidUSB";
    public static final String ACTION_USB_PERMISSION = "com.example.adk002.action.USB_PERMISSION";

    public PendingIntent mPermissionIntent;
    public boolean mPermissionRequestPending;

    public UsbManager mUsbManager;
    public UsbAccessory mAccessory;

    ParcelFileDescriptor mFileDescriptor;

    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    
    boolean f_registerReceiver = false;
    
    public boolean f_run = true;
    
    public boolean morter_lr = false;
    public boolean morter_reverse = false;

    String old_result = "";
    
    public void init(){
        //enableControls(false);
    }
    
    public void openAccessory(UsbAccessory accessory) {
        // アクセサリにアクセスするためのファイルディスクリプタを取得
        mFileDescriptor = mUsbManager.openAccessory(accessory);

        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();

            // 入出力用のストリームを確保
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);

            // この中でアクセサリとやりとりする
            Thread thread = new Thread(null, this, "DemoKit");
            thread.start();
            Log.d(TAG, "accessory opened");

            enableControls(true);
        } else {
            Log.d(TAG, "accessory open fail");
        }
    }

    public void closeAccessory() {
        enableControls(false);
        Log.d(TAG, "closeAccessory ");
        try {
        	f_run = false;
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
                Log.d(TAG, "closeAccessory mFileDescriptor.close");
            }
            mInputStream = null;
            mOutputStream = null;
            mUsbManager = null;
            mAccessory = null;
            
        } catch (IOException e) {
            Log.d(TAG, "closeAccessory IOException " + e.toString());
        } finally {
            Log.d(TAG, "closeAccessory finally ");
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    private void enableControls(boolean enable) {
        if (enable) {
        	ADK002.testSocket.showToast("USB connected", Toast.LENGTH_SHORT);
        }
    }

    // ここでアクセサリと通信する
    @Override
    public void run() {
        int ret = 0;
        byte[] buffer = new byte[16384];
        int i;

        // アクセサリ -> アプリ
        while (ret >= 0 && f_run) {
            try {
                ret = mInputStream.read(buffer);
            } catch (IOException e) {
                break;
            }

            i = 0;
            while (i < ret) {
                int len = ret - i;

                switch (buffer[i]) {

                    default:
                        //Log.d(TAG, "unknown msg: " + buffer[i]);
                        i = len;
                        break;
                }
            }

        }
    }
    
    public void sendCommand(byte command, int speed) {
    	int speed2 = speed - 20;
		if (speed2 < 0)
			speed2 = 0;
		
    	switch(command){
	 		case 0x01: morter_control(speed2, speed);         break;
	 		case 0x02: morter_control(speed, speed);          break;
	 		case 0x03: morter_control(speed, speed2);         break;
	 		case 0x04: morter_control(speed+100, speed);      break;
	 		case 0x05: stopMotorControl();                    break;
	 		case 0x06: morter_control(speed, speed+100);      break;
	 		case 0x07: morter_control(speed2+100, speed+100); break;
	 		case 0x08: morter_control(speed+100, speed+100);  break;
	 		case 0x09: morter_control(speed+100, speed2+100); break;
    	}
    }
    
    public void morter_control(int speed1, int speed2){    	
    	normal_control(0, speed1, speed2, 0);
    	/*
        ADK002.testSocket.showToast(
        		"control test 1." + (int)speed1 + "" + 
        		" 2." + (int)speed2 + ""  
                , Toast.LENGTH_SHORT);/**/
        
    }
    
    public void normal_control(int morter_a_speed, int morter_b_speed, int morter_c_speed, int delay){
    	if(morter_lr){
    		int tmp = morter_b_speed;
    		morter_b_speed = morter_c_speed;
    		morter_c_speed = tmp;
    	}
    	
    	if(morter_reverse){
    		if(morter_b_speed> 100){ morter_b_speed -= 100; }else if(morter_b_speed> 0){ morter_b_speed += 100;}
    		if(morter_c_speed> 100){ morter_c_speed -= 100; }else if(morter_c_speed> 0){ morter_c_speed += 100;}
    	}
    	
    	sendCommand((byte)0x01, (byte)morter_a_speed, (byte)morter_b_speed, (byte)morter_c_speed, (byte)delay);
    }

    public void sendCommand(byte command, byte value1, byte value2, byte value3, byte delay) {
        byte[] buffer = new byte[5];
        
        buffer[0] = command;
        buffer[1] = value1;
        buffer[2] = value2;
        buffer[3] = value3;
        buffer[4] = delay;
        /*
        ADK002.testSocket.showToast(
        		" c." + (int)command + "" + 
        		" 1." + (int)value1 + "" + 
        		" 2." + (int)value2 + "" + 
        		" 3." + (int)value3 + "" + 
        		" d." + (int)delay + ""
                , Toast.LENGTH_SHORT);/**/
        
        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }

	public void stopMotorControl() {
		sendCommand((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
	}
    
    public void WebControl(String tmp) {
    	
    	String pfx = tmp.substring(0, 1);
    	if(pfx.equals("c")){
    		if (tmp.length() >= 13) {
    			Log.i(TAG, " WebControl_normal " + tmp);
    	    	//ADK002.testSocket.showToast("9:" + tmp, Toast.LENGTH_SHORT);
    			WebControl_normal(tmp.substring(1, 13));
    		}
    	}else if(pfx.equals("p")){
    		if (tmp.length() >= 4) {
    			Log.i(TAG, " WebControl_pg " + tmp);
    			WebControl_pg(tmp.substring(1, 4));
    		}
    	}	
	}
    
    public void WebControl_normal(String now_result) {
    	//ADK002.testSocket.showToast(":" + now_result, Toast.LENGTH_SHORT);
    	//Log.i(TAG, " WebControl " + now_result);
    	
    	if(!old_result.equals(now_result)){
			old_result = now_result;
		}else{
			//return;
		}

    	try{
			String tmp2 = now_result;
			
			int morter_a_speed = Integer.parseInt(tmp2.substring(0, 3));
			int morter_b_speed = Integer.parseInt(tmp2.substring(3, 6));
			int morter_c_speed = Integer.parseInt(tmp2.substring(6, 9));
			int delay = Integer.parseInt(tmp2.substring(9, 12));
	
			if(morter_a_speed == 0 && morter_b_speed == 0 && morter_c_speed == 0){
				stopMotorControl();
			}else{
		    	normal_control(morter_a_speed, morter_b_speed, morter_c_speed, delay);
			}
		}catch(Exception e){
			Log.i(TAG, " WebControl_normal Exception " + e.toString());
		}
    }
    
    public void WebControl_pg(String now_result) {
		try {
			int x =  Integer.parseInt(now_result);
			sendCommand((byte)0x02, (byte)x, (byte)0x0, (byte)0x0, (byte)0x0);
		} catch (Exception e) {
			Log.i(TAG, " WebControl_pg Exception " + e.toString());
		}
	}
    
}
