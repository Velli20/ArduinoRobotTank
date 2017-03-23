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

import com.velli.commander.interfaces.OnGetProgramNamesCallback;

import java.util.ArrayList;



public class GetAllProgramNamesTask extends AsyncTask<Void, Void, ArrayList<String>> {
    private SQLiteDatabase mDb;
    private OnGetProgramNamesCallback mListener;

    public GetAllProgramNamesTask(SQLiteDatabase db) {
        mDb = db;
    }

    public void setOnGetProgramNamesCallback(OnGetProgramNamesCallback callback) {
        mListener = callback;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        if(mDb == null) {
            return null;
        }
        mDb.beginTransaction();

        ArrayList<String> names = new ArrayList<>();
        Cursor cursor;

        try {
            cursor = mDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        } catch (SQLiteException e) {
            mDb.endTransaction();
            return null;
        }

        if(cursor != null && cursor.moveToPosition(1)) {
            do {
                names.add(cursor.getString(cursor.getColumnIndex("name")));
            } while(cursor.moveToNext());
        }

        if(cursor != null) {
            cursor.close();
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        return names;
    }

    @Override
    protected void onPostExecute(ArrayList<String> names) {
        super.onPostExecute(names);

        if(mListener != null) {
            mListener.onGetProgramNamesCallback(names);
            mListener = null;
        }
        mDb = null;
    }
}
