package com.siemens.currentiterationswitcher.helpers;

import com.ibm.team.process.common.IIteration;

import java.util.TimeZone;

public class TimeHelper {

    //works out the delay in seconds from system time to (start) time
    public static long delayFromMilitaryTime(long time) {
        long systemDate = System.currentTimeMillis();
        int offsetFromUTC = TimeZone.getDefault().getOffset(systemDate);//diff between utc and local timezone + daylightsavings
        long systemTime = (systemDate + offsetFromUTC) % (1000 * 3600 * 24); //Get just the time
        systemTime = systemTime / 1000; //get rid of millisec
        time = (time / 100 * 60 + time % 100) * 60;//transforms hhmm to sssssssss
        long delay = time - systemTime;
        if (delay < 0) {
            delay = delay + 24 * 3600; //same time in future instead of past
        }

        return delay;
    }

    //returns true if the iteration isn't current, e.g. has to be changed
    public static boolean isNotCurrent(IIteration iteration) {
        return iteration.getStartDate() == null || iteration.getEndDate() == null
                || iteration.getStartDate().getTime() > System.currentTimeMillis()
                || iteration.getEndDate().getTime() < System.currentTimeMillis();
    }
}
