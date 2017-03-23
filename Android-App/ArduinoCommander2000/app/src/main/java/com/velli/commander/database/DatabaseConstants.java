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



public class DatabaseConstants {

    public static final String KEY_PROGRAM_STEP_TYPE = "TYPE";
    public static final String KEY_PROGRAM_STEP_DEGREES_TO_ROTATE = "DEGREESTOROTATE";
    public static final String KEY_PROGRAM_STEP_DISTANCE_TO_MOVE = "DISTANCETOMOVE";
    public static final String KEY_PROGRAM_STEP_SPEED_TO_SET = "SPEEDTOSET";
    public static final String KEY_PROGRAM_STEP_TIME_TO_DELAY = "TIMETODELAY";
    public static final String KEY_PROGRAM_STEP_OTHER_COMMAND = "OTHERCOMMAND";
    public static final String KEY_PROGRAM_STEP_ROTATION_DIRECTION = "ROTATIONDIRECTION";
    public static final String KEY_PROGRAM_STEP_MOVING_DIRECTION = "MOVINGDIRECTION";
    public static final String KEY_PROGRAM_STEP_TIME_UNIT = "TIMEUNIT";
    public static final String KEY_PROGRAM_STEP_ORDER_IN_PROGRAM = "ORDERINPROGRAM";
    public static final String KEY_ROW_ID = "rowid";

    public static final String columns[] = {
            KEY_PROGRAM_STEP_TYPE,
            KEY_PROGRAM_STEP_DEGREES_TO_ROTATE,
            KEY_PROGRAM_STEP_DISTANCE_TO_MOVE,
            KEY_PROGRAM_STEP_SPEED_TO_SET,
            KEY_PROGRAM_STEP_TIME_TO_DELAY,
            KEY_PROGRAM_STEP_OTHER_COMMAND,
            KEY_PROGRAM_STEP_ROTATION_DIRECTION,
            KEY_PROGRAM_STEP_MOVING_DIRECTION,
            KEY_PROGRAM_STEP_TIME_UNIT,
            KEY_PROGRAM_STEP_ORDER_IN_PROGRAM
    };

    public static final String columnSelection =
            KEY_PROGRAM_STEP_TYPE
                    + ", " + KEY_PROGRAM_STEP_DEGREES_TO_ROTATE
                    + ", " + KEY_PROGRAM_STEP_DISTANCE_TO_MOVE
                    + ", " + KEY_PROGRAM_STEP_SPEED_TO_SET
                    + ", " + KEY_PROGRAM_STEP_TIME_TO_DELAY
                    + ", " + KEY_PROGRAM_STEP_OTHER_COMMAND
                    + ", " + KEY_PROGRAM_STEP_ROTATION_DIRECTION
                    + ", " + KEY_PROGRAM_STEP_MOVING_DIRECTION
                    + ", " + KEY_PROGRAM_STEP_TIME_UNIT
                    + ", " + KEY_ROW_ID
                    + ", " + KEY_PROGRAM_STEP_ORDER_IN_PROGRAM;


    public static final String CREATE_PROGRAM_TABLE_KEYS =  " ("
            + KEY_PROGRAM_STEP_TYPE + " INTEGER, "
            + KEY_PROGRAM_STEP_DEGREES_TO_ROTATE + " INTEGER, "
            + KEY_PROGRAM_STEP_DISTANCE_TO_MOVE + " INTEGER, "
            + KEY_PROGRAM_STEP_SPEED_TO_SET + " INTEGER, "
            + KEY_PROGRAM_STEP_TIME_TO_DELAY + " TEXT, "
            + KEY_PROGRAM_STEP_OTHER_COMMAND + " TEXT, "
            + KEY_PROGRAM_STEP_ROTATION_DIRECTION + " INTEGER, "
            + KEY_PROGRAM_STEP_MOVING_DIRECTION + " INTEGER, "
            + KEY_PROGRAM_STEP_TIME_UNIT + " INTEGER, "
            + KEY_PROGRAM_STEP_ORDER_IN_PROGRAM + " INTEGER" + ")";

    public static String getProgramTable(String programName) {
        return "CREATE TABLE " + programName + CREATE_PROGRAM_TABLE_KEYS;
    }

}
