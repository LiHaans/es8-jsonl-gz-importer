package com.github.lihaans.esimporter.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.util.BinaryData;
import com.github.lihaans.esimporter.config.JobConfig;
import com.github.lihaans.esimporter.deadletter.DeadLetterWriter;
import com.github.lihaans.esimporter.metrics.MetricsCollector;
import com.github.lihaans.esimporter.model.BulkBatch;
import com.github.lihaans.esimporter.model.DocumentRecord;
import com.github.lihaans.esimporter.util.BackoffUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BulkIndexer {
    private static final Logger log = LoggerFactory.getLogger(BulkIndexer.class);

    private final ElasticsearchClient client;
    private final JobConfig config;
    private final DeadLetterWriter deadLetterWriter;
    private final MetricsCollector metrics;

    public BulkIndexer(ElasticsearchClient client, JobConfig config, DeadLetterWriter deadLetterWriter, MetricsCollector metrics) {
        this.client = client;
        this.config = config;
        this.deadLetterWriter = deadLetterWriter;
        this.metrics = metrics;
    }

    public BulkWriteResult write(BulkBatch batch) {
        List<DocumentRecord> pending = new ArrayList<DocumentRecord>(batch.getRecords());
        long success = 0;
        long failed = 0;
        long lastSuccessLine = 0;

        for (int retry = 0; retry <= config.getMaxRetries() && !pending.isEmpty(); retry++) {
            metrics.incBulkRequests(1);
            try {
                BulkResponse response = client.bulk(buildRequest(pending));
                List<DocumentRecord> retryDocs = new ArrayList<DocumentRecord>();
                List<BulkResponseItem> items = response.items();
                for (int i = 0; i < items.size(); i++) {
                    BulkResponseItem item = items.get(i);
                    DocumentRecord record = pending.get(i);
                    if (item.error() == null) {
                        success++;
                        lastSuccessLine = Math.max(lastSuccessLine, record.getLineNo());
                    } else if (isRetryable(item.status())) {
                        retryDocs.add(record);
                    } else {
                        failed++;
                        deadLetterWriter.write(config.getJobName(), record.getFilePath(), record.getLineNo(), record.getSourceJson(), "es_non_retryable", item.error().reason());
                    }
                }
                pending = retryDocs;
                if (!pending.isEmpty() && retry < config.getMaxRetries()) {
                    metrics.incRetryDocs(pending.size());
                    sleep(retry + 1);
                }
            } catch (Exception e) {
                metrics.incBulkFailures(1);
                if (retry >= config.getMaxRetries()) {
                    for (DocumentRecord record : pending) {
                        failed++;
                        deadLetterWriter.write(config.getJobName(), record.getFilePath(), record.getLineNo(), record.getSourceJson(), "bulk_exception", e.getMessage());
                    }
                    pending.clear();
                } else {
                    sleep(retry + 1);
                }
            }
        }

        metrics.incIndexedDocs(success);
        metrics.incFailedDocs(failed);
        return new BulkWriteResult(success, failed, lastSuccessLine);
    }

    private BulkRequest buildRequest(List<DocumentRecord> records) {
        List<BulkOperation> operations = new ArrayList<BulkOperation>(records.size());
        for (DocumentRecord record : records) {
            BinaryData document = BinaryData.of(record.getSourceJson().getBytes(java.nio.charset.StandardCharsets.UTF_8), "application/json");
            BulkOperation operation = new BulkOperation.Builder()
                    .index(idx -> {
                        idx.index(record.getIndex());
                        idx.id(record.getId());
                        if (record.getRouting() != null && !record.getRouting().trim().isEmpty()) {
                            idx.routing(record.getRouting());
                        }
                        idx.document(document);
                        return idx;
                    })
                    .build();
            operations.add(operation);
        }
        return new BulkRequest.Builder().operations(operations).build();
    }

    private boolean isRetryable(int status) {
        return status == 429 || status == 502 || status == 503 || status == 504;
    }

    private void sleep(int retry) {
        long millis = BackoffUtils.exponentialBackoff(config.getRetryBaseMillis(), retry);
        log.warn("job={} retry={} sleepMs={}", config.getJobName(), retry, millis);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class BulkWriteResult {
        private final long successCount;
        private final long failedCount;
        private final long lastSuccessLine;

        public BulkWriteResult(long successCount, long failedCount, long lastSuccessLine) {
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.lastSuccessLine = lastSuccessLine;
        }

        public long getSuccessCount() { return successCount; }
        public long getFailedCount() { return failedCount; }
        public long getLastSuccessLine() { return lastSuccessLine; }
    }
}
