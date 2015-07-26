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

        // gs -dPDFA -dBATCH -dUseCIEColor -sProcessColorModel=DeviceCMYK -sDEVICE=pdfwrite -dPDFACompatibilityPolicy=1 -sOutputFile=$file.pdfa $file
        CommandLine cmdLine = new CommandLine("gs");
        cmdLine.addArgument("-dPDFA");
        cmdLine.addArgument("-dBATCH");
        cmdLine.addArgument("-dNOPAUSE");
        cmdLine.addArgument("-sProcessColorModel=DeviceCMYK");
        cmdLine.addArgument("-sDEVICE=pdfwrite");
        cmdLine.addArgument("-dPDFACompatibilityPolicy=1");
        cmdLine.addArgument("-sOutputFile=${outFile}");
        cmdLine.addArgument("${file}");
        HashMap<String, File> map = new HashMap<>();
        map.put("file", new File("invoice.pdf"));
        map.put("outFile", new File("invoice.pdfa"));
        cmdLine.setSubstitutionMap(map);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, resultHandler);

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        resultHandler.waitFor();
        @SuppressWarnings("unused")
        int exitValue = resultHandler.getExitValue();
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
