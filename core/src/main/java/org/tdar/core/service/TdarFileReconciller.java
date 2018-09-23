package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.fileprocessing.workflows.RequiredOptionalPairs;

public class TdarFileReconciller {
    
    private HashMap<String, Set<RequiredOptionalPairs>> extensionMap;

    public TdarFileReconciller(Collection<RequiredOptionalPairs> pairs) {
        Set<RequiredOptionalPairs> complex = new HashSet<>();
        Set<RequiredOptionalPairs> standard = new HashSet<>();
        // complex ones have more than one file
        extensionMap = new HashMap<>();
        for (RequiredOptionalPairs pair : pairs) {
            if (pair.requiresSidecar()) {
                complex.add(pair);
            } else {
                standard.add(pair);
            }
            // create a map to find the "simple" extensions -- they only map to one Pair
            pair.getAllExtensions().forEach(ext -> {
                Set<RequiredOptionalPairs> match = extensionMap.getOrDefault(ext, new HashSet<>());
                match.add(pair);
                extensionMap.put(ext, match);
            });
        }
        
    }

    public void reconcile(Collection<TdarFile> files) {
        
        Map<String, TdarFile> fileMap = new HashMap<>();
        files.forEach(file -> {
            fileMap.put(file.getName().toLowerCase(), file);
        });
        
        
        // find the simple extensions
        Map<String,Set<RequiredOptionalPairs>> unambiguous = new HashMap<>();
        extensionMap.entrySet().forEach(entry -> {
            if (entry.getValue().size() == 1) {
                unambiguous.put(entry.getKey(), entry.getValue());
            }
        });
        
        Iterator<TdarFile> fileIterator = files.iterator();
        Map<TdarFile,RequiredOptionalPairs> result = new HashMap<>();
        Set<String> skip = new HashSet<>();
        
        // iterate over all files
        while (fileIterator.hasNext()) {
            TdarFile file = fileIterator.next();
            // if we've seen and dealt with it, skip it
            if (skip.contains(file.getName().toLowerCase())) {
                fileIterator.remove();
                continue;
            }
            String ext = file.getExtension();
            List<TdarFile> errors = new ArrayList<>();

            // for the unambigous file groups, that is, the ones that don't overlap with other pairs...
            if (unambiguous.containsKey(ext)) {
                RequiredOptionalPairs pair = result.put(file, unambiguous.get(ext).iterator().next());
                fileIterator.remove();
                
                // find the primary file
                String base = StringUtils.stripEnd(file.getName(), ext).toLowerCase();
                TdarFile primary = null;
                if (file.getExtension().equals(pair.getPrimaryExtension())) {
                    primary = file;
                } else {
                    primary = fileMap.get(base + pair.getPrimaryExtension());
                }

                // error out if we don't have the primary file
                if (primary == null) {
                    errors.add(file);
                    continue;
                }
                skip.add(primary.getName());
                
                // go through and get all of the other files
                for (String ext_ : pair.getAllExtensions()) {
                    if(!ext.equals(ext_) && !pair.getPrimaryExtension().equals(ext_)) {
                        TdarFile file2 = fileMap.get(base + ext_);
                        if (file2 != null) {
                            primary.getParts().add(file2);
                            skip.add(file2.getName());
                        }
                    }
                }
                // iterate over the other parts of the required optional pair and cleanup / remove
            }
        }
    }
}
