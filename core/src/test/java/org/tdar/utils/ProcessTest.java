package org.tdar.utils;

import org.junit.Test;

public class ProcessTest {

    @Test
    public void testProcessList() {
        ProcessList list = new ProcessList();
        System.out.println(list.listProcesses("java"));

    }
}
