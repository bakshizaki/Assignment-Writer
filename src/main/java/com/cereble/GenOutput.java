package com.cereble;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.cereble.SingleListDialog.GetResultDialogListner;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GenOutput extends Activity implements OnClickListener,GetResultDialogListner {

	private static final String TAG = "BT_Test";
	Button bConnect,bSendData,bListDevices;
	BluetoothAdapter BA;
	private DialogFragment singlechoice;
	ProgressDialog prog;
	TextView status,received_data;
	int selected_item;
	ArrayList list;
	ArrayList mList = new ArrayList();
	ArrayList paired_addresses = new ArrayList();
	ArrayList discovered_addresses = new ArrayList();
	int readBufferPosition = 0;
	byte[] readBuffer =new byte[1024];;
	EditText textToSend;
	private ConnectedThread mConnectedThread;
	private BluetoothSocket btSocket = null;
	final Handler handler = new Handler();
	boolean is_device_selected = false;
	int gpsCounter = 0;
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private String address;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gen_output);
		init_elements();
		BA=BluetoothAdapter.getDefaultAdapter();
		StartBluetooth();
	}

	private void StartBluetooth() {
		// TODO Auto-generated method stub
		if(!BA.isEnabled())
		{
			Intent turnon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnon, 1);
			Toast.makeText(getApplicationContext(),"Turned On",Toast.LENGTH_LONG).show();
		}
		else
			Toast.makeText(getApplicationContext(),"Already On",Toast.LENGTH_LONG).show();
	}

	public void ConnectToDevice()
	{
    	BluetoothDevice device = BA.getRemoteDevice(address);
    	try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "In onResume, socket creation failed", Toast.LENGTH_LONG).show();
        }
    	BA.cancelDiscovery();
    	Log.d(TAG, "...Connecting...");
    	status.setText("...Connecting...");
        try {
          btSocket.connect();
          Log.d(TAG, "....Connection ok...");
          status.setText("....Connection ok...");
        } catch (IOException e) {
          try {
            btSocket.close();
          } catch (IOException e2) {
            Toast.makeText(getApplicationContext(), "Unable to close socket during socket failure", Toast.LENGTH_SHORT).show();
          }
        }
          
        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");
        status.setText("...Create Socket...");
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
		
	}
	
	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
	      if(Build.VERSION.SDK_INT >= 10){
	          try {
	              final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
	              return (BluetoothSocket) m.invoke(device, MY_UUID);
	          } catch (Exception e) {
	              Log.e("MainActivity.java", "Could not create Insecure RFComm Connection",e);
	          }
	      }
	      return  device.createRfcommSocketToServiceRecord(MY_UUID);
	  }
	
	private void init_elements() {
		// TODO Auto-generated method stub
		bConnect = (Button) findViewById(R.id.bConnect);
		bSendData = (Button) findViewById(R.id.bSend);
		bListDevices = (Button) findViewById(R.id.bListDevices);
		status = (TextView) findViewById(R.id.tvStatus);
		received_data=(TextView) findViewById(R.id.tvReceived);
		textToSend=(EditText) findViewById(R.id.etTextToSend);
		received_data.setMovementMethod(new ScrollingMovementMethod());
		bConnect.setOnClickListener(this);
		bSendData.setOnClickListener(this);
		bListDevices.setOnClickListener(this);
		
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try {
			unregisterReceiver(mReceiver);
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			}
		super.onDestroy();
		
	}
	
	 @Override
	  public void onResume() {
	    super.onResume();
	    if (is_device_selected)
	    {
	    	ConnectToDevice();
	    }
	 }


	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
	        	prog= ProgressDialog.show(GenOutput.this, "Scanning...", "Please Wait");
	        }
	        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        	prog.dismiss();
	        	singlechoice = new SingleListDialog("Paired Devices",(String []) mList.toArray(new String[mList.size()]));
				singlechoice.show(getFragmentManager(), "FoundDevices");
	        }
	        else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	        	Toast.makeText(getApplicationContext(),"Found something",Toast.LENGTH_LONG).show();
	            BluetoothDevice device =(BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	            mList.add(device.getName());
	            discovered_addresses.add(device.getAddress());
	            //final ArrayAdapter adapter_D = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,mList);
				//lv.setAdapter(adapter_D);
	        }
	    }
	};
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		
		case R.id.bListDevices:
			

			
			Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
			list = new ArrayList();
			for (BluetoothDevice bt : pairedDevices)
			{
				list.add(bt.getName());
				paired_addresses.add(bt.getAddress());
			}
			Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_LONG).show();
			//final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
			//lv.setAdapter(adapter);
			
			singlechoice = new SingleListDialog("Paired Devices",(String []) list.toArray(new String[list.size()]));
			singlechoice.show(getFragmentManager(), "Iamhopeless");
			
			break;
			
		case R.id.bSend:
			String msg = textToSend.getText().toString();
			msg+="\n";
			mConnectedThread.write(msg);
			break;
		}
	}

	@Override
	public void onDialogFinish(int selected) {
		// TODO Auto-generated method stub
		selected_item=selected;
		//status.setText(Integer.toString(selected_item));
		status.setText(list.get(selected_item).toString()+" "+paired_addresses.get(selected_item).toString());
		address = paired_addresses.get(selected_item).toString();
		ConnectToDevice();
		is_device_selected=true;
	}

	 private class ConnectedThread extends Thread {
	        private final InputStream mmInStream;
	        private final OutputStream mmOutStream;
	      
	        public ConnectedThread(BluetoothSocket socket) {
	            InputStream tmpIn = null;
	            OutputStream tmpOut = null;
	      
	            // Get the input and output streams, using temp objects because
	            // member streams are final
	            try {
	                tmpIn = socket.getInputStream();
	                tmpOut = socket.getOutputStream();
	            } catch (IOException e) { }
	      
	            mmInStream = tmpIn;
	            mmOutStream = tmpOut;
	        }
	      
	        public void run() {
	            byte[] buffer = new byte[256];  // buffer store for the stream
	            int bytes; // bytes returned from read()
	 
	            // Keep listening to the InputStream until an exception occurs
	            while (true) {
	                try {
	                    // Read from the InputStream
	                	int bytesAvailable = mmInStream.available();
	                	if(bytesAvailable > 0)
	                	{
	                		 byte[] packetBytes = new byte[bytesAvailable];
	                         mmInStream.read(packetBytes);
	                         for(int i=0;i<bytesAvailable;i++)
	                         {
	                        	 byte b = packetBytes[i];
	                             if(b == 10)
	                             {
	                            	 byte[] encodedBytes = new byte[readBufferPosition];
	                            	 System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
	                            	 final String data = new String(encodedBytes, "US-ASCII");
	                            	 readBufferPosition = 0;

	                                 handler.post(new Runnable()
	                                 {
	                                     public void run()
	                                     {
	                                    	 if(data.contains("Time"))
	                                    	 {
	                                    		 received_data.setText("");
	                                    		 gpsCounter=0;
	                                    	 }
	                                    	 status.setText(data);
	                                    	 
	                                         received_data.append(data);
	                                         received_data.append("\n");
	                                         gpsCounter++;
	                                    	 
	                                     }
	                                 });
	                             }
	                             else
	                             {
	                                 readBuffer[readBufferPosition++] = b;
	                             }
	                         
	                         }
	                	}
	                    //bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
	                    //h.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
	                } catch (IOException e) {
	                    break;
	                }
	            }
	        }
	      
	        /* Call this from the main activity to send data to the remote device */
	        public void write(String message) {
	            Log.d(TAG, "...Data to send: " + message + "...");
	            byte[] msgBuffer = message.getBytes();
	            try {
	                mmOutStream.write(msgBuffer);
	            } catch (IOException e) {
	                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");     
	              }
	        }
	 }
}
