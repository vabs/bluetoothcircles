package hackthon.bluetoothcircles;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private static final String NAME_INSECURE = "BluetoothCircles";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("14fffd3e-5ec5-4a61-a056-aafb6f44d7cc");

    ArrayAdapter<String> listAdapter;
    Button connectNew;
    ListView listView;
    ProgressBar progressBar;

    BluetoothAdapter btAdapater;
    Set<BluetoothDevice> devicesArray;
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;

    ArrayList<String> pairedDevices;


    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    AcceptThread listenThread = null;

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        Log.d(TAG, "connected");

        setState(STATE_CONNECTED);
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        if (btAdapater == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!btAdapater.isEnabled()) {
                turnOnBT();
            }

            startListening();
            //getPairedDevices();
            //startDiscovery();
        }
    }

    private void startDiscovery() {
        btAdapater.cancelDiscovery();
        btAdapater.startDiscovery();
    }

    private void turnOnBT() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    public void init(){
        connectNew = (Button)findViewById(R.id.button);
        listView = (ListView)findViewById(R.id.listView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        listView.setAdapter(listAdapter);
        btAdapater = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        pairedDevices = new ArrayList<String>();

        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        broadcastReceiver = new BroadcastReceiver() {
            private String getName(BluetoothDevice d) {
                String s = d.getName();

                if(s == null) {
                    s = "Unknown Device";
                }

                return s;
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND == action) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    listAdapter.add(this.getName(device));
                    /*if(pairedDevices.contains(device.getAddress())) {
                        listAdapter.add(this.getName(device));
                    }*/

                } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.d(TAG, "Discovery finished: ");
                    Toast.makeText(getApplicationContext(), "Discovery finished", Toast.LENGTH_SHORT).show();
                } else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if(btAdapater.getState() == btAdapater.STATE_TURNING_OFF) {
                        turnOnBT();
                    }
                } //else if(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            }
        };

        registerReceiver(broadcastReceiver, intentFilter);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(broadcastReceiver, filter);

         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);
         filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);

         filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(broadcastReceiver, filter);

         //filter = new IntentFilter();
    }

    private void getPairedDevices() {
        stopListening();
        //progressBar.setVisibility(View.VISIBLE);
        devicesArray = btAdapater.getBondedDevices();
        if(devicesArray.size() > 0) {
            for(BluetoothDevice device:devicesArray) {
                BluetoothSocket tmp = null;

                Toast.makeText(getApplicationContext(), "Trying to connect " + device.getName(), Toast.LENGTH_LONG).show();
                try {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                    try {
                        tmp.connect();
                        // Add device to the list
                        pairedDevices.add(device.getName());
                    } catch (IOException e) {

                        Log.d(TAG, "Cannot cconnect");
                        try {
                            tmp.close();
                        } catch (IOException e2) {
                            Log.d(TAG, "Cannot close");
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Cannot create console");
                }
            }
        }
        //progressBar.setVisibility(View.INVISIBLE);
        listAdapter.addAll(pairedDevices);
        Toast.makeText(getApplicationContext(), "Discovery finished.", Toast.LENGTH_SHORT).show();
        startListening();
    }

    private void startListening() {
        if (listenThread == null) {
            listenThread = new AcceptThread();
            listenThread.start();
        }
    }

    private void stopListening() {
        if (listenThread != null) {
            listenThread.cancel();
            listenThread = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        btAdapater.cancelDiscovery();
        if(listAdapter.getItem(arg2).contains("Paired")) {
            Toast.makeText(getApplicationContext(), "device is paired", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickBtnScan(View view) {
        getPairedDevices();
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = btAdapater.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
                //Toast.makeText(getApplicationContext(), "listen() failed", Toast.LENGTH_SHORT).show();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    //Toast.makeText(getApplicationContext(), "accept() failed", Toast.LENGTH_SHORT).show();
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (MainActivity.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    //Toast.makeText(getApplicationContext(), "Could not close unwanted socket", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread");
            //Toast.makeText(getApplicationContext(), "END mAcceptThread", Toast.LENGTH_SHORT).show();
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            //Toast.makeText(getApplicationContext(), "cancel " + this, Toast.LENGTH_SHORT).show();

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }
}


