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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.velli.commander.collections.AdapterProgramEditor;
import com.velli.commander.collections.ListItemProgram;
import com.velli.commander.collections.ListItemProgramStep;
import com.velli.commander.collections.ViewHolderProgramStep;
import com.velli.commander.database.DatabaseHandler;
import com.velli.commander.interfaces.OnGetProgramTaskCallback;
import com.velli.commander.interfaces.OnProgramStepClickListener;
import com.velli.commander.roboto.RobotoTextView;
import com.velli.commander.interfaces.OnProgramStepCountChangedListener;


public class ActivityProgramEditor extends AppCompatActivity implements ActionMenuView.OnMenuItemClickListener, OnGetProgramTaskCallback, OnProgramStepClickListener, OnProgramStepCountChangedListener {
    public static final String INTENT_KEY_PROGRAM_NAME = "PROGRAM_NAME";
    private ActionMenuView mCommandsMenu;
    private RobotoTextView mNoProgramStepsText;
    private RecyclerView mProgramStepsList;
    private AdapterProgramEditor mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mIsInEditMode = false;
    private String mProgramName;
    private ListItemProgram mProgram;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_program);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));


        mProgramName = getIntent().getStringExtra(INTENT_KEY_PROGRAM_NAME);
        mIsInEditMode = mProgramName != null;

        if(mProgramName != null) {
            DatabaseHandler.getInstance().getProgram(mProgramName, this);
        }

        mNoProgramStepsText = (RobotoTextView) findViewById(R.id.activity_edit_program_no_program_steps);
        mNoProgramStepsText.setVisibility(mIsInEditMode ? View.GONE : View.VISIBLE);

        mAdapter = new AdapterProgramEditor(this);
        mAdapter.setOnProgramStepClickListener(this);
        mAdapter.setOnProgramStepCountChangedListener(this);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(mProgramName == null ? getString(R.string.title_new_program) : mProgramName);
        }



        mProgramStepsList = (RecyclerView) findViewById(R.id.activity_edit_program_recycler_view);
        mProgramStepsList.setLayoutManager(new LinearLayoutManager(this));
        mProgramStepsList.setAdapter(mAdapter);

        mItemTouchHelper = new ItemTouchHelper(mItemReorderCallback);
        mItemTouchHelper.attachToRecyclerView(mProgramStepsList);

        mCommandsMenu = (ActionMenuView) findViewById(R.id.activity_edit_program_command_menu);
        mCommandsMenu.setOnMenuItemClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAdapter != null) {
            mAdapter.setOnProgramStepClickListener(null);
            mAdapter.setOnProgramStepCountChangedListener(null);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_delete).setVisible(mIsInEditMode);
        menu.findItem(R.id.menu_save).setVisible(mAdapter.getItemCount() > 0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_program_editor, menu);
        if(mCommandsMenu.getMenu().size() == 0) {
            inflater.inflate(R.menu.menu_activity_program_editor_commands, mCommandsMenu.getMenu());
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_save:
                saveProgram();
                return true;
            case R.id.menu_delete:
                DatabaseHandler.getInstance().deleteProgram(mProgramName);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        ListItemProgramStep step = new ListItemProgramStep();

        switch(item.getItemId()) {
            case R.id.menu_command_move:
                step.setStepType(ListItemProgramStep.PROGRAM_STEP_MOVE);
                editCommandMove(step, false);
                return true;
            case R.id.menu_command_rotate:
                step.setStepType(ListItemProgramStep.PROGRAM_STEP_ROTATE);
                editCommandTurn(step, false);
                return true;
            case R.id.menu_command_set_speed:
                step.setStepType(ListItemProgramStep.PROGRAM_STEP_SET_SPEED);
                editCommandSetSpeed(step, false);
                return true;
            case R.id.menu_command_delay:
                step.setStepType(ListItemProgramStep.PROGRAM_STEP_DELAY);
                editCommandDelay(step, false);
                return true;
            case R.id.menu_command_other:
                step.setStepType(ListItemProgramStep.PROGRAM_STEP_OTHER);
                editCommandOther(step, false);
                return true;
        }
        return false;
    }

    @Override
    public void onGetProgramTaskCallback(ListItemProgram program) {
        if(program != null) {
            mProgram = program;
            mAdapter.setProgramStepsList(program.getProgramStepList());
        }
    }

    @Override
    public void onProgramStepClick(ListItemProgramStep step) {

        switch (step.getStepType()) {
            case ListItemProgramStep.PROGRAM_STEP_MOVE:
                editCommandMove(step, true);
                break;
            case ListItemProgramStep.PROGRAM_STEP_ROTATE:
                editCommandTurn(step, true);
                break;
            case ListItemProgramStep.PROGRAM_STEP_SET_SPEED:
                editCommandSetSpeed(step, true);
                break;
            case ListItemProgramStep.PROGRAM_STEP_DELAY:
                editCommandDelay(step, true);
                break;
            case ListItemProgramStep.PROGRAM_STEP_OTHER:
                editCommandOther(step, true);
                break;
        }
    }

    @Override
    public void onProgramStepCountChanged(int oldCount, int newCount) {
        if(newCount == 0 || (oldCount == 0 && newCount > 0)) {
            invalidateOptionsMenu();
            mNoProgramStepsText.setVisibility(newCount > 0 ? View.GONE : View.VISIBLE);
            mNoProgramStepsText.setAlpha(newCount > 0 ? 1f : 0f);
            mNoProgramStepsText.animate().alpha(newCount > 0 ? 0f : 1f).setDuration(500).start();
        }



    }

    @Override
    public void onProgramStepReorder(RecyclerView.ViewHolder holder) {
        if(mItemTouchHelper != null && holder != null) {
            mItemTouchHelper.startDrag(holder);

            ((ViewHolderProgramStep) holder).mConnectorLineTop.animate().alpha(0f).setDuration(300).start();
            ((ViewHolderProgramStep) holder).mConnectorLineBottom.animate().alpha(0f).setDuration(300).start();

        }
    }

    private void editCommandMove(final ListItemProgramStep step, final boolean edit) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.command_move_directions));

       final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.menu_command_move)
                .customView(R.layout.view_command_options, false)
                .positiveText(android.R.string.ok)
                .dividerColor(getResources().getColor(android.R.color.transparent));
        if(edit) {
            builder.neutralText(R.string.action_delete);
            builder.onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mAdapter.deleteProgramStep(step);
                    dialog.dismiss();
                }
            });
        }

        final MaterialDialog dialog = builder.show();
        final Spinner spinner = (Spinner) dialog.getCustomView().findViewById(R.id.view_command_options_spinner);
        final EditText editText = (EditText) dialog.getCustomView().findViewById(R.id.view_command_options_edit_text);

        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(edit);
        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                step.setDistanceToMove(Integer.valueOf(editText.getText().toString()), spinner.getSelectedItemPosition());
                dialog.dismiss();

                if(edit) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.addProgramStep(step);
                }
            }
        });
        if(edit) {
            editText.setText(String.valueOf(step.getDistanceToMove()));
            spinner.setSelection(step.getMovingDirection());
        }
        spinner.setAdapter(adapter);
        editText.setHint(R.string.edit_text_hint_distance);
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
    }

    private void editCommandTurn(final ListItemProgramStep step, final boolean edit) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.command_rotate_directions));

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.menu_command_rotate)
                .customView(R.layout.view_command_options, false)
                .positiveText(android.R.string.ok)
                .dividerColor(getResources().getColor(android.R.color.transparent));

        if(edit) {
            builder.neutralText(R.string.action_delete);
            builder.onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mAdapter.deleteProgramStep(step);
                    dialog.dismiss();
                }
            });
        }

        final MaterialDialog dialog = builder.show();
        final Spinner spinner = (Spinner) dialog.getCustomView().findViewById(R.id.view_command_options_spinner);
        final EditText editText = (EditText) dialog.getCustomView().findViewById(R.id.view_command_options_edit_text);

        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(edit);
        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                step.setDegreesToRotate(Integer.valueOf(editText.getText().toString()), spinner.getSelectedItemPosition());
                dialog.dismiss();

                if(edit) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.addProgramStep(step);
                }
            }
        });

        if(edit) {
            editText.setText(String.valueOf(step.getDegreesToRotate()));
            spinner.setSelection(step.getRotationDirection());
        }
        spinner.setAdapter(adapter);
        editText.setHint(R.string.edit_text_hint_angle);
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
    }

    private void editCommandDelay(final ListItemProgramStep step, final boolean edit) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.command_delay_units));

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.menu_command_delay)
                .customView(R.layout.view_command_options, false)
                .positiveText(android.R.string.ok)
                .dividerColor(getResources().getColor(android.R.color.transparent));
        if(edit) {
            builder.neutralText(R.string.action_delete);
            builder.onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mAdapter.deleteProgramStep(step);
                    dialog.dismiss();
                }
            });
        }

        final MaterialDialog dialog = builder.show();
        final Spinner spinner = (Spinner) dialog.getCustomView().findViewById(R.id.view_command_options_spinner);
        final EditText editText = (EditText) dialog.getCustomView().findViewById(R.id.view_command_options_edit_text);

        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(edit);
        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long time = Long.valueOf(editText.getText().toString());
                int unit = spinner.getSelectedItemPosition();
                step.setTimeToDelay(time, unit);
                dialog.dismiss();

                if(edit) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.addProgramStep(step);
                }
            }
        });


        if(edit) {
            editText.setText(String.valueOf(step.getTimeToDelay()));
            spinner.setSelection(step.getDelayUnit());
        }
        spinner.setAdapter(adapter);
        editText.setHint(R.string.edit_text_hint_delay);
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


    }

    private void editCommandSetSpeed(final ListItemProgramStep step, final boolean edit) {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.menu_command_set_speed)
                .customView(R.layout.view_command_speed, false)
                .positiveText(android.R.string.ok)
                .dividerColor(getResources().getColor(android.R.color.transparent));

        if(edit) {
            builder.neutralText(R.string.action_delete);
            builder.onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mAdapter.deleteProgramStep(step);
                    dialog.dismiss();
                }
            });
        }

        final MaterialDialog dialog = builder.show();

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                step.setSpeed(((AppCompatSeekBar)dialog.getCustomView().findViewById(R.id.view_command_options_seek_bar)).getProgress());
                dialog.dismiss();

                if(edit) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.addProgramStep(step);
                }
            }
        });

        ((AppCompatSeekBar)dialog.getCustomView().findViewById(R.id.view_command_options_seek_bar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((RobotoTextView)dialog.getCustomView().findViewById(R.id.view_command_options_text)).setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void editCommandOther(final ListItemProgramStep step, final boolean edit) {

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.menu_command_other)
                .customView(R.layout.view_command_other, false)
                .positiveText(android.R.string.ok)
                .dividerColor(getResources().getColor(android.R.color.transparent));

        if(edit) {
            builder.neutralText(R.string.action_delete);
            builder.onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mAdapter.deleteProgramStep(step);
                    dialog.dismiss();
                }
            });
        }

        final MaterialDialog dialog = builder.show();
        final EditText editText = (EditText) dialog.getCustomView().findViewById(R.id.view_command_options_edit_text);

        if(edit) {
            editText.setText(step.getOtherCommand());
        }
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
        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(edit);
        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                step.setOtherCommand(editText.getText().toString());
                dialog.dismiss();

                if(edit) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.addProgramStep(step);
                }
            }
        });
    }

    private void saveProgram() {
        if(mProgram != null) {
            overwriteProgram();
            return;
        }

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.menu_save)
                .customView(R.layout.view_command_other, false)
                .positiveText(android.R.string.ok)
                .dividerColor(getResources().getColor(android.R.color.transparent))
                .show();

        final EditText editText = (EditText) dialog.getCustomView().findViewById(R.id.view_command_options_edit_text);
        editText.setHint(R.string.edit_text_hint_program_name);
        editText.setText(Utils.getDateTimeString());
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
                ListItemProgram program = new ListItemProgram();
                program.setProgramName(editText.getText().toString());
                program.setProgramStepList(mAdapter.getProgramStepList());
                DatabaseHandler.getInstance().writeProgram(program);
                dialog.dismiss();
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(editText.getText().toString());
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.toast_message_program_saved, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void overwriteProgram() {
        ListItemProgram program = new ListItemProgram();
        program.setProgramName(mProgramName);
        program.setProgramStepList(mAdapter.getProgramStepList());
        DatabaseHandler.getInstance().deleteProgram(mProgramName);
        DatabaseHandler.getInstance().writeProgram(program);

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.toast_message_program_saved, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private final ItemTouchHelper.SimpleCallback mItemReorderCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            /* This function is called when the user is dragging list item to new position */
            final int fromPos = viewHolder.getAdapterPosition();
            final int toPos = target.getAdapterPosition();

            boolean showConnectorLineTop = (fromPos != 0);
            boolean showConnectorLineBottom = (fromPos < mAdapter.getItemCount() - 1);

            /* Change stepper circle text to correspond new position. Add +1 to steppers new position
             * because position counting in list starts with 0
             */
            ((ViewHolderProgramStep) target).mStepCircle.setText(String.valueOf(fromPos +1));
            ((ViewHolderProgramStep) viewHolder).mStepCircle.setText(String.valueOf(toPos +1));

            /* Change visibility of the stepper circle connector lines if the new position is at
             * the top of the list or at the bottom
             */
            ((ViewHolderProgramStep) target).mConnectorLineTop.setVisibility(showConnectorLineTop ? View.VISIBLE : View.GONE);
            ((ViewHolderProgramStep) target).mConnectorLineBottom.setVisibility(showConnectorLineBottom ? View.VISIBLE : View.GONE);
            mAdapter.moveProgramStep(fromPos, toPos);

            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            /* This function is called when user starts or stops dragging list item */
            boolean showConnectorLineTop = (viewHolder.getAdapterPosition() != 0);
            boolean showConnectorLineBottom = (viewHolder.getAdapterPosition() < mAdapter.getItemCount() - 1);

            /* Change visibility of the connector lines of the item that is being dragged */
            ((ViewHolderProgramStep) viewHolder).mConnectorLineTop.setVisibility(showConnectorLineTop ? View.VISIBLE : View.GONE);
            ((ViewHolderProgramStep) viewHolder).mConnectorLineBottom.setVisibility(showConnectorLineBottom ? View.VISIBLE : View.GONE);
            ((ViewHolderProgramStep) viewHolder).mConnectorLineTop.animate().alpha(0.12f).setDuration(300).start();
            ((ViewHolderProgramStep) viewHolder).mConnectorLineBottom.animate().alpha(0.12f).setDuration(300).start();
        }
    };



}
