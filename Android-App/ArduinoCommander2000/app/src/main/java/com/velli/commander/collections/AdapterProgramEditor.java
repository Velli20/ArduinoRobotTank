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

package com.velli.commander.collections;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.velli.commander.R;
import com.velli.commander.interfaces.OnProgramStepClickListener;
import com.velli.commander.interfaces.OnProgramStepCountChangedListener;

import java.util.ArrayList;
import java.util.Collections;


public class AdapterProgramEditor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LayoutInflater mInflater;
    private ArrayList<ListItemProgramStep> mListProgramSteps;
    private Resources mRes;
    private OnProgramStepClickListener mListener;
    private OnProgramStepCountChangedListener mStepCountListener;

    public AdapterProgramEditor(Context context) {
        mInflater = LayoutInflater.from(context);
        mRes = context.getResources();
    }

    public void setProgramStepsList(ArrayList<ListItemProgramStep> list) {
        mListProgramSteps = list;
        notifyDataSetChanged();
    }

    public void setOnProgramStepClickListener(OnProgramStepClickListener listener) {
        mListener = listener;
    }

    public void setOnProgramStepCountChangedListener(OnProgramStepCountChangedListener listener) {
        mStepCountListener = listener;
    }

    public void addProgramStep(ListItemProgramStep step) {
        if(mListProgramSteps == null) {
            mListProgramSteps = new ArrayList<>();
        }

        if(mStepCountListener != null) {
            int count = mListProgramSteps.size();
            mStepCountListener.onProgramStepCountChanged(count, count+1);
        }

        mListProgramSteps.add(step);
        notifyDataSetChanged();
    }

    public void deleteProgramStep(ListItemProgramStep step) {
        if(mListProgramSteps == null) {
            mListProgramSteps = new ArrayList<>();
        }
        if(mStepCountListener != null) {
            int count = mListProgramSteps.size();
            mStepCountListener.onProgramStepCountChanged(count, count-1);
        }
        mListProgramSteps.remove(step);
        notifyDataSetChanged();


    }

    public ListItemProgramStep getProgramStepAt(int position) {
        if(mListProgramSteps == null) {
            return null;
        }
        return mListProgramSteps.get(position);
    }

    public void moveProgramStep(int fromPos, int toPos) {
        /* Update step items position in list so we get right
           step when ProgramStepClickListener is triggered
         */
        mListProgramSteps.get(fromPos).setPositionInProgramList(toPos);
        mListProgramSteps.get(toPos).setPositionInProgramList(fromPos);
        Collections.swap(mListProgramSteps, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
    }



    public ArrayList<ListItemProgramStep> getProgramStepList() {
        return mListProgramSteps;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolderProgramStep viewHolder = new ViewHolderProgramStep(mInflater.inflate(R.layout.list_item_program_step, parent, false));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        /* Store step position in list item because */
        mListProgramSteps.get(position).setPositionInProgramList(position);
        ((ViewHolderProgramStep)holder).mStepCircle.setText(String.valueOf((position + 1)));
        ((ViewHolderProgramStep)holder).mStepText.setText(getItemText(mListProgramSteps.get(position)));

        boolean showConnectorLineTop = (position != 0);
        boolean showConnectorLineBottom = (position < getItemCount() - 1);


        ((ViewHolderProgramStep) holder).mConnectorLineTop.setVisibility(showConnectorLineTop ? View.VISIBLE : View.GONE);
        ((ViewHolderProgramStep) holder).mConnectorLineBottom.setVisibility(showConnectorLineBottom ? View.VISIBLE : View.GONE);


        ((ViewHolderProgramStep)holder).itemView.setOnClickListener(new ProgramStepClickListener(getProgramStepAt(position)));
        ((ViewHolderProgramStep)holder).mButtonReorder.setOnTouchListener(new ReorderButtonTouchListener(holder));
    }

    private String getItemText(ListItemProgramStep item) {
        switch(item.getStepType()) {
            case ListItemProgramStep.PROGRAM_STEP_DELAY:
                return String.format(mRes.getString(R.string.action_command_delay), item.getTimeToDelay(), mRes.getStringArray(R.array.command_delay_units)[item.getDelayUnit()]);
            case ListItemProgramStep.PROGRAM_STEP_MOVE:
                return String.format(mRes.getString(R.string.action_command_move), item.getDistanceToMove(), mRes.getStringArray(R.array.command_move_directions)[item.getMovingDirection()].toLowerCase());
            case ListItemProgramStep.PROGRAM_STEP_OTHER:
                return item.getOtherCommand();
            case ListItemProgramStep.PROGRAM_STEP_ROTATE:
                return String.format(mRes.getString(R.string.action_command_rotate), item.getDegreesToRotate(), mRes.getStringArray(R.array.command_rotate_directions)[item.getRotationDirection()].toLowerCase());
            case ListItemProgramStep.PROGRAM_STEP_SET_SPEED:
                return String.format(mRes.getString(R.string.action_command_set_speed), item.getSpeedToSet());
        }
        return "-";
    }

    @Override
    public int getItemCount() {
        return mListProgramSteps == null ? 0 : mListProgramSteps.size();
    }

    private class ProgramStepClickListener implements View.OnClickListener {
        private ListItemProgramStep mStep;

        public ProgramStepClickListener(ListItemProgramStep step) {
            mStep = step;
        }



        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onProgramStepClick(mStep);
            }
        }
    }

    private class ReorderButtonTouchListener implements View.OnTouchListener {
        private RecyclerView.ViewHolder mViewHolder;

        public ReorderButtonTouchListener(RecyclerView.ViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(mListener != null) {
                mListener.onProgramStepReorder(mViewHolder);
            }
            return false;
        }
    }
}
