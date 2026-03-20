package com.github.lihaans.esimporter.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BulkBatch {
    private final String filePath;
    private final long startLine;
    private final long endLine;
    private final List<DocumentRecord> records;
    private final long estimatedBytes;

    public BulkBatch(String filePath, long startLine, long endLine, List<DocumentRecord> records, long estimatedBytes) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.records = new ArrayList<DocumentRecord>(records);
        this.estimatedBytes = estimatedBytes;
    }

    public String getFilePath() { return filePath; }
    public long getStartLine() { return startLine; }
    public long getEndLine() { return endLine; }
    public List<DocumentRecord> getRecords() { return Collections.unmodifiableList(records); }
    public long getEstimatedBytes() { return estimatedBytes; }
    public int size() { return records.size(); }
}
