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

import android.content.ContentValues;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import com.velli.commander.Utils;
import com.velli.commander.collections.ListItemProgram;
import com.velli.commander.collections.ListItemProgramStep;
import com.velli.commander.interfaces.OnWriteProgramTaskResultListener;



public class WriteProgramTask extends AsyncTask<Void, Void, Integer> {
    public static final String TAG = "WriteProgramTask";
    public static final int RESULT_OK = 1;
    public static final int RESULT_DATABASE_NOT_OPEN = -1;
    public static final int RESULT_INVALID_PROGRAM_NAME = -2;

    private SQLiteDatabase mDb;
    private ListItemProgram mProgram;
    private OnWriteProgramTaskResultListener mListener;

    public WriteProgramTask(SQLiteDatabase db, ListItemProgram program) {
        mDb = db;
        mProgram = program;
    }

    public void setOnWriteProgramTaskResultListener(OnWriteProgramTaskResultListener listener) {
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if(mDb == null) {
            return RESULT_DATABASE_NOT_OPEN;
        }
        mDb.beginTransaction();

        String tableName = DatabaseConstants.getProgramTable(Utils.escapeSqlString(mProgram.getProgramName()));
        try {
            mDb.execSQL(tableName);
        } catch (SQLiteCantOpenDatabaseException e){
            mDb.endTransaction();
            return RESULT_DATABASE_NOT_OPEN;
        } catch(SQLiteException e) {
            mDb.endTransaction();
            return RESULT_INVALID_PROGRAM_NAME;
        }



        int stepPositionInProgram = 0;

        for(ListItemProgramStep step : mProgram.getProgramStepList()) {
            ContentValues values = new ContentValues();

            values.put(DatabaseConstants.KEY_PROGRAM_STEP_TYPE, step.getStepType());
            values.put(DatabaseConstants.KEY_PROGRAM_STEP_DEGREES_TO_ROTATE, step.getDegreesToRotate());
            values.put(DatabaseConstants.KEY_PROGRAM_STEP_DISTANCE_TO_MOVE, step.getDistanceToMove());
            values.put(DatabaseConstants.KEY_PROGRAM_STEP_SPEED_TO_SET, step.getSpeedToSet());
            values.put(DatabaseConstants.KEY_PROGRAM_STEP_TIME_TO_DELAY, step.getTimeToDelay());
            values.put(DatabaseConstants.KEY_PROGRAM_STEP_ROTATION_DIRECTION, step.getRotationDirection());
            values.put(DatabaseConstants.KEY_PROGRAM_STEP_MOVING_DIRECTION, step.getMovingDirection());
            values.put(DatabaseConstants.KEY_PROGRAM_STEP_TIME_UNIT, step.getDelayUnit());
            values.put(DatabaseConstants.KEY_PROGRAM_STEP_ORDER_IN_PROGRAM, stepPositionInProgram);

            if(step.getOtherCommand() != null) {
                values.put(DatabaseConstants.KEY_PROGRAM_STEP_OTHER_COMMAND, step.getOtherCommand());
            }

            stepPositionInProgram++;

            mDb.insert(Utils.escapeSqlString(mProgram.getProgramName()), null, values);
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        return RESULT_OK;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        if(mListener != null) {
            mListener.onWriteProgramTaskResult(integer);
        }
        mListener = null;
        mDb = null;
    }
}
