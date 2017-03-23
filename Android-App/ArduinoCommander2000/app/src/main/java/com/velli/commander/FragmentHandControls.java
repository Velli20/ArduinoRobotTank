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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.velli.commander.roboto.RobotoTextView;
import com.velli.commander.views.JoystickView;
import com.velli.commander.collections.Command;

import java.util.Locale;


public class FragmentHandControls extends Fragment implements JoystickView.OnJoyStickProgressChangedListener {
    private JoystickView mJoyStick;
    private RobotoTextView mTextSpeed;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hand_controls, container, false);

        mTextSpeed = (RobotoTextView) view.findViewById(R.id.fragment_hand_controls_text_speed);
        mTextSpeed.setText(String.format(Locale.getDefault(), getString(R.string.action_speed_manual_control), 0, 0));

        mJoyStick = (JoystickView) view.findViewById(R.id.fragment_hand_controls_controller);
        mJoyStick.setOnJoyStickProgressChangedListener(this);
        return view;
    }

    @Override
    public void onProgressChanged(JoystickView view, int progressY, int progressX) {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        mTextSpeed.setText(String.format(Locale.getDefault(), getString(R.string.action_speed_manual_control), progressY, progressX));

        if(BluetoothService.getInstance().getState() != Constants.STATE_CONNECTED) {
            Snackbar.make(getView(), R.string.error_not_connected, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.menu_bluetooth_connect, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), ActivitySearchDevices.class);
                            startActivityForResult(i, MainActivity.REQUEST_CODE_SCAN_FOR_DEVICES);
                        }
                    }).show();
            return;
        }

        String commandPrefixAxisX = prefs.getString(getString(R.string.preference_command_prefix_joystick_x_axis),
                getString(R.string.default_value_prefix_joystick_y_axis));
        String commandPrefixAxisY = prefs.getString(getString(R.string.preference_command_prefix_joystick_y_axis),
                getString(R.string.default_value_prefix_joystick_x_axis));

        String command = String.format(Locale.getDefault(),"%s%d%s%d", commandPrefixAxisX, progressX, commandPrefixAxisY, progressY);

        BluetoothService.getInstance().sendCommand(new Command(command, System.currentTimeMillis(), false));
    }



}
