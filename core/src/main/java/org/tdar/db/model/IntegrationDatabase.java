package org.tdar.db.model;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.service.integration.IntegrationContext;
import org.tdar.core.service.integration.ModernIntegrationDataResult;
import org.tdar.db.Database;

import com.opensymphony.xwork2.TextProvider;

/**
 * A base class for target Databases that can be written to via a
 * DatabaseConverter.
 * 
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public interface IntegrationDatabase extends Database, TargetDatabase {

    @Transactional(value = "tdarDataTx", readOnly = false)
    ModernIntegrationDataResult generateIntegrationResult(IntegrationContext proxy, String rawIntegration, TextProvider provider);

}
