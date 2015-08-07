package org.tdar.oai.bean;

import org.tdar.oai.bean.generated.oai._2_0.OAIPMHerrorcodeType;
import org.tdar.oai.exception.OAIException;
import org.tdar.oai.service.OaiPmhConfiguration;
import org.tdar.utils.MessageHelper;

public class OaiIdentifier {

    private static final String SEPARATOR = ":";
    private static final String OAI = "oai:";
    private String repositoryNamespaceIdentifier = OaiPmhConfiguration.getInstance().getRepositoryNamespaceIdentifier();
    private OAIRecordType recordType;
    private Long id;

    /**
     * Simple bean model to help manage the creation of and validation of identifiers.
     * @param identifier
     * @throws OAIException
     */
    public OaiIdentifier(String identifier) throws OAIException {
        String[] token = identifier.split(SEPARATOR, 4);
        // First token must = "oai"
        // Second token must = the repository namespace identifier
        if (!token[0].equals("oai") || !token[1].equals(repositoryNamespaceIdentifier)) {
            throw new OAIException(MessageHelper.getInstance().getText("oaiController.identifier_not_part"), OAIPMHerrorcodeType.ID_DOES_NOT_EXIST);
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
