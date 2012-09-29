package org.tdar.core.service;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;

@Service
public class OdataService implements Serializable {

    private static final long serialVersionUID = -1988541559642339564L;

    
    @Autowired
    private AuthenticationAndAuthorizationService authService;

    @Autowired
    private DataIntegrationService databaseService;

    @Autowired
    private GenericService genericService;

    public void editDataset(Long personId, Long datasetId,Long dataTableId, Long rowId, Map<?,?> data) {
        
        Person person  = genericService.find(Person.class, personId);
        Dataset dataset = genericService.find(Dataset.class, datasetId);
        
        if (!authService.canEdit(person, dataset)) {
            throw new TdarRecoverableRuntimeException(String.format("user %s cannot edit the dataset %s", person, dataset));
        }
        
        databaseService.editRow(dataset.getDataTableById(dataTableId), rowId, data);
        
    }
    
}
