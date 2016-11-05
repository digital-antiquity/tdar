package org.tdar.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessList {
    int PARENT_ID = 0;
    int PROCESS_ID = 1;
    int COMMAND = 2;
    private static final transient Logger logger = LoggerFactory.getLogger(ProcessList.class);

    public static Set<Long> listProcesses(String parentName) {

        Map<Long, String> processMap = new HashMap<>();
        Map<Long, Long> parentMap = new HashMap<>();
        List<Long> matches = new ArrayList<>();
        try {
            String line;
            Process p = Runtime.getRuntime().exec("ps axo ppid,pid,command");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                // System.out.println(line); // <-- Parse data here.
                line = line.trim();
                if (line.contains("PPID")) {
                    continue;
                }
                Long ppid = Long.parseLong(StringUtils.substringBefore(line, " "));
                String line_ = StringUtils.substringAfter(line.trim(), " ");
                line_ = line_.trim();
                Long pid = Long.parseLong(StringUtils.substringBefore(line_, " "));
                logger.trace("{}| {} ", ppid, pid);
                line_ = StringUtils.substringAfter(line_.trim(), " ");
                String command = StringUtils.substringBefore(line_, " ");
                processMap.put(pid, command);
                parentMap.put(pid, ppid);
                if (command.contains(parentName)) {
                    matches.add(pid);
                }
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }

        Set<Long> toKill = new HashSet<>(matches);
        int num = toKill.size();
        logger.trace("matches: {}", matches);
        while (true) {
            toKill.addAll(findChildren(parentMap, toKill));
            if (toKill.size() == num) {
                break;
            }
            num = toKill.size();
        }
        return toKill;
    }

    private static Set<Long> findChildren(Map<Long, Long> parentMap, Set<Long> toKill) {
        Set<Long> toReturn = new HashSet<>();
        for (Map.Entry<Long, Long> entry : parentMap.entrySet()) {
            if (toKill.contains(entry.getValue())) {
                toReturn.add(entry.getKey());
            }
        }
        return toReturn;
    }

    public static void killProcesses(Set<Long> toKill) {
        for (Long id : toKill) {
            try {
                String line;
                logger.debug("killing pid: {}", id);
                Process p = Runtime.getRuntime().exec("kill -9 " + id);
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((line = input.readLine()) != null) {
                }
                input.close();
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }
}
