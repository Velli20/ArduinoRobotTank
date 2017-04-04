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


import java.lang.ref.WeakReference;
import java.util.ArrayList;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.velli.commander.collections.Command;
import com.velli.commander.interfaces.BluetoothCommandCallback;
import com.velli.commander.interfaces.OnBtServiceStateChangedListener;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	public static final int REQUEST_CODE_SCAN_FOR_DEVICES = 0;


    private Handler mHandler;

    private static final String TAG = "MainActivity ";

	private String mBtDeviceName = "";
	private String mBtDeviceAddress = "";

	private MaterialDialog mDialog;

    private ArrayList<OnBtServiceStateChangedListener> mBtStateChangedCallbacks = new ArrayList<>();
    private ArrayList<BluetoothCommandCallback> mBluetoothCommandCallbacks = new ArrayList<>();
    //
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

		final NavigationView navigationView = ((NavigationView) findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_hand_control).setChecked(true);

	    mHandler = new BluetoothServiceHandler(this);
        BluetoothService.getInstance().setHandler(mHandler);

        getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_content, new FragmentHandControls()).commit();

	}
	
	@Override
	public void onPause(){
		super.onPause();

	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(BluetoothService.getInstance().getState() == Constants.STATE_CONNECTED){
            stopAndDisconnect();
        }
        mBluetoothCommandCallbacks = null;
        mBtStateChangedCallbacks = null;
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.menu_search_devices).setTitle(BluetoothService.getInstance().getState() == Constants.STATE_CONNECTED ?
                R.string.menu_bluetooth_disconnect : R.string.menu_bluetooth_connect);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_search_devices:

                if(BluetoothService.getInstance().getState() != Constants.STATE_CONNECTED) {
                    Intent i = new Intent(this, ActivitySearchDevices.class);
                    startActivityForResult(i, REQUEST_CODE_SCAN_FOR_DEVICES);
                } else if(BluetoothService.getInstance().getState() == Constants.STATE_CONNECTED){
                    stopAndDisconnect();
                }
                return true;
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);

        Fragment fragment;

        switch(item.getItemId()) {
            case R.id.nav_routes:
                fragment = new FragmentPrograms();
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_content, fragment).commit();
                return true;
            case R.id.nav_console:
                fragment = new FragmentConsole();
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_content, fragment).commit();
                return true;
            case R.id.nav_hand_control:
                fragment = new FragmentHandControls();
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_content, fragment).commit();
                return true;
            case R.id.nav_settings:
                Intent settings = new Intent(this, ActivitySettings.class);
                startActivity(settings);
                return true;

            default:
            case R.id.nav_accelerometer:
                fragment = new FragmentAccelerometer();
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_content, fragment).commit();
                return true;
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_CODE_SCAN_FOR_DEVICES:
				if(data != null) {
					connectToDevice(data.getStringExtra(ActivitySearchDevices.INTENT_EXTRA_DEVICE_NAME),
							data.getStringExtra(ActivitySearchDevices.INTENT_EXTRA_DEVICE_ADDRESS));
				}
				break;
		}
	}

	private void connectToDevice(String deviceName, String deviceAdress) {
		mBtDeviceName = deviceName;
		mBtDeviceAddress = deviceAdress;

		hideDialog();
		BluetoothService.getInstance().connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAdress));
		mDialog = new MaterialDialog.Builder(this)
				.content(getString(R.string.action_connecting_to_device) + " " + deviceName)
				.cancelable(false)
				.progress(true, 0)
				.show();
	}

	private void showConnectionFailed() {
		hideDialog();

		mDialog = new MaterialDialog.Builder(this)
				.content(getString(R.string.action_connecting_to_device_failed))
				.positiveText(R.string.action_retry)
				.negativeText(R.string.action_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        connectToDevice(mBtDeviceName, mBtDeviceAddress);
                    }
                })
				.show();
	}

	private void showConnectedToDevice() {
		hideDialog();
		Snackbar.make(findViewById(android.R.id.content),
				getText(R.string.action_connected_to_device) + " " + mBtDeviceAddress,
				Snackbar.LENGTH_LONG).show();
	}

	private void hideDialog() {
		if(mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		mDialog = null;
	}


	public void stopAndDisconnect(){
        if (App.DEBUG) {
            Log.i(TAG, TAG + "stopAndDisconnect()");
        }
        if(BluetoothService.getInstance().getState() == Constants.STATE_CONNECTED){
            BluetoothService.getInstance().sendCommand(new Command("STOP", System.currentTimeMillis(), false));
			BluetoothService.getInstance().disconnect();
        }
		 
	}


	private static class BluetoothServiceHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        BluetoothServiceHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mActivity == null || mActivity.get() == null) {return;}

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    mActivity.get().notifyBtStateCallbacks(msg.arg1);
                    switch(msg.arg1){
                        case Constants.STATE_NONE:
                            break;
                        case Constants.STATE_CONNECTING:
                            break;
                        case Constants.STATE_CONNECTED:
                            mActivity.get().showConnectedToDevice();
                            break;
                        case Constants.STATE_CONNECTION_FAILED:
                            mActivity.get().showConnectionFailed();
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                case Constants.MESSAGE_SENT:
                    if(msg.obj != null && (msg.obj instanceof Command)) {
                        mActivity.get().notifyBluetoothCommandCallbacks((Command)msg.obj);
                    }
                    break;
            }
        }
    }

    public void registerBluetoothCommandCallback(BluetoothCommandCallback callback) {
        if(mBluetoothCommandCallbacks != null && !mBluetoothCommandCallbacks.contains(callback)) {
            mBluetoothCommandCallbacks.add(callback);
        }
    }

    public void unregisterBluetoothCommandCallback(BluetoothCommandCallback callback) {
        if(mBluetoothCommandCallbacks != null) {
            mBluetoothCommandCallbacks.remove(callback);
        }
    }

    public  void notifyBluetoothCommandCallbacks(final Command command) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(mBluetoothCommandCallbacks != null && command != null) {
                    for(BluetoothCommandCallback callback : mBluetoothCommandCallbacks) {
                        if(command.isIncomingCommand()) {
                            callback.onBluetoothCommandReceived(command);
                        } else {
                            callback.onBluetoothCommandSent(command);
                        }
                    }
                }
            }
        });

    }

    public void notifyBtStateCallbacks(int newState) {
        invalidateOptionsMenu();
        if(mBtStateChangedCallbacks == null) {
            return;
        }
        for(OnBtServiceStateChangedListener c : mBtStateChangedCallbacks) {
            c.onBluetoothStateChanged(newState);
        }
    }




	


	



	

	

		




}
	
