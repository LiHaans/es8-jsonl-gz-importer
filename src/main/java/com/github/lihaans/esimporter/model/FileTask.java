package com.github.lihaans.esimporter.model;

public class FileTask {
    private final String jobName;
    private final String filePath;
    private final long fileSize;
    private String status;
    private long processedLines;
    private long successLines;
    private long failedLines;
    private long lastSuccessLine;
    private int retryCount;

    public FileTask(String jobName, String filePath, long fileSize) {
        this.jobName = jobName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.status = "NEW";
    }

    public String getJobName() { return jobName; }
    public String getFilePath() { return filePath; }
    public long getFileSize() { return fileSize; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getProcessedLines() { return processedLines; }
    public void setProcessedLines(long processedLines) { this.processedLines = processedLines; }
    public long getSuccessLines() { return successLines; }
    public void setSuccessLines(long successLines) { this.successLines = successLines; }
    public long getFailedLines() { return failedLines; }
    public void setFailedLines(long failedLines) { this.failedLines = failedLines; }
    public long getLastSuccessLine() { return lastSuccessLine; }
    public void setLastSuccessLine(long lastSuccessLine) { this.lastSuccessLine = lastSuccessLine; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}
