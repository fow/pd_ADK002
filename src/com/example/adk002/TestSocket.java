package com.example.adk002;

import io.socket.SocketIO;
import io.socket.util.SocketIOManager;
import net.arnx.jsonic.JSONException;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class TestSocket {

	boolean f_connect = false;
	static final String TAG = "TestSocket";
	
	private SocketIOManager mSocketManager;
    private SocketIO mSocket;
	Toast reusableToast;
	
	String name = "";
	String pass = "";
	boolean f_showToast = true;
	
	void WebControl(String tmp){
		ADK002.androidUSB.WebControl(tmp);
	}
	

	public void showToast(String textToShow, int length) {
		reusableToast.setText(textToShow);
		reusableToast.setDuration(length);
		reusableToast.show();
	}
	
    public void connect(){
    	mSocketManager = new SocketIOManager(mHandler);
    }
    
    public void connectSocketIO(String ipAddress, String port) {
    	String add = "/room";
    	if(port.equals("")){
    		mSocket = mSocketManager.connect("http://" + ipAddress + "" + add);
    	}else{
    		mSocket = mSocketManager.connect("http://" + ipAddress + ":" + port + add);
    	}
    }

    public void disconnectSocketIO() {
    	emitData("robo_discon", "");
    	mSocketManager_disconnect();
    }
    
    public void mSocketManager_disconnect() {
    	if(mSocketManager != null){
    		mSocketManager.disconnect();
    	}
    }

    public void emitJoin(String name, String pass){
    	try {
    		JSONObject obj=new JSONObject();
    		obj.put("name", name);
    		obj.put("pass", pass);
    		mSocket.emit("robo_join", obj);
    	} catch (JSONException e) {
             e.printStackTrace();
    	} catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void emitMessage(String s){
    	try {
    		JSONObject obj=new JSONObject();
    		obj.put("v", s);
    		mSocket.emit("robo_msg", obj);
    	} catch (JSONException e) {
             e.printStackTrace();
    	} catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void emitData(String name, String data) {
        try {
            mSocket.emit(name, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void showToast(String s){    	
    	showToast(s, Toast.LENGTH_SHORT);
    }
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	JSONObject obj;
            switch (msg.what) {
                case SocketIOManager.SOCKETIO_DISCONNECT:
                    Log.i(TAG, "SOCKETIO_DISCONNECT");
                    f_connect = false;
                    //mCallback.onDisconnect();
                    break;
                case SocketIOManager.SOCKETIO_CONNECT:
                    Log.i(TAG, "SOCKETIO_CONNECT");
                    showToast("SOCKETIO_CONNECT" + msg.toString());
                    f_connect = true;
                    //mCallback.onConnect();                    
                    emitJoin(name, pass);
                    break;
                case SocketIOManager.SOCKETIO_HERTBEAT:
                    Log.i(TAG, "SOCKETIO_HERTBEAT");
                    showToast("SOCKETIO_HERTBEAT" + msg.toString());
                    //mCallback.onHertbeat();
                    break;
                case SocketIOManager.SOCKETIO_MESSAGE:
                    Log.i(TAG, "SOCKETIO_MESSAGE");
                    
                    try{
                    	String v = msg.obj.toString();
                    	
                    	if(f_showToast){
                            Log.i(TAG, "SOCKETIO_MESSAGE:" + v);
                            showToast("SOCKETIO_MESSAGE:" + v);
                        }
                    	if(v.length() >= 1){
                            String pfx = v.substring(0, 1);
                	    	if(pfx.equals("c")){
                	    		emitMessage("robo rcv:" + v);
                	    	}
                	    	if(pfx.equals("p")){
                	    		emitMessage("robo rcv:" + v);
                	    	}
                  			WebControl(v);
                    	}
                    }catch(Exception e){
                    	Log.i(TAG, "SOCKETIO_MESSAGE Exception " + e.toString());
                    }
                    
                    break;
                case SocketIOManager.SOCKETIO_JSON_MESSAGE:
                    Log.i(TAG, "SOCKETIO_JSON_MESSAGE");
                    showToast("SOCKETIO_JSON_MESSAGE" + msg.toString());
                    
                    break;
                    
                case SocketIOManager.SOCKETIO_JOIN_RESULT:
                    Log.i(TAG, "SOCKETIO_JOIN_RESULT");
                    
					try {
	                    obj = (JSONObject) (msg.obj);
						String v = obj.getString("v");
						Log.i(TAG, "MESSAGE>" + v);
						
						if(v.equals("joined")){
							//–¢Žg—p
							showToast("joined");
							f_connect = true;
						}else{
							if(v.equals("deny_pass")){
								showToast("error password");
								mSocketManager.disconnect();
							}else if(v.equals("no_exist")){
								showToast("error id no exists");
								mSocketManager.disconnect();
							}
							f_connect = false;
						}
						
					} catch (org.json.JSONException e) {
						e.printStackTrace();
					}
                    
                    break;
                        
                case SocketIOManager.SOCKETIO_EVENT:
                    Log.i(TAG, "SOCKETIO_EVENT");
                    showToast("SOCKETIO_EVENT" + msg.toString());
                    //mCallback.onEvent();
                    break;
                case SocketIOManager.SOCKETIO_ERROR:
                    Log.i(TAG, "SOCKETIO_ERROR");
                    showToast("SOCKETIO_ERROR" + msg.toString());
                    mSocketManager.disconnect();
                    //mCallback.onError();
                    break;
                case SocketIOManager.SOCKETIO_ACK:
                    Log.i(TAG, "SOCKETIO_ACK");
                    showToast("SOCKETIO_ACK" + msg.toString());
                    //mCallback.onAck();
                    break;
            }
        }
    };
    
}
