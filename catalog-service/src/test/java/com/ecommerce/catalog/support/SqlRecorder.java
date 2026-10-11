package com.ecommerce.catalog.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

/**
 * Mencatat setiap eksekusi JDBC lewat datasource-proxy. Satu eksekusi
 * batch dihitung satu kali, beserta jumlah baris di dalam batch.
 */
public class SqlRecorder implements QueryExecutionListener {

    public record Execution(String sql, boolean batch, int batchSize) {
    }

    private final List<Execution> executions =
            Collections.synchronizedList(new ArrayList<>());

    @Override
    public void beforeQuery(ExecutionInfo info, List<QueryInfo> queries) {
    }

    @Override
    public void afterQuery(ExecutionInfo info, List<QueryInfo> queries) {
        String sql = queries.isEmpty()
                ? ""
                : queries.getFirst().getQuery();
        executions.add(new Execution(
                sql, info.isBatch(), info.getBatchSize()));
    }

    public void reset() {
        executions.clear();
    }

    public List<Execution> executions() {
        synchronized (executions) {
            return List.copyOf(executions);
        }
    }

    public List<String> statements() {
        return executions().stream().map(Execution::sql).toList();
    }

    public long count() {
        return executions().size();
    }

    /** Jumlah statement yang diawali kata kunci, mis. "select". */
    public long count(String keyword) {
        return statements().stream()
                .filter(s -> s.stripLeading().toLowerCase(Locale.ROOT)
                        .startsWith(keyword))
                .count();
    }
}
