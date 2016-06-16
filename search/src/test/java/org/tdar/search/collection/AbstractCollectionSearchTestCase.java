package org.tdar.search.collection;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.search.QuietIndexReciever;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.query.CollectionSearchService;

public abstract class AbstractCollectionSearchTestCase extends AbstractWithIndexIntegrationTestCase {

    @Override
    public void reindex() {
        searchIndexService.purgeAll(LookupSource.COLLECTION);
        searchIndexService.indexAll(new QuietIndexReciever(), Arrays.asList( LookupSource.COLLECTION), getAdminUser());
    };

    @Autowired
    CollectionSearchService collectionSearchService;

    String[] collectionNames = new String[] { "Kalaupapa National Historical Park, Hawaii", "Kaloko-Honokohau National Historical Park, Hawaii", "Kapsul",
            "KBP Artifact Photographs", "KBP Field Notes", "KBP Level Maps", "KBP Maps", "KBP Profiles", "KBP Reports", "KBP Site Photographs",
            "Kharimkotan 1", "Kienuka", "Kintigh - Carp Fauna Coding Sheets", "Kintigh - Cibola Excavation", "Kintigh - Cibola Research",
            "Kintigh - Cibola Survey Projects", "Kintigh - Context Ontologies", "Kintigh - Fauna Ontologies", "Kintigh - HARP Coding Sheets",
            "Kintigh - Quantitative and Formal Methods Class - Assignments & Data", "Kleis", "Klinko", "Kokina 1", "Kompaneyskyy 1",
            "Kuril Biocomplexity Research", "Kuybyshevskaya 1",
            "Spielmann/Kintigh - Fauna Ontologies - Current", "Australian Journal of Historical Archaeology Archive" };

}
