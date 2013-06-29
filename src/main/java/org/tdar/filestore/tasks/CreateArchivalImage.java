package org.tdar.filestore.tasks;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.tdar.filestore.tasks.Task.AbstractTask;

public class CreateArchivalImage extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 325840072970181427L;

    @Override
    public void run() throws Exception {

        /*
         * #!/usr/bin/perl
         * 
         * foreach my $file (@ARGV) {
         * print $file . "\r\n";
         * $file =~ s/\r\n//ig;
         * my $prefix = substr($file,0,rindex($file,"."));
         * `convert $file -compress None -quality 100 tiff/$prefix.archival.tif`;
         * `convert $file -compress Lzw -quality 100 lzw/$prefix.archival-lzw.tif`;
         * `convert $file -quality 100 png/$prefix.archival-compressed.png`;
         * # `cp $file $file.bzip.jpg;bzip2 $file.bzip.jpg`;
         * # `cp $file $file.zip.jpg;zip $file.bzip.jpg`;
         * # `convert $file -define jp2:numrlvls=6 -define jp2:tilewidth=1024 -define jp2:tileheight=1024 -define jp2:rate=1.0 -define jp2:lazy -define
         * jp2:prg=rlcp -define jp2:ilyrrates='0.015625,0.01858,0.0221,0.025,0.03125,0.03716,0.04419,0.05,0.0625,
         * 0.075,0.088,0.1,0.125,0.15,0.18,0.21,0.25,0.3,0.35,0.4,0.5,0.6,0.7,0.84' -define jp2:mode=int $prefix.jp2`
         * 
         * }
         */
        CommandLine cmdLine = new CommandLine("gs");
        cmdLine.addArgument("-dPDFA");
        cmdLine.addArgument("-dBATCH");
        cmdLine.addArgument("-dNOPAUSE");
        cmdLine.addArgument("-sProcessColorModel=DeviceCMYK");
        cmdLine.addArgument("-sDEVICE=pdfwrite");
        cmdLine.addArgument("-dPDFACompatibilityPolicy=1");
        cmdLine.addArgument("-sOutputFile=${outFile}");
        cmdLine.addArgument("${file}");
        HashMap map = new HashMap();
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
        int exitValue = resultHandler.getExitValue();
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
