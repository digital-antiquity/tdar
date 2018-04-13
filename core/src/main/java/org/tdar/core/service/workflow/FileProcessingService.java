package org.tdar.core.service.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.dao.FileProcessingDao;

@Service
public class FileProcessingService {

    @Autowired
    private FileProcessingDao fileProcessingDao;
    
    @Transactional(readOnly=true)
    public List<TdarFile> findFilesToValidate() {
        return fileProcessingDao.findFiles(ImportFileStatus.UPLOADED);
    }
    
    @Transactional(readOnly=true)
    public List<TdarFile> findFilesToProcess() {
        return fileProcessingDao.findFiles(ImportFileStatus.VALIDATED);
    }

    @Transactional(readOnly=false) 
    public void validateFiles(List<TdarFile> files) {
        for (TdarFile file : files) {
            validateFile(file);
        }
    }

    @Transactional(readOnly=false) 
    public void validateFile(TdarFile file) {
        
    }
}
