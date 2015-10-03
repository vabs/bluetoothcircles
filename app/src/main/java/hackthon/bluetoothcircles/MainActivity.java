package hackthon.bluetoothcircles;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    ArrayAdapter<String> listAdapter;
    Button connectNew;
    ListView listView;

    BluetoothAdapter btAdapater;
    Set<BluetoothDevice> devicesArray;
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;

    ArrayList<String> pairedDevices;


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

            getPairedDevices();
            startDiscovery();
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
        listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        listView.setAdapter(listAdapter);
        btAdapater = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND == action) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    //if (device.getBondState() != BluetoothDevice.BOND_BONDED){
                    if(device.getName() != null) {
                        for (int i = 0; i < listAdapter.getCount(); i++) {
                            if (!listAdapter.getItem(i).contains(device.getName())) {
                                listAdapter.add(device.getName() + "\n" + device.getAddress());
                            }
                        }
                    }
                    //}

                } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.d(TAG, "Discovery finished: ");

                } else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if(btAdapater.getState() == btAdapater.STATE_TURNING_OFF) {
                        turnOnBT();
                    }
                }
            }
        };

        registerReceiver(broadcastReceiver, intentFilter);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(broadcastReceiver, filter);

         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);
         filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);
    }

    private void getPairedDevices() {
        devicesArray = btAdapater.getBondedDevices();
        if(devicesArray.size() > 0) {
            for(BluetoothDevice device:devicesArray) {
                pairedDevices.add(device.getName());
            }
        }
        listAdapter.addAll(pairedDevices);
    }


    @Override
    protected void onPause() {
        super.onPause();
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
}
