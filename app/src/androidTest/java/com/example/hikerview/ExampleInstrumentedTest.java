package com.example.hikerview;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(JUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.hiker.hikerview", appContext.getPackageName());
    }


    @Test
    public void testString() {
        String s1 = "1111";
        String s2 = s1;
        s2 = "2222";
//        assertEquals(s1, s2);
        System.out.println("s1=" + s1);
        System.out.println("s2=" + s2);
    }

    @Test
    public void testInt() {
        int size = 60;
        int maxCount = 16;
        int batch = size / maxCount + 1;
        for (int i = 0; i < batch; i++) {
            int start = i * maxCount;
            int end = (i + 1) * maxCount;
            if (end > size) {
                end = size;
            }
            System.out.println("start=" + start + ", end=" + end);
        }
    }
}
