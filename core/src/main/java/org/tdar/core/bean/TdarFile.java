package org.tdar.core.bean;

import java.util.List;

import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;

public class TdarFile extends AbstractPersistable {

    private static final long serialVersionUID = 8203692812833995820L;

    private String filename;
    private String displayFilename;
    private BillingAccount account;
    private String localPath;
    private Long fileSize;
    private String extension;
    private ImportFileStatus status;
    private List<String> fileIssues;

    private TdarFile parentFile;

    private ResourceType targetFileType;
    private Resource resource;a
}
