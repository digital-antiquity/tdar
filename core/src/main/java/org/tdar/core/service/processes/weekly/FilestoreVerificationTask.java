package org.tdar.core.service.processes.weekly;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;

@Component
@Scope("prototype")
public class FilestoreVerificationTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Filestore filestore;
    private List<FileStoreFileProxy> proxyList = new ArrayList<>();
    private List<FileStoreFileProxy> tainted = new ArrayList<>();
    private List<FileStoreFileProxy> missing = new ArrayList<>();

    private WeeklyFilestoreLoggingProcess parent;

    public FilestoreVerificationTask(Filestore filestore, WeeklyFilestoreLoggingProcess weeklyFilestoreLoggingProcess) {
        this.filestore = filestore;
        this.parent = weeklyFilestoreLoggingProcess;
    }

    public boolean setup(List<FileStoreFileProxy> proxyList) {
        this.proxyList = proxyList;
        if (CollectionUtils.isNotEmpty(proxyList)) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        boolean running = true;
        int slept = 10;
        while (running) {
            verify();
            parent.updateCounts(proxyList, tainted, missing);
            cleanup();
            boolean setup = setup(parent.createThreadBatch());
            if (setup == false) {
                try {
                    logger.debug("sleeeping #{}, maybe there will be more work to do???", slept);
                    Thread.sleep(1000);
                    slept--;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (slept == 0) {
                    running = false;
                }
            } else {
                // reset
                slept = 10;
            }
        }
    }

    private void verify() {
        for (FileStoreFileProxy proxy : proxyList) {
            try {
                if (!filestore.verifyFile(FilestoreObjectType.RESOURCE, proxy)) {
                    tainted.add(proxy);
                }
            } catch (FileNotFoundException e) {
                missing.add(proxy);
                logger.debug("file not found ", e);
            } catch (Exception e) {
                tainted.add(proxy);
                logger.debug("other error ", e);
            }
        }
    }

    public void cleanup() {
        proxyList.clear();
        tainted.clear();
        missing.clear();
    }
}
