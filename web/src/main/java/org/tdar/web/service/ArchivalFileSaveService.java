package org.tdar.web.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.fileupload.FileUploadException;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;

public interface ArchivalFileSaveService {

    void saveArchivalVersion(Resource resource, InformationResourceFile informationResourceFile, File file, String fileFileName, TdarUser tdarUser) throws IOException, FileUploadException;

}
