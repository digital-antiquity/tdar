package org.tdar.filestore.tasks;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.tdar.filestore.tasks.Task.AbstractTask;

public class CreatePdfATask extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 325840072970181427L;

    @Override
    public void run() throws Exception {

        
        CommandLine cmdLine = new CommandLine("AcroRd32.exe");
        cmdLine.addArgument("/p");
        cmdLine.addArgument("/h");
        cmdLine.addArgument("${file}");
        HashMap map = new HashMap();
        map.put("file", new File("invoice.pdf"));
        commandLine.setSubstitutionMap(map);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, resultHandler);

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        resultHandler.waitFor();
        int exitValue = resultHandler.getExitValue();
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
