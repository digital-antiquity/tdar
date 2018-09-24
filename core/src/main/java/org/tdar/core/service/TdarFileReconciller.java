package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.fileprocessing.workflows.RequiredOptionalPairs;

public class TdarFileReconciller {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private HashMap<String, Set<RequiredOptionalPairs>> extensionMap;
    private Map<String, Set<RequiredOptionalPairs>> unambiguous = new HashMap<>();
    private Map<String, Set<RequiredOptionalPairs>> ambiguous = new HashMap<>();
    private Set<RequiredOptionalPairs> simple = new HashSet<>();

    public TdarFileReconciller(Collection<RequiredOptionalPairs> pairs) {
        Set<RequiredOptionalPairs> complex = new HashSet<>();
        Set<RequiredOptionalPairs> standard = new HashSet<>();
        // complex ones have more than one file
        extensionMap = new HashMap<>();
        for (RequiredOptionalPairs pair : pairs) {
            if (pair.requiresSidecar()) {
                complex.add(pair);
            } else {
                simple.add(pair);
                standard.add(pair);
            }
            // create a map to find the "simple" extensions -- they only map to one Pair
            pair.getAllExtensions().forEach(ext -> {
                Set<RequiredOptionalPairs> match = extensionMap.getOrDefault(ext, new HashSet<>());
                match.add(pair);
                extensionMap.put(ext, match);
            });
        }
        // find the simple extensions
        extensionMap.entrySet().forEach(entry -> {
            if (entry.getValue().size() == 1) {
                unambiguous.put(entry.getKey(), entry.getValue());
            } else {
                ambiguous.put(entry.getKey(), entry.getValue());
            }
        });

    }

    public Map<TdarFile, RequiredOptionalPairs> reconcile(Collection<TdarFile> files_) {
        Set<TdarFile> files = new HashSet<>(files_);
        Set<TdarFile> toReturn = new HashSet<>();
        Map<String, TdarFile> fileMap = new HashMap<>();
        files.forEach(file -> {
            if (fileMap.containsKey(file.getName())) {
                throw new TdarRecoverableRuntimeException("tdarFileReconciller.duplicate",Arrays.asList(file.getName()));
            }
            fileMap.put(file.getName().toLowerCase(), file);
        });

        Iterator<TdarFile> fileIterator = files.iterator();
        Map<TdarFile, RequiredOptionalPairs> result = new HashMap<>();
        Set<String> skip = new HashSet<>();
        List<TdarFile> errors = new ArrayList<>();

        // iterate over all files
        while (fileIterator.hasNext()) {
            TdarFile file = fileIterator.next();
            // if we've seen and dealt with it, skip it
            if (skip.contains(file.getName().toLowerCase())) {
                fileIterator.remove();
                continue;
            }
            String ext = file.getExtension();

            // for the unambigous file groups, that is, the ones that don't overlap with other pairs...
            if (unambiguous.containsKey(ext)) {
                RequiredOptionalPairs pair = unambiguous.get(ext).iterator().next();
                fileIterator.remove();

                // find the primary file
                String base = StringUtils.stripEnd(file.getName(), ext).toLowerCase();
                TdarFile primary = null;

                if (file.getExtension().equals(pair.getPrimaryExtension())) {
                    primary = file;
                } else {
                    primary = fileMap.get(base + pair.getPrimaryExtension());
                }

                result.put(primary, pair);
                // error out if we don't have the primary file
                if (primary == null) {
                    errors.add(file);
                    continue;
                }
                cleanup(fileMap, skip, primary);
                toReturn.add(primary);
                // go through and get all of the other files
                for (String ext_ : pair.getAllExtensions()) {
                    if (!ext.equals(ext_) && !pair.getPrimaryExtension().equals(ext_)) {
                        TdarFile file2 = fileMap.get(base + ext_);
                        if (file2 != null) {
                            primary.getParts().add(file2);
                            cleanup(fileMap, skip, file2);
                        }
                    }
                }
                // iterate over the other parts of the required optional pair and cleanup / remove
            }
        }

        logger.trace("files:{} /\n\t {}", files, ambiguous);
        Iterator<TdarFile> iterator = files.iterator();
        reset(files, skip);

        while (iterator.hasNext()) {
            TdarFile file = iterator.next();
            for (RequiredOptionalPairs pair : simple) {
                if (pair.getPrimaryExtension().equals(file.getExtension())) {
                    result.put(file, pair);
                    cleanup(fileMap, skip, file);
                    iterator.remove();
                }
            }
        }

        logger.debug("toReturn: {}", toReturn);
        logger.debug("errors : {}", errors);
        logger.debug("result : {}", result);

        return result;
    }

    private void reset(Collection<TdarFile> files, Set<String> skip) {
        Iterator<TdarFile> fileIterator;
        fileIterator = files.iterator();
        while (fileIterator.hasNext()) {
            TdarFile next = fileIterator.next();
            if (skip.contains(next.getName())) {
                fileIterator.remove();
            }
        }
        skip.clear();
    }

    private void cleanup(Map<String, TdarFile> fileMap, Set<String> skip, TdarFile file2) {
        skip.add(file2.getName());
        fileMap.remove(file2.getName());
    }
}
