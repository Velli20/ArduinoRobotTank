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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.velli.commander.collections.AdapterConsoleCommands;
import com.velli.commander.collections.Command;
import com.velli.commander.interfaces.BluetoothCommandCallback;


public class FragmentConsole extends Fragment implements BluetoothCommandCallback, View.OnClickListener {
    private RecyclerView mConsoleMessages;
    private AppCompatButton mButtonSendCommand;
    private AppCompatEditText mEditTextCommand;
    private AdapterConsoleCommands mAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_console, container, false);

        mAdapter = new AdapterConsoleCommands(getActivity());

        mConsoleMessages = (RecyclerView) view.findViewById(R.id.fragment_console_recycler_view);
        mConsoleMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        mConsoleMessages.setAdapter(mAdapter);

        mButtonSendCommand = (AppCompatButton) view.findViewById(R.id.fragment_console_button_send);
        mButtonSendCommand.setOnClickListener(this);
        mEditTextCommand = (AppCompatEditText) view.findViewById(R.id.fragment_console_edit_text_command);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).registerBluetoothCommandCallback(this);
        mAdapter.addCommandsList(BluetoothService.getInstance().getCommandsList());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).unregisterBluetoothCommandCallback(this);
        if(mAdapter != null) {
            mAdapter.addCommandsList(null);
        }
    }

    @Override
    public void onBluetoothCommandReceived(Command command) {
        mAdapter.addCommand(command);
    }

    @Override
    public void onBluetoothCommandSent(Command command) {
        mAdapter.addCommand(command);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_console_button_send:
                if(BluetoothService.getInstance().getState() != Constants.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.error_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                }
                String command = mEditTextCommand.getText().toString();
                if(!command.isEmpty()) {
                    BluetoothService.getInstance().sendCommand(new Command(command, System.currentTimeMillis(), false));
                }
                mEditTextCommand.setText("");
                break;
        }
    }
}
