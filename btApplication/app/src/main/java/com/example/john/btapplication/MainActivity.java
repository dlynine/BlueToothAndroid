package com.example.john.btapplication;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.content.BroadcastReceiver;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Set;
import android.os.AsyncTask;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    //这条是蓝牙串口通用的UUID，不要更改
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String deviceMac;

    //接收到的字符串
    String ReceiveData = "";

    EditText myview;
    private int mState;
    // Debugging
    private static final String TAG = "BTApp";
    private static final boolean D = true;
 //   private final Handler mHandler;
    boolean connected = false;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming
    // connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote
    public static final int STATE_SEND = 4; // now sending message
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        myview = (EditText) this.findViewById(R.id.editText);
        //    myview.setInputType(InputType.TYPE_NULL);
        button.setOnClickListener(this);
    //    handler = new MyHandler();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //// Device does not support Bluetooth
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                bluetoothTurnon(v);
                break;
            case R.id.button2:
                startReading();
                break;
            default:
                break;
        }
    }
    public void bluetoothTurnon(View v)
    {
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
            Toast.makeText(getApplicationContext(),"打开蓝牙"
                    ,Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"蓝牙已开启",
                    Toast.LENGTH_LONG).show();
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {

                if(device.getName().contains("hc01.com HC-05"))
                {
                    deviceMac = device.getAddress().toString();
                    mDevice = device;
                    Toast.makeText(getApplicationContext(),deviceMac,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"蓝牙配对数为0" ,
                    Toast.LENGTH_LONG).show();
        }
        if (!deviceMac.isEmpty())
        {
            connectSocket();
            try {
                Thread.currentThread().sleep(9000);//阻断9秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startReading();
        }
    }
    public void readData(View v)
    {
        startReading();
    }
    public void connectSocket(){
        mBluetoothAdapter.cancelDiscovery();
        try {
            btSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            System.out.println("connectSocket error!");
        }
        new ConnectBluetoothTask().execute(btSocket);
    }

    public void startReading(){
        new ReadInputTask().execute(inStream);
    }

    private class ReadInputTask extends AsyncTask<InputStream, Integer, Void>{
        @Override
        protected Void doInBackground(InputStream... params) {
            InputStream inputStream = params[0];
      //      Log.d("e","ReadInputTask doInBackground");
            byte[] mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                int byteCount = 0;
                try {
                    byteCount = inputStream.available();
               //     Log.d("e",String.valueOf(byteCount));
                    if(byteCount > 0)
                    {
                        byte[] rawBytes = new byte[byteCount];
                        inputStream.read(rawBytes);
                        final String string=new String(rawBytes,"UTF-8");
                        //TO DO
                 //       Toast.makeText(context,string,Toast.LENGTH_SHORT).show();
                        Log.d("e",String.valueOf(string.length()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Void ds) {
       //     Toast.makeText(context,"The reading task has finished",Toast.LENGTH_SHORT).show();
        }
    }
    private class ConnectBluetoothTask extends AsyncTask<BluetoothSocket, Integer, InputStream>{

        @Override
        protected InputStream doInBackground(BluetoothSocket... params) {
            BluetoothSocket bluetoothSocket = params[0];
            try {
                bluetoothSocket.connect();
                //get inputStream
                inStream=bluetoothSocket.getInputStream();
                Log.e("e","connected");
                return inStream;
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("CEVAAAAA");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //TO DO here
            //setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            inStream = inputStream;
            connected = inStream != null;
        }
    }
}
