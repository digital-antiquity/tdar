package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * $Id$
 * 
 * DAO access for DataTableColumnS.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
public class DataTableColumnDao extends Dao.HibernateBase<DataTableColumn> {

    public DataTableColumnDao() {
        super(DataTableColumn.class);
    }

    public List<CodingRule> findMappedCodingRules(DataTableColumn column, List<String> valuesToMatch) {
        if ((column == null) || CollectionUtils.isEmpty(valuesToMatch)) {
            getLogger().debug("No mapped coding rules available for column {} and values {}", column, valuesToMatch);
            return Collections.emptyList();
        }
        return findMappedCodingRules(column.getDefaultCodingSheet(), valuesToMatch);
    }

    @SuppressWarnings("unchecked")
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH",
            justification = "ignoring null derefernece because findbugs is not paying attention to the null-check above")
    public List<CodingRule> findMappedCodingRules(CodingSheet sheet, List<String> valuesToMatch) {
        if (PersistableUtils.isNullOrTransient(sheet) || CollectionUtils.isEmpty(valuesToMatch)) {
            getLogger().debug("no mapped coding rules available for sheet {} and values {}", sheet, valuesToMatch);
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_MAPPED_CODING_RULES);
        query.setParameter("codingSheetId", sheet.getId());
        query.setParameter("valuesToMatch", valuesToMatch);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<DataTableColumn> findOntologyMappedColumns(Dataset dataset) {
        if (dataset == null) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY);
        query.setParameter("datasetId", dataset.getId());
        return query.getResultList();
    }

    public CodingSheet setupGeneratedCodingSheet(DataTableColumn column, Dataset dataset, TdarUser user, TextProvider provider, Ontology ontology) {
        CodingSheet codingSheet = new CodingSheet();
        codingSheet.markUpdated(user);
        codingSheet
                .setTitle(provider.getText("dataIntegrationService.generated_coding_sheet_title", Arrays.asList(column.getDisplayName(), dataset.getTitle())));
        if (ontology != null) {
            codingSheet.setCategoryVariable(ontology.getCategoryVariable());
            codingSheet.setDefaultOntology(ontology);
        }
        codingSheet.setDescription(provider.getText(
                "dataIntegrationService.generated_coding_sheet_description",
                Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym(), column, dataset.getTitle(),
                        dataset.getId(), codingSheet.getDateCreated())));

        codingSheet.setDate(Calendar.getInstance().get(Calendar.YEAR));
        codingSheet.setGenerated(true);
        codingSheet.setAccount(dataset.getAccount());
        save(codingSheet);
        if (dataset != null) {
            codingSheet.setProject(dataset.getProject());
            for (ResourceCollection collection : dataset.getResourceCollections()) {
                if (collection.isShared()) {
                    codingSheet.getResourceCollections().add(collection);
                }
            }
        }
        String msg = String.format("genearting coding sheet from datatable column %s (%s) datasetId: %s", column, column.getId(), dataset.getId());
        ResourceRevisionLog rrl = new ResourceRevisionLog(msg, codingSheet, user, RevisionLogType.CREATE);
        codingSheet.getResourceRevisionLog().add(rrl);
        saveOrUpdate(codingSheet);
        saveOrUpdate(rrl);
        return codingSheet;
    }

}
