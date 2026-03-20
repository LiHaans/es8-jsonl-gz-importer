package com.github.lihaans.esimporter.job;

import com.github.lihaans.esimporter.config.JobConfig;
import com.github.lihaans.esimporter.model.BulkBatch;
import com.github.lihaans.esimporter.model.DocumentRecord;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BulkBatchBuilder {
    private final JobConfig config;
    private final String filePath;
    private final List<DocumentRecord> records = new ArrayList<DocumentRecord>();
    private long bytes = 0;
    private long startLine = -1;
    private long endLine = -1;

    public BulkBatchBuilder(JobConfig config, String filePath) {
        this.config = config;
        this.filePath = filePath;
    }

    public void add(DocumentRecord record) {
        if (records.isEmpty()) {
            startLine = record.getLineNo();
        }
        endLine = record.getLineNo();
        records.add(record);
        bytes += record.getSourceJson().getBytes(StandardCharsets.UTF_8).length;
    }

    public boolean shouldFlush() {
        return records.size() >= config.getBatchDocLimit() || bytes >= config.getBatchBytesLimit();
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public BulkBatch buildAndReset() {
        BulkBatch batch = new BulkBatch(filePath, startLine, endLine, records, bytes);
        records.clear();
        bytes = 0;
        startLine = -1;
        endLine = -1;
        return batch;
    }
}
