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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.velli.commander.collections.AdapterProgramNames;
import com.velli.commander.collections.Command;
import com.velli.commander.collections.ListItemProgram;
import com.velli.commander.collections.ListItemProgramStep;
import com.velli.commander.database.DatabaseHandler;
import com.velli.commander.database.RenameProgramTask;
import com.velli.commander.interfaces.OnDatabaseChangedListener;
import com.velli.commander.interfaces.OnGetProgramNamesCallback;
import com.velli.commander.interfaces.OnGetProgramTaskCallback;
import com.velli.commander.interfaces.OnProgramNameClickListener;
import com.velli.commander.interfaces.OnRenameProgramTaskListener;

import java.util.ArrayList;


public class FragmentPrograms extends Fragment implements View.OnClickListener, OnGetProgramNamesCallback, OnProgramNameClickListener, OnDatabaseChangedListener {
    public static final String TAG = "FragmentPrograms";
    private RecyclerView mRecyclerView;
    private LinearLayout mViewNoPrograms;
    private FloatingActionButton mButtonNewProgram;
    private AdapterProgramNames mAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_programs, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_programs_recycler_view);
        mViewNoPrograms = (LinearLayout) view.findViewById(R.id.view_no_programs);
        mButtonNewProgram = (FloatingActionButton) view.findViewById(R.id.fragment_programs_button_new_program);
        mButtonNewProgram.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new AdapterProgramNames(getActivity());
        mAdapter.setOnProgramNameClickListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        DatabaseHandler.getInstance().registerOnDatabaseChangedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAdapter != null) {
            mAdapter.setOnProgramNameClickListener(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseHandler.getInstance().getProgramNames(this);
        DatabaseHandler.getInstance().registerOnDatabaseChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        DatabaseHandler.getInstance().unregisterOnDatabaseChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.fragment_programs_button_new_program:
                Intent i = new Intent(getActivity(), ActivityProgramEditor.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void onGetProgramNamesCallback(ArrayList<String> programNames) {
        Log.i(TAG, TAG + " onGetProgramNamesCallback list size: " + programNames.size());
        if(programNames == null || programNames.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mViewNoPrograms.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mViewNoPrograms.setVisibility(View.GONE);
            mAdapter.setProgramNamesList(programNames);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onProgramNameClick(String programName, int position) {
        Intent i = new Intent(getActivity(), ActivityProgramEditor.class);
        i.putExtra(ActivityProgramEditor.INTENT_KEY_PROGRAM_NAME, programName);
        startActivity(i);
    }

    @Override
    public void onOverflowButtonClicked(View v, int positionInList) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_fragment_programs_program_options, popup.getMenu());
        popup.setOnMenuItemClickListener(new OnOverflowMenuItemClickListener(positionInList));
        popup.show();

    }

    @Override
    public void onDatabaseChanged() {
        DatabaseHandler.getInstance().getProgramNames(this);
    }

    private class OnOverflowMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private final int mPosition;

        public OnOverflowMenuItemClickListener(int positionInList) {
            mPosition = positionInList;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_run_program:
                    DatabaseHandler.getInstance().getProgram(mAdapter.getProgramName(mPosition), new OnGetProgramTaskCallback() {
                        @Override
                        public void onGetProgramTaskCallback(ListItemProgram program) {
                            if(program != null) {
                                runProgram(program);
                            }
                        }
                    });
                    return true;
                case R.id.menu_rename_program:
                    showRenameProgramDialog(mAdapter.getProgramName(mPosition));
                    return true;
                case R.id.menu_delete_program:
                    DatabaseHandler.getInstance().deleteProgram(mAdapter.getProgramName(mPosition));
                    return true;
            }
            return false;
        }
    }

    private void showRenameProgramDialog(final String programToRename) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.menu_rename_program)
                .customView(R.layout.view_command_other, false)
                .positiveText(android.R.string.ok)
                .dividerColor(getResources().getColor(android.R.color.transparent))
                .show();

        final EditText editText = (EditText) dialog.getCustomView().findViewById(R.id.view_command_options_edit_text);

        editText.setText(programToRename);
        editText.setHint(R.string.edit_text_hint_program_name);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(editText.getText().toString().length() > 0);
            }
        });

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler.getInstance().renameProgram(programToRename, editText.getText().toString(), new OnRenameProgramTaskListener() {
                    @Override
                    public void onProgramRenamed(int resultCode) {
                        if(resultCode == RenameProgramTask.RESULT_OK && getView() != null) {
                            Snackbar.make(getView(), R.string.toast_message_program_renamed, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
    }

    private void runProgram(ListItemProgram program) {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);

        for(ListItemProgramStep step : program.getProgramStepList()) {
            StringBuilder command = new StringBuilder();

            switch(step.getStepType()) {
                case ListItemProgramStep.PROGRAM_STEP_DELAY:
                    command.append(prefs.getString(getString(R.string.preference_command_prefix_delay),
                            getString(R.string.default_value_prefix_delay)));

                    switch (step.getDelayUnit()) {
                        case ListItemProgramStep.PROGRAM_DELAY_UNIT_MIN:
                            command.append(step.getTimeToDelay() * 1000 * 60);
                            break;
                        case ListItemProgramStep.PROGRAM_DELAY_UNIT_S:
                            command.append(step.getTimeToDelay() * 1000);
                            break;
                        case ListItemProgramStep.PROGRAM_DELAY_UNIT_MS:
                            command.append(step.getTimeToDelay());
                            break;
                    }
                    break;
                case ListItemProgramStep.PROGRAM_STEP_MOVE:
                    if(step.getMovingDirection() == ListItemProgramStep.PROGRAM_MOVING_DIRECTION_FORWARD) {
                        command.append(prefs.getString(getString(R.string.preference_command_prefix_move_forward),
                                getString(R.string.default_value_prefix_move_forward)));
                    } else {
                        command.append(prefs.getString(getString(R.string.preference_command_prefix_move_backward),
                                getString(R.string.default_value_prefix_move_backward)));
                    }
                    command.append(step.getDistanceToMove());
                    break;
                case ListItemProgramStep.PROGRAM_STEP_OTHER:
                    command.append(step.getOtherCommand());
                    break;
                case ListItemProgramStep.PROGRAM_STEP_ROTATE:
                    if(step.getRotationDirection() == ListItemProgramStep.PROGRAM_ROTATION_DIRECTION_CLOCKWISE) {
                        command.append(prefs.getString(getString(R.string.preference_command_prefix_rotate_clockwise),
                                getString(R.string.default_value_prefix_rotate_clockwise)));
                    } else {
                        command.append(prefs.getString(getString(R.string.preference_command_prefix_rotate_counterclockwise),
                                getString(R.string.default_value_prefix_rotate_counterclockwise)));
                    }
                    command.append(step.getDegreesToRotate());
                    break;
                case ListItemProgramStep.PROGRAM_STEP_SET_SPEED:
                    command.append(prefs.getString(getString(R.string.preference_command_prefix_set_speed),
                            getString(R.string.default_value_prefix_set_speed)));
                    command.append(step.getSpeedToSet());
                    break;
            }
            BluetoothService.getInstance().sendCommand(new Command(command.toString(), System.currentTimeMillis(), false));
        }
    }


}
