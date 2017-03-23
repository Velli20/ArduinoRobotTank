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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.velli.commander.R;
import com.velli.commander.roboto.RobotoTextView;



public class ViewHolderProgramStep extends RecyclerView.ViewHolder {
    public RobotoTextView mStepCircle;
    public RobotoTextView mStepText;
    public View mConnectorLineTop;
    public View mConnectorLineBottom;
    public ImageButton mButtonReorder;


    public ViewHolderProgramStep(View itemView) {
        super(itemView);

        mStepCircle = (RobotoTextView) itemView.findViewById(R.id.list_item_program_step_icon);
        mStepText = (RobotoTextView) itemView.findViewById(R.id.list_item_program_step_text);

        mConnectorLineTop = itemView.findViewById(R.id.list_item_program_step_connector_line_top);
        mConnectorLineBottom = itemView.findViewById(R.id.list_item_program_step_connector_line_bottom);

        mButtonReorder = (ImageButton) itemView.findViewById(R.id.list_item_program_step_button_reorder);
    }
}
