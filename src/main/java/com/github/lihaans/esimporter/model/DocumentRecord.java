package com.github.lihaans.esimporter.model;

public class DocumentRecord {
    private final String filePath;
    private final long lineNo;
    private final String index;
    private final String id;
    private final String routing;
    private final String sourceJson;

    public DocumentRecord(String filePath, long lineNo, String index, String id, String routing, String sourceJson) {
        this.filePath = filePath;
        this.lineNo = lineNo;
        this.index = index;
        this.id = id;
        this.routing = routing;
        this.sourceJson = sourceJson;
    }

    public String getFilePath() { return filePath; }
    public long getLineNo() { return lineNo; }
    public String getIndex() { return index; }
    public String getId() { return id; }
    public String getRouting() { return routing; }
    public String getSourceJson() { return sourceJson; }
}
