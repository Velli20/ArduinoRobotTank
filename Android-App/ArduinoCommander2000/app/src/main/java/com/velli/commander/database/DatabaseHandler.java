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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.velli.commander.App;
import com.velli.commander.Utils;
import com.velli.commander.collections.ListItemProgram;
import com.velli.commander.interfaces.OnDatabaseChangedListener;
import com.velli.commander.interfaces.OnGetProgramNamesCallback;
import com.velli.commander.interfaces.OnGetProgramTaskCallback;
import com.velli.commander.interfaces.OnRenameProgramTaskListener;
import com.velli.commander.interfaces.OnWriteProgramTaskResultListener;

import java.util.ArrayList;



public class DatabaseHandler extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "programs.db";
    public static final int DATABASE_VERSION = 1;

    private static DatabaseHandler sInstance;
    public SQLiteDatabase mDb;
    private ArrayList<OnDatabaseChangedListener> mDatabaseCallbacks = new ArrayList<>();

    public static DatabaseHandler getInstance(){
        if(sInstance == null) sInstance = getSync();
        return sInstance;
    }

    private static synchronized DatabaseHandler getSync() {
        if(sInstance == null) sInstance = new DatabaseHandler(App.get());
        return sInstance;
    }

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradeTo = oldVersion + 1;

        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 1:
                    break;
            }

            upgradeTo++;
        }
    }

    public void  registerOnDatabaseChangedListener(OnDatabaseChangedListener l) {
        if(mDatabaseCallbacks != null && l != null && !mDatabaseCallbacks.contains(l)) {
            mDatabaseCallbacks.add(l);
        }
    }

    public void  unregisterOnDatabaseChangedListener(OnDatabaseChangedListener l) {
        if(mDatabaseCallbacks != null && l != null) {
            mDatabaseCallbacks.remove(l);
        }
    }

    private void notifyCallbacks() {
        if(mDatabaseCallbacks == null) {
            return;
        }
        for(OnDatabaseChangedListener callback : mDatabaseCallbacks) {
            callback.onDatabaseChanged();
        }
    }

    public boolean isDatabaseOpen(){
        return mDb != null && mDb.isOpen();
    }

    public void openDatabase(){
        mDb = getWritableDatabase();

        if(mDb.getVersion() < DATABASE_VERSION){
            onUpgrade(mDb, mDb.getVersion(), DATABASE_VERSION);
        }
    }

    public void closeDatabase(){
        if(isDatabaseOpen()){
            mDb.close();
            mDb = null;
        }
    }

    public void writeProgram(ListItemProgram program) {
        if(!isDatabaseOpen()) {
            openDatabase();
        }

        WriteProgramTask task = new WriteProgramTask(mDb, program);
        task.setOnWriteProgramTaskResultListener(new OnWriteProgramTaskResultListener() {
            @Override
            public void onWriteProgramTaskResult(int resultCode) {
                closeDatabase();
                notifyCallbacks();

            }
        });
        task.execute();
    }

    public void getProgram(String programName, final OnGetProgramTaskCallback callback) {
        if(!isDatabaseOpen()) {
            openDatabase();
        }

        GetProgramTask task = new GetProgramTask(mDb, programName);
        task.setOnGetProgramTaskCallback(new OnGetProgramTaskCallback() {
            @Override
            public void onGetProgramTaskCallback(ListItemProgram program) {
                callback.onGetProgramTaskCallback(program);
                closeDatabase();
            }
        });
        task.execute();
    }

    public void getProgramNames(final OnGetProgramNamesCallback callback) {
        if(!isDatabaseOpen()) {
            openDatabase();
        }

        GetAllProgramNamesTask task = new GetAllProgramNamesTask(mDb);
        task.setOnGetProgramNamesCallback(new OnGetProgramNamesCallback() {
            @Override
            public void onGetProgramNamesCallback(ArrayList<String> programNames) {
                callback.onGetProgramNamesCallback(programNames);
                closeDatabase();
            }
        });
        task.execute();
    }

    public void deleteProgram(String programName) {
        if(!isDatabaseOpen()) {
            openDatabase();
        }
        mDb.beginTransaction();
        mDb.execSQL("DROP TABLE IF EXISTS " + Utils.escapeSqlString(programName));
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        closeDatabase();
        notifyCallbacks();
    }

    public void renameProgram(String programName, String newName, final OnRenameProgramTaskListener listener) {
        if(!isDatabaseOpen()) {
            openDatabase();
        }
        RenameProgramTask task = new RenameProgramTask(mDb, programName, newName);
        task.setOnRenameProgramTaskListener(new OnRenameProgramTaskListener() {

            @Override
            public void onProgramRenamed(int resultCode) {
                if(listener != null) {
                    listener.onProgramRenamed(resultCode);
                }
                closeDatabase();
                notifyCallbacks();
            }
        });
        task.execute();

    }
}
