package com.siemens.currentiterationswitcher.helpers;

import com.ibm.team.process.common.IIteration;
import com.siemens.currentiterationswitcher.mocks.Mockeration;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static com.siemens.currentiterationswitcher.helpers.TimeHelper.delayFromMilitaryTime;
import static com.siemens.currentiterationswitcher.helpers.TimeHelper.isNotCurrent;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TimeHelperTest {
    @Test
    public void testTime() {
        long time1 = 1000;
        long delay1 = delayFromMilitaryTime(time1);
        Assert.assertTrue(delay1 > 0 && delay1 <= 864000);//delay bigger or smaller than a day
        long time3 = 10000;
        long delay3 = delayFromMilitaryTime(time3);
        Assert.assertTrue(delay3 > 0 && delay3 <= 864000);//delay bigger or smaller than a day
        long time2 = 1200;
        long delay2 = delayFromMilitaryTime(time2);
        long diff = delay2 - delay1;
        if (diff < 0)
            diff = diff + 24 * 3600;//in case this test runs between time 1 & 2
        Assert.assertEquals(diff, 2 * 3600);//diff should be 2hours
    }

    @Test
    public void testIsNotCurrent() {
        //5 possibilities(2 null), only one should return false...
        IIteration iteration = new Mockeration();
        Date startDate = new Date(System.currentTimeMillis() - 400);
        Date endDate = new Date(System.currentTimeMillis() - 200);
        iteration.setEndDate(endDate);
        iteration.setStartDate(startDate);


        Boolean bool = isNotCurrent(iteration);
        assertTrue(bool);


        endDate = new Date(System.currentTimeMillis() + 400);
        iteration.setEndDate(endDate);
        bool = isNotCurrent(iteration);
        assertFalse(bool);

        startDate = new Date(System.currentTimeMillis() + 200);
        iteration.setStartDate(startDate);
        bool = isNotCurrent(iteration);
        assertTrue(bool);

        endDate = null;
        iteration.setEndDate(endDate);
        bool = isNotCurrent(iteration);
        assertTrue(bool);

        startDate = null;
        iteration.setStartDate(startDate);
        endDate = new Date(System.currentTimeMillis() + 400);
        iteration.setEndDate(endDate);
        bool = isNotCurrent(iteration);
        assertTrue(bool);
    }
}
