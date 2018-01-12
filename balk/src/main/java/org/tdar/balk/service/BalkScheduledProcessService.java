package org.tdar.balk.service;

public interface BalkScheduledProcessService {

    void cronUploadTdar();

    void cronPollingStatsQueue();

}