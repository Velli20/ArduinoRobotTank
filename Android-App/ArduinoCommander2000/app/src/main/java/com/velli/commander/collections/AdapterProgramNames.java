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
import com.velli.commander.R;
import com.velli.commander.interfaces.OnProgramNameClickListener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;



public class AdapterProgramNames extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<String> mProgramNames;
    private LayoutInflater mInflater;
    private OnProgramNameClickListener mProgramNameClickListener;

    public AdapterProgramNames(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setProgramNamesList(ArrayList<String> programNames) {
        mProgramNames = programNames;
    }

    public void setOnProgramNameClickListener(OnProgramNameClickListener listener) {
        mProgramNameClickListener = listener;
    }

    public String getProgramName(int position) {
        return mProgramNames.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderProgramName(mInflater.inflate(R.layout.list_item_program_name, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolderProgramName)holder).mProgramName.setText(getProgramName(position));
        ((ViewHolderProgramName)holder).itemView.setOnClickListener(new ProgramNameClickListener(position));
        ((ViewHolderProgramName)holder).mButtonOverflow.setOnClickListener(new OverflowButtonClickListener(position));
    }

    @Override
    public int getItemCount() {
        return mProgramNames == null ? 0 : mProgramNames.size();
    }

    private class ProgramNameClickListener implements View.OnClickListener {
        int mPosition;

        public ProgramNameClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if(mProgramNameClickListener != null) {
                mProgramNameClickListener.onProgramNameClick(mProgramNames.get(mPosition), mPosition);
            }
        }
    }

    private class OverflowButtonClickListener implements View.OnClickListener {
        private int mPosition;

        public OverflowButtonClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if(mProgramNameClickListener != null) {
                mProgramNameClickListener.onOverflowButtonClicked(v, mPosition);
            }
        }
    }
}
