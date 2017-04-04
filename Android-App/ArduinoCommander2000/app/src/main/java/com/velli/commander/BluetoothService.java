/*
 * MIT License
 *
 * Copyright (c) [2017] [velli20]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.velli.commander;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.velli.commander.collections.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class BluetoothService {
    private static final String TAG = "BluetoothService ";

    private BluetoothAdapter mBtAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private Handler mHandler;
    private int mState;
    private static BluetoothService sInstance;
    private ArrayList<Command> mCommandsList = new ArrayList<>();

    public static BluetoothService getInstance() {
        if (sInstance == null) sInstance = getSync();
        return sInstance;
    }

    private static synchronized BluetoothService getSync() {
        if (sInstance == null) sInstance = new BluetoothService();
        return sInstance;
    }

    public BluetoothService() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = Constants.STATE_NONE;
    }

    private synchronized void setState(int state) {
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        if (mHandler != null) {
            mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

    public synchronized int getState() {
        return mState;
    }

    public ArrayList<Command> getCommandsList() {
        return mCommandsList;
    }

    public void clearCommandsList() {
        mCommandsList.clear();
    }

    public synchronized void connect(BluetoothDevice device) {

        // Start the thread to connect with the given device
        // Cancel any thread attempting to make a connection
        if (mState == Constants.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(Constants.STATE_CONNECTING);

    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void disconnect() {
        if (App.DEBUG) {
            Log.i(TAG, TAG + "disconnect()");
        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(Constants.STATE_NONE);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel the ConnectedThread to make sure that it's not running currently connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the ConnectedThread to manage connection and perform transmission
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Set state of connection to STATE_CONNECTED
        setState(Constants.STATE_CONNECTED);
    }






    public void sendCommand(Command command) {
        if(command == null || command.getCommand() == null) {
            return;
        }
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != Constants.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        mCommandsList.add(command);

        if (mHandler != null) {
            mHandler.obtainMessage(Constants.MESSAGE_SENT, command);
        }

        r.write(command.getCommand().getBytes());
    }

    public void receiveCommand(Command command) {
        mCommandsList.add(command);

        if (mHandler != null) {
            mHandler.obtainMessage(Constants.MESSAGE_READ, command);
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        ConnectThread(BluetoothDevice device) {
            mDevice = device;

            BluetoothSocket tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(Constants.MY_UUID);
            } catch (IOException ignored) {
            }
            mSocket = tmp;
        }


        public void run() {
            // Get local bluetooth adapter
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            // Cancel discovery because it will slow down a connection
            mBtAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // Connect the device through the socket. This is blocking call so it
                // will return on a successful connection or an exception
                mSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mSocket.close();
                    setState(Constants.STATE_CONNECTION_FAILED);
                } catch (IOException ignored) {}
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            // Do work to manage the connection (in a separate thread)
            connected(mSocket, mDevice);
        }



        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            if (App.DEBUG) {
                Log.i(TAG, TAG + "ConnectThread cancel()");
            }
            try {
                mSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) {
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;


        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true) {

                // Read from the InputStream
                try {
                    if(bytes < 1024) {
                        buffer[bytes] = (byte) mInStream.read();
                        bytes++;
                    }
                } catch (IOException ignored) {}
                // Send the obtained bytes to the UI Activity
                if (bytes > 1023 || (buffer[bytes -1] == '\n')||(buffer[bytes -1]=='\r')) {
                    try {
                        String command = new String(buffer, "UTF-8");
                        receiveCommand(new Command(command, System.currentTimeMillis(), true));
                        buffer = new byte[1024];
                        bytes=0;
                    } catch (UnsupportedEncodingException ignored) {}

                }
            }

        }
        // Call this from the MainActivity to send data to the remote device
        void write(byte[] out) {

            if (mState == Constants.STATE_CONNECTED && out != null) {
                try {
                    mOutStream.write(out);
                } catch (IOException ignored) {}
            }
        }

        void cancel() {
            if (App.DEBUG) {
                Log.i(TAG, TAG + "ConnectedThread cancel()");
            }
            try {
                mSocket.close();
                setState(Constants.STATE_NONE);

            } catch (IOException ignored) {}
            try {
                mInStream.available();
                mInStream.close();
            } catch (IOException ignored) {}
            try {
                if (mOutStream != null) {
                    mOutStream.close();
                }
            } catch (IOException ignored) {}

        }
    }
}

	





	





	






	






	


