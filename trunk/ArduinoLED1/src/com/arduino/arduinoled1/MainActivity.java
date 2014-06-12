package com.arduino.arduinoled1;
 
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
 
public class MainActivity extends Activity {
  private static final String TAG = "Abdel-Domotic";
   
  Button ledOn, ledOff;
  Button gateOn, gateOff;
  Button curtOn, curtOff;
  Button getTemp;
  
  TextView temp;
   
  private static final int REQUEST_ENABLE_BT = 1;
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private OutputStream outStream = null;
   
  // Well known SPP UUID
  private static final UUID MY_UUID =
      UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
  // Insert your server's MAC address
  private static String address = "20:13:12:05:10:24";
   
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
 
    Log.d(TAG, "In onCreate()");
 
    setContentView(R.layout.activity_main);
 
    ledOn = (Button) findViewById(R.id.ledOn);
    ledOff = (Button) findViewById(R.id.ledOff);
    gateOn = (Button) findViewById(R.id.gateOn);
    gateOff = (Button) findViewById(R.id.gateOff);
    curtOn = (Button) findViewById(R.id.curtOn);
    curtOff = (Button) findViewById(R.id.curtOff);
     
    btAdapter = BluetoothAdapter.getDefaultAdapter();
    checkBTState();
 
    ledOn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        sendData("1");
        Toast msg = Toast.makeText(getBaseContext(),
            "You have clicked LED On", Toast.LENGTH_SHORT);
        msg.show();
      }
    });
 
    ledOff.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        sendData("0");
        Toast msg = Toast.makeText(getBaseContext(),
            "You have clicked LED Off", Toast.LENGTH_SHORT);
        msg.show();
      }
    });
    
    gateOn.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          sendData("3");
          Toast msg = Toast.makeText(getBaseContext(),
              "You have clicked Gate On", Toast.LENGTH_SHORT);
          msg.show();
        }
      });
   
      gateOff.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          sendData("2");
          Toast msg = Toast.makeText(getBaseContext(),
              "You have clicked Gate Off", Toast.LENGTH_SHORT);
          msg.show();
        }
      });
      
      curtOn.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            sendData("5");
            Toast msg = Toast.makeText(getBaseContext(),
                "You have clicked Curton On", Toast.LENGTH_SHORT);
            msg.show();
          }
        });
     
        curtOff.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            sendData("4");
            Toast msg = Toast.makeText(getBaseContext(),
                "You have clicked Curton Off", Toast.LENGTH_SHORT);
            msg.show();
          }
        });
    
  }
   
  @Override
  public void onResume() {
    super.onResume();
 
    Log.d(TAG, "...In onResume - Attempting client connect...");
   
    // Set up a pointer to the remote node using it's address.
    BluetoothDevice device = btAdapter.getRemoteDevice(address);
   
    // Two things are needed to make a connection:
    //   A MAC address, which we got above.
    //   A Service ID or UUID.  In this case we are using the
    //     UUID for SPP.
    try {
      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
    }
   
    // Discovery is resource intensive.  Make sure it isn't going on
    // when you attempt to connect and pass your message.
    btAdapter.cancelDiscovery();
   
    // Establish the connection.  This will block until it connects.
    Log.d(TAG, "...Connecting to Remote...");
    try {
      btSocket.connect();
      Log.d(TAG, "...Connection established and data link opened...");
    } catch (IOException e) {
      try {
        btSocket.close();
      } catch (IOException e2) {
        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
      }
    }
     
    // Create a data stream so we can talk to server.
    Log.d(TAG, "...Creating Socket...");
 
    try {
      outStream = btSocket.getOutputStream();
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
    }
  }
 
  @Override
  public void onPause() {
    super.onPause();
 
    Log.d(TAG, "...In onPause()...");
 
    if (outStream != null) {
      try {
        outStream.flush();
      } catch (IOException e) {
        errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
      }
    }
 
    try     {
      btSocket.close();
    } catch (IOException e2) {
      errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
    }
  }
   
  private void checkBTState() {
    // Check for Bluetooth support and then check to make sure it is turned on
 
    // Emulator doesn't support Bluetooth and will return null
    if(btAdapter==null) { 
      errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
    } else {
      if (btAdapter.isEnabled()) {
        Log.d(TAG, "...Bluetooth is enabled...");
      } else {
        //Prompt user to turn on Bluetooth
        Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }
    }
  }
 
  private void errorExit(String title, String message){
    Toast msg = Toast.makeText(getBaseContext(),
        title + " - " + message, Toast.LENGTH_SHORT);
    msg.show();
    finish();
  }
 
  private void sendData(String message) {
    byte[] msgBuffer = message.getBytes();
 
    Log.d(TAG, "...Sending data: " + message + "...");
 
    try {
      outStream.write(msgBuffer);
    } catch (IOException e) {
      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
      if (address.equals("00:00:00:00:00:00")) 
        msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
      msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
       
      errorExit("Fatal Error", msg);       
    }
  }
}