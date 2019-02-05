/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: StoppableRunnable.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */

package com.otipass.tools;

public abstract class StoppableRunnable implements Runnable {

    private volatile boolean mIsStopped = false;

    public abstract void stoppableRun();

    public void run() {
        setStopped(false);
        while(!mIsStopped) {
            stoppableRun();
            stop();
        }
    }

    public boolean isStopped() {
        return mIsStopped;
    }

    private void setStopped(boolean isStop) {    
        if (mIsStopped != isStop)
            mIsStopped = isStop;
    }

    public void stop() {
        setStopped(true);
    }
}