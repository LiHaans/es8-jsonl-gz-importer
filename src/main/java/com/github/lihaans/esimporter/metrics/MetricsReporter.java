package com.github.lihaans.esimporter.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsReporter {
    private static final Logger log = LoggerFactory.getLogger(MetricsReporter.class);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void start(final String jobName, final MetricsCollector collector, long intervalSeconds) {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("job={} metrics readLines={} indexedDocs={} failedDocs={} retryDocs={} bulkRequests={} bulkFailures={}",
                        jobName,
                        collector.getReadLines(),
                        collector.getIndexedDocs(),
                        collector.getFailedDocs(),
                        collector.getRetryDocs(),
                        collector.getBulkRequests(),
                        collector.getBulkFailures());
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
