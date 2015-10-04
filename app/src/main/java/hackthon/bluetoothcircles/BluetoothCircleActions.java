package hackthon.bluetoothcircles;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by demo on 04-10-2015.
 */
public class BluetoothCircleActions {
    private static final String TAG = "BluetoothCircleActions";

    private static final String NAME_INSECURE = "BluetoothCircles";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("14fffd3e-5ec5-4a61-a056-aafb6f44d7cc");

    public BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private Set<BluetoothDevice> devicesArray;

    public ArrayList<String> pairedDevices = new ArrayList<String>();

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_DISCOVERY = 4;  // now connected to a remote device
    // Current Connection State
    private int mState;

    private AcceptThread listenThread = null;   // Listening Thread
    private DiscoveryThread discoveryThread = null; // Connection Thread

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
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

    public BluetoothCircleActions(Context context, Handler mHandler) {
        this.mHandler = mHandler;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        devicesArray = mAdapter.getBondedDevices();
        mState = STATE_NONE;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class DiscoveryThread extends Thread {
        // The local server socket
        private BluetoothSocket bsocket = null;

        public DiscoveryThread() {
            pairedDevices.clear();
        }

        public void run() {
            for(BluetoothDevice device:devicesArray) {
                if(mState != STATE_DISCOVERY)
                    return;
                try {
                    bsocket = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                    try {
                        bsocket.connect();
                        // Add device to the list
                        pairedDevices.add(device.getName());
                    } catch (IOException e) {
                        Log.d(TAG, "Cannot connect");
                    } finally {
                        // Close connection
                        try {
                            bsocket.close();
                            bsocket = null;
                        } catch (IOException e2) {
                            Log.d(TAG, "Cannot close");
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Cannot create console");
                }
            }

            synchronized (BluetoothCircleActions.this) {
                // Send a failure message back to the Activity
                Message msg = mHandler.obtainMessage(Constants.COMPLETE_DISCOVERY);
                mHandler.sendMessage(msg);
            }
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            //Toast.makeText(getApplicationContext(), "cancel " + this, Toast.LENGTH_SHORT).show();

            if(bsocket != null) {
                try {
                    bsocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "close() of server failed", e);
                }
            }
        }
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
                tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
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
                    Log.e(TAG, "accept() failed");
                    //Toast.makeText(getApplicationContext(), "accept() failed", Toast.LENGTH_SHORT).show();
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothCircleActions.this) {
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

    /**
     * Get paired connected devices
     */
    public synchronized void startDiscovery() {
        Log.d(TAG, "start discovery");

        if (listenThread != null) {
            listenThread.cancel();
            listenThread = null;
        }

        setState(STATE_DISCOVERY);

        if (discoveryThread == null) {
            discoveryThread = new DiscoveryThread();
            discoveryThread.start();
        }
    }

    /**
     * Start the AcceptThread to begin a session in listening mode.
     */
    public synchronized void startListening() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (discoveryThread != null) {
            discoveryThread.cancel();
            discoveryThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (listenThread == null) {
            listenThread = new AcceptThread();
            listenThread.start();
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stopEverything() {
        Log.d(TAG, "stop");

        if (discoveryThread != null) {
            discoveryThread.cancel();
            discoveryThread = null;
        }

        if (listenThread != null) {
            listenThread.cancel();
            listenThread = null;
        }

        setState(STATE_NONE);
    }
}
