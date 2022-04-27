package org.tdar;

import java.io.File;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.VersionType;

@ContextConfiguration(classes = BaseConfiguration.class)
@SuppressWarnings("rawtypes")
@ActiveProfiles(profiles = { "test" })
public abstract class AbstractSimpleIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests  {

    protected Filestore filestore = TdarConfiguration.getInstance().getFilestore();

    protected PlatformTransactionManager transactionManager;
    private TransactionCallback verifyTransactionCallback;
    private TransactionTemplate transactionTemplate;
    
    public static final String SPITAL_DB_NAME = TestConstants.SPITAL_DB_NAME;
    protected static final String PATH = TestConstants.TEST_DATA_INTEGRATION_DIR;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    
    @Rule
    public TestWatcher failWatcher = new TestWatcher() {

        @Override
        protected void failed(Throwable e, Description description) {
            AbstractSimpleIntegrationTest.this.onFail(e, description);
        }
    };

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            String fmt = " ***   RUNNING TEST: {}.{}() ***";
            logger.info(fmt, description.getTestClass().getSimpleName(), description.getMethodName());
        }

        @Override
        protected void finished(Description description) {
            String fmt = " ***   COMPLETED TEST: {}.{}() ***";
            logger.info(fmt, description.getTestClass().getSimpleName(), description.getMethodName());
        }
    };

    public String getTestFilePath() {
        return PATH;
    }

    // Called when your test fails. Did I say "when"? I meant "if".
    public void onFail(Throwable e, Description description) {
    }

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    protected <V> V runInNewTransaction(TransactionCallback<V> action) {
        logger.debug("starting new transaction");
        return transactionTemplate.execute(action);
    }

    protected void runInNewTransactionWithoutResult(TransactionCallback<Object> action) {
        runInNewTransaction(action);
    }

    @AfterTransaction
    @SuppressWarnings("unchecked")
    public void verifyTransactionCallback() {
        if (verifyTransactionCallback != null) {
            runInNewTransaction(verifyTransactionCallback);
        }
    }

    public TransactionCallback getVerifyTransactionCallback() {
        return verifyTransactionCallback;
    }

    public <T> void setVerifyTransactionCallback(TransactionCallback<T> verifyTransactionCallback) {
        this.verifyTransactionCallback = verifyTransactionCallback;
    }
    

    protected FileStoreFile makeFileStoreFile(File name, long id) throws IOException {
        long infoId = (long) (Math.random() * 10000);
        FileStoreFile version = new FileStoreFile(FilestoreObjectType.RESOURCE, VersionType.UPLOADED, name.getName(), 1, infoId, 123L, 1L);
        version.setId(id);
        filestore.store(FilestoreObjectType.RESOURCE, name, version);
        version.setTransientFile(name);
        return version;
    }

}
