package com.github.lihaans.esimporter.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {
    private final AtomicLong readLines = new AtomicLong();
    private final AtomicLong indexedDocs = new AtomicLong();
    private final AtomicLong failedDocs = new AtomicLong();
    private final AtomicLong retryDocs = new AtomicLong();
    private final AtomicLong bulkRequests = new AtomicLong();
    private final AtomicLong bulkFailures = new AtomicLong();

    public void incReadLines(long delta) { readLines.addAndGet(delta); }
    public void incIndexedDocs(long delta) { indexedDocs.addAndGet(delta); }
    public void incFailedDocs(long delta) { failedDocs.addAndGet(delta); }
    public void incRetryDocs(long delta) { retryDocs.addAndGet(delta); }
    public void incBulkRequests(long delta) { bulkRequests.addAndGet(delta); }
    public void incBulkFailures(long delta) { bulkFailures.addAndGet(delta); }

    public long getReadLines() { return readLines.get(); }
    public long getIndexedDocs() { return indexedDocs.get(); }
    public long getFailedDocs() { return failedDocs.get(); }
    public long getRetryDocs() { return retryDocs.get(); }
    public long getBulkRequests() { return bulkRequests.get(); }
    public long getBulkFailures() { return bulkFailures.get(); }
}
