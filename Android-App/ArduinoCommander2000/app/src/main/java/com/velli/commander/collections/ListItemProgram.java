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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class ListItemProgram {
    private String mProgramName;
    private ArrayList<ListItemProgramStep> mProgramSteps;


    public ListItemProgram() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        mProgramName = String.format(Locale.getDefault(), "PROGRAM_%02d_%02d_%02d-%02d_%02d", cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));

    }

    public void setProgramName(String programName) {
        mProgramName = programName;
    }

    public String getProgramName() {
        return mProgramName;
    }

    public void setProgramStepList(ArrayList<ListItemProgramStep> list) {
        mProgramSteps = list;
    }

    public ArrayList<ListItemProgramStep> getProgramStepList() {
        return mProgramSteps;
    }
}
