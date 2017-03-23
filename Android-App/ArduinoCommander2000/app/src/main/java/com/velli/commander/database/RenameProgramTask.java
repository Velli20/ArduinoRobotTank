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

import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import com.velli.commander.Utils;
import com.velli.commander.interfaces.OnRenameProgramTaskListener;



public class RenameProgramTask extends AsyncTask<Void, Void, Integer> {
    public static final String TAG = "RenameProgramTask";
    public static final int RESULT_OK = 1;
    public static final int RESULT_DATABASE_NOT_OPEN = -1;
    public static final int RESULT_INVALID_PROGRAM_NAME = -2;

    private SQLiteDatabase mDb;
    private String mProgramName;
    private String mNewProgramName;
    private OnRenameProgramTaskListener mListener;

    public RenameProgramTask(SQLiteDatabase db, String programName, String newName) {
        mProgramName = programName;
        mNewProgramName = newName;
        mDb = db;
    }

    public void setOnRenameProgramTaskListener(OnRenameProgramTaskListener listener) {
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if(mDb == null) {
            return RESULT_DATABASE_NOT_OPEN;
        }
        mDb.beginTransaction();
        try {
            mDb.execSQL(String.format("ALTER TABLE %s RENAME TO %s;", Utils.escapeSqlString(mProgramName), Utils.escapeSqlString(mNewProgramName)));
        } catch (SQLiteCantOpenDatabaseException e){
            mDb.endTransaction();
            return RESULT_DATABASE_NOT_OPEN;
        } catch(SQLiteException e) {
            mDb.endTransaction();
            return RESULT_INVALID_PROGRAM_NAME;
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        return RESULT_OK;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        if (mListener != null) {
            mListener.onProgramRenamed(integer);
        }
        mListener = null;
        mDb = null;
    }
}
