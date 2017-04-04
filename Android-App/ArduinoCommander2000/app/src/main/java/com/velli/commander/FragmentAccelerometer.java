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

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.velli.commander.collections.Command;

import java.util.Locale;

public class FragmentAccelerometer extends Fragment implements SensorEventListener{
	private static final String TAG = " FragmentAccelerometer";
	private View mView;
	private SensorManager mSensorManager;
	private ProgressBar mProgressBarX, mProgressBarY;
	private TextView mTextX, mTextY;


    private static final float IN_MAX = 10f;
    private static final float IN_MIN = -10f;
    private static final float OUT_MAX = 255f;
    private static final float OUT_MIN = -255f;

    private static final long DEFAULT_SENSOR_UPDATE_INTERVAL = 300;

    private long mLastSensorUpdate = -1;
    private long mSensorUpdateInterval = DEFAULT_SENSOR_UPDATE_INTERVAL;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
		mView = inflater.inflate(R.layout.fragment_accelerometer, root, false);
		mSensorManager = (SensorManager)getActivity().getSystemService(Activity.SENSOR_SERVICE);
		mProgressBarX = (ProgressBar)mView.findViewById(R.id.progressBar_xAxis);
		mProgressBarY = (ProgressBar)mView.findViewById(R.id.progressBar_yAxis);
		mTextX = (TextView)mView.findViewById(R.id.textView_xAxis);
		mTextY = (TextView)mView.findViewById(R.id.textView_yAxis);


		return mView;
	}



	@Override
	public void onResume(){
		super.onResume();
		if (App.DEBUG) {
			Log.i(TAG, TAG + "onResume()");
		}
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onPause(){
		super.onPause();
		if (App.DEBUG) {
			Log.i(TAG, TAG + "onPause()");
		}
		mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
		if (BluetoothService.getInstance().getState() == Constants.STATE_CONNECTED) {
			String command = String.format(Locale.getDefault(), "X%dY%d", 0, 0);
			BluetoothService.getInstance().sendCommand(new Command(command, System.currentTimeMillis(), false));
		}
	}



	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
        if(System.currentTimeMillis() < (mLastSensorUpdate + mSensorUpdateInterval) && mLastSensorUpdate != -1) {
            return;
        }
        if (BluetoothService.getInstance().getState() == Constants.STATE_CONNECTED) {
            float out_x = (event.values[0] - (IN_MIN)) * (OUT_MAX - OUT_MIN) / (IN_MAX - (IN_MIN)) + OUT_MIN;
            float out_y = (event.values[1] - (IN_MIN)) * (OUT_MAX - OUT_MIN) / (IN_MAX - (IN_MIN)) + OUT_MIN;

            String command = String.format(Locale.getDefault(), "X%dY%d", (int)out_x, (int)out_y);
            BluetoothService.getInstance().sendCommand(new Command(command, System.currentTimeMillis(), false));
        }


        float xAxis = event.values[0];
        float yAxis = event.values[1];


	    float mProgressStatus_x = (xAxis - (-10f)) * (255 - 0) / (10f - (-10f)) + 0;
	    float mProgressStatus_y = (yAxis - (-10f)) * (255 - 0) / (10f - (-10f)) + 0;

	    mProgressBarX.setProgress((int)mProgressStatus_x);
	    mProgressBarY.setProgress((int) mProgressStatus_y);
	    mTextX.setText(Integer.toString((int)xAxis));
	    mTextY.setText(Integer.toString((int)yAxis));

        mLastSensorUpdate = System.currentTimeMillis();

	}
	



}
