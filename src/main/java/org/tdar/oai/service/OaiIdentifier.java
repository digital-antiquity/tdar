package org.tdar.oai.service;

import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.OAIException;
import org.tdar.core.exception.OaiErrorCode;
import org.tdar.struts.data.oai.OAIRecordType;
import org.tdar.utils.MessageHelper;

public class OaiIdentifier {

    private static final String SEPARATOR = ":";
    private static final String OAI = "oai:";
    private String repositoryNamespaceIdentifier = TdarConfiguration.getInstance().getRepositoryNamespaceIdentifier();
    private OAIRecordType recordType;
    private Long id;

    public OaiIdentifier(String identifier) throws OAIException {
        String[] token = identifier.split(SEPARATOR, 4);
        // First token must = "oai"
        // Second token must = the repository namespace identifier
        if (!token[0].equals("oai") || !token[1].equals(repositoryNamespaceIdentifier)) {
            throw new OAIException(MessageHelper.getInstance().getText("oaiController.identifier_not_part"), OaiErrorCode.ID_DOES_NOT_EXIST);
        }
        // the third token is the type of the record, i.e. "Resource", "Person" or "Institution"
        recordType = OAIRecordType.fromString(token[2]);
        id = Long.valueOf(token[3]);
    }

    public OaiIdentifier(OAIRecordType recordType, Long id) {
        this.id = id;
        this.recordType = recordType;
    }

    public OAIRecordType getRecordType() {
        return recordType;
    }

    public Long getTdarId() {
        return id;
    }

    public String getOaiId() {
        return constructIdentifier(recordType, id);
    }

    public String constructIdentifier(OAIRecordType type, Long numericIdentifier) {
        return OAI + repositoryNamespaceIdentifier + SEPARATOR + type.getName() + SEPARATOR + String.valueOf(numericIdentifier);
    }
}
