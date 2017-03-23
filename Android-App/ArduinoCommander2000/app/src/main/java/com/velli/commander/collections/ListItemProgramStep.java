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



public class ListItemProgramStep {
    public static final int PROGRAM_ROTATION_DIRECTION_CLOCKWISE = 0;
    public static final int PROGRAM_ROTATION_DIRECTION_COUNTERCLOCKWISE = 1;
    public static final int PROGRAM_MOVING_DIRECTION_FORWARD = 0;
    public static final int PROGRAM_MOVING_DIRECTION_BACKWARD = 1;
    
    public static final int PROGRAM_DELAY_UNIT_MS = 0;
    public static final int PROGRAM_DELAY_UNIT_S = 1;
    public static final int PROGRAM_DELAY_UNIT_MIN = 2;
    
    public static final int PROGRAM_STEP_ROTATE= 0;
    public static final int PROGRAM_STEP_MOVE = 1;
    public static final int PROGRAM_STEP_SET_SPEED = 2;
    public static final int PROGRAM_STEP_DELAY = 3;
    public static final int PROGRAM_STEP_OTHER = 4;

    private int mStepType;

    private int mDegreesToRotate;
    private int mDistanceToMove;
    private int mSpeed;
    private long mTimeToDelay;
    private String mOtherCommand;

    private int mRotationDirection;
    private int mMovingDirection;  
    private int mDelayUnit;

    private int mPositionInProgramList;

    public ListItemProgramStep() {

    }

    public ListItemProgramStep(int step) {
        mStepType = step;
    }

    public void setStepType(int stepType) {
        mStepType = stepType;
    }

    public int getStepType() {
        return mStepType;
    }

    public void setDegreesToRotate(int degrees, int direction) {
        mDegreesToRotate = degrees;
        mRotationDirection = direction;
    }

    public void setDistanceToMove(int distance, int direction) {
        mDistanceToMove = distance;
        mMovingDirection = direction;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    public void setTimeToDelay(long millis, int unit) {
        mTimeToDelay = millis;
        mDelayUnit = unit;
    }

    public void setOtherCommand(String command) {
        mOtherCommand = command;
    }

    public void setPositionInProgramList(int position) {
        mPositionInProgramList = position;
    }

    public int getDegreesToRotate() {
        return mDegreesToRotate;
    }

    public int getDistanceToMove() {
        return mDistanceToMove;
    }

    public int getSpeedToSet() {
        return mSpeed;
    }

    public long getTimeToDelay() {
        return mTimeToDelay;
    }

    public int getRotationDirection() {
        return mRotationDirection;
    }

    public int getMovingDirection() {
        return mMovingDirection;
    }
    
    public int getDelayUnit() {
        return mDelayUnit;
    }

    public String getOtherCommand() {
        return mOtherCommand;
    }

    public int getPositionInProgramList() { return mPositionInProgramList; };


}
