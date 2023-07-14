package lol.lolpany.ormik.regression;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class TablesToRecreateGenerator {
    private Set<String> tables;
    private final Map<String, List<String>> tableToChild;

    public TablesToRecreateGenerator(Map<String, List<String>> childsToParents, Set<String> tablesToRecreate) {
        this.tableToChild = new ConcurrentHashMap<>();
        for (Map.Entry<String, List<String>> childToParents : childsToParents.entrySet()) {
            for (String parent : childToParents.getValue()) {
                tableToChild.computeIfAbsent(parent, key -> Collections.synchronizedList(new ArrayList<>()));
                tableToChild.get(parent).add(childToParents.getKey());
            }
        }
        this.tables = new HashSet<>();
        for (Map.Entry<String, List<String>> tableToChildren : tableToChild.entrySet()) {
            tables.addAll(walkTable(tableToChild, tablesToRecreate, tableToChildren.getKey()));
        }
        tables.addAll(tablesToRecreate);
    }

    private Set<String> walkTable(Map<String, List<String>> tableToChild, Set<String> tablesToRecreate, String table) {
        Set<String> result = new HashSet<>();
        if (tablesToRecreate.contains(table)) {
            result.add(table);
            for (String child : ofNullable(tableToChild.get(table)).orElse(emptyList())) {
                result.addAll(collectAllChildren(tableToChild, child));
            }
        }
        return result;
    }

    private Set<String> collectAllChildren(Map<String, List<String>> tableToChild, String table) {
        Set<String> result = new HashSet<>();
        result.add(table);
        for (String child : ofNullable(tableToChild.get(table)).orElse(emptyList())) {
            result.addAll(collectAllChildren(tableToChild, child));
        }
        return result;
    }

    public String next() {
        for (String table : tables) {
            if (tableToChild.get(table) == null) {
                tables.remove(table);
                return table;
            }
        }
        if (!tables.isEmpty()) {
            return "";
        } else {
            return null;
        }
    }

    public synchronized void complete(String table) {
        for (Map.Entry<String, List<String>> tableToCh : tableToChild.entrySet()) {
            tableToCh.getValue().remove(table);
            if (tableToCh.getValue().isEmpty()) {
                tableToChild.remove(tableToCh.getKey());
            }
        }
    }
}
