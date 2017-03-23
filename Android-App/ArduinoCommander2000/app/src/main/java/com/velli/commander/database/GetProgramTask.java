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

package com.velli.commander.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import com.velli.commander.Utils;
import com.velli.commander.collections.ListItemProgram;
import com.velli.commander.collections.ListItemProgramStep;
import com.velli.commander.interfaces.OnGetProgramTaskCallback;

import java.util.ArrayList;


public class GetProgramTask extends AsyncTask<Void, Void, ListItemProgram> {
    private SQLiteDatabase mDb;
    private String mProgramName;
    private OnGetProgramTaskCallback mListener;

    public GetProgramTask(SQLiteDatabase db, String programName) {
        mDb = db;
        mProgramName = programName;
    }

    public void setOnGetProgramTaskCallback(OnGetProgramTaskCallback callback) {
        mListener = callback;
    }

    @Override
    protected ListItemProgram doInBackground(Void... params) {
        if(mDb == null) {
            return null;
        }
        mDb.beginTransaction();
        ListItemProgram program = new ListItemProgram();
        program.setProgramName(mProgramName);

        Cursor cursor;

        try {
           cursor = mDb.rawQuery("SELECT " + DatabaseConstants.columnSelection  + " FROM " + Utils.escapeSqlString(mProgramName) + " ORDER BY " + DatabaseConstants.KEY_PROGRAM_STEP_ORDER_IN_PROGRAM + " ASC", null);
        } catch (SQLiteException e) {
            mDb.endTransaction();
            return null;
        }

        if(cursor != null && cursor.moveToFirst()) {
            ArrayList<ListItemProgramStep> list = new ArrayList<>();
            do {
                ListItemProgramStep step = new ListItemProgramStep(cursor.getInt(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_TYPE)));

                step.setDegreesToRotate(cursor.getInt(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_DEGREES_TO_ROTATE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_ROTATION_DIRECTION)));
                step.setDistanceToMove(cursor.getInt(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_DISTANCE_TO_MOVE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_MOVING_DIRECTION)));
                step.setSpeed(cursor.getInt(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_SPEED_TO_SET)));
                if(!cursor.isNull(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_TIME_TO_DELAY))){
                    step.setTimeToDelay(Long.valueOf(cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_TIME_TO_DELAY))),
                        cursor.getInt(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_TIME_UNIT)));
                }
                if(!cursor.isNull(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_OTHER_COMMAND))){
                    step.setOtherCommand(cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_PROGRAM_STEP_OTHER_COMMAND)));
                }
                list.add(step);
            } while(cursor.moveToNext());

            program.setProgramStepList(list);
        }

        if(cursor != null) {
            cursor.close();
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();

        return program;
    }

    @Override
    protected void onPostExecute(ListItemProgram listItemProgram) {
        super.onPostExecute(listItemProgram);
        if(mListener != null) {
            mListener.onGetProgramTaskCallback(listItemProgram);
        }
        mListener = null;
        mDb = null;
    }
}
