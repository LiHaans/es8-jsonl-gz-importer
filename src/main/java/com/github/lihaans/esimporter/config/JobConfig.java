package com.github.lihaans.esimporter.config;

import java.util.ArrayList;
import java.util.List;

public class JobConfig {
    private String jobName;
    private String inputDir;
    private String indexName;
    private List<String> esHosts = new ArrayList<String>();
    private String idField;
    private String routingField;
    private int fileParallelism = 2;
    private int writerParallelism = 4;
    private int batchDocLimit = 2000;
    private int batchBytesLimit = 8 * 1024 * 1024;
    private int queueCapacity = 200;
    private int maxRetries = 5;
    private long retryBaseMillis = 1000L;
    private String checkpointDbPath = "./runtime/checkpoints.db";
    private String deadLetterDir = "./runtime/deadletter";
    private long checkpointFlushEveryLines = 1000L;
    private long metricsLogIntervalSeconds = 15L;
    private int connectTimeoutMillis = 3000;
    private int socketTimeoutMillis = 120000;
    private int maxConnTotal = 100;
    private int maxConnPerRoute = 20;
    private String username;
    private String password;
    private String apiKey;

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getInputDir() { return inputDir; }
    public void setInputDir(String inputDir) { this.inputDir = inputDir; }
    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }
    public List<String> getEsHosts() { return esHosts; }
    public void setEsHosts(List<String> esHosts) { this.esHosts = esHosts; }
    public String getIdField() { return idField; }
    public void setIdField(String idField) { this.idField = idField; }
    public String getRoutingField() { return routingField; }
    public void setRoutingField(String routingField) { this.routingField = routingField; }
    public int getFileParallelism() { return fileParallelism; }
    public void setFileParallelism(int fileParallelism) { this.fileParallelism = fileParallelism; }
    public int getWriterParallelism() { return writerParallelism; }
    public void setWriterParallelism(int writerParallelism) { this.writerParallelism = writerParallelism; }
    public int getBatchDocLimit() { return batchDocLimit; }
    public void setBatchDocLimit(int batchDocLimit) { this.batchDocLimit = batchDocLimit; }
    public int getBatchBytesLimit() { return batchBytesLimit; }
    public void setBatchBytesLimit(int batchBytesLimit) { this.batchBytesLimit = batchBytesLimit; }
    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public long getRetryBaseMillis() { return retryBaseMillis; }
    public void setRetryBaseMillis(long retryBaseMillis) { this.retryBaseMillis = retryBaseMillis; }
    public String getCheckpointDbPath() { return checkpointDbPath; }
    public void setCheckpointDbPath(String checkpointDbPath) { this.checkpointDbPath = checkpointDbPath; }
    public String getDeadLetterDir() { return deadLetterDir; }
    public void setDeadLetterDir(String deadLetterDir) { this.deadLetterDir = deadLetterDir; }
    public long getCheckpointFlushEveryLines() { return checkpointFlushEveryLines; }
    public void setCheckpointFlushEveryLines(long checkpointFlushEveryLines) { this.checkpointFlushEveryLines = checkpointFlushEveryLines; }
    public long getMetricsLogIntervalSeconds() { return metricsLogIntervalSeconds; }
    public void setMetricsLogIntervalSeconds(long metricsLogIntervalSeconds) { this.metricsLogIntervalSeconds = metricsLogIntervalSeconds; }
    public int getConnectTimeoutMillis() { return connectTimeoutMillis; }
    public void setConnectTimeoutMillis(int connectTimeoutMillis) { this.connectTimeoutMillis = connectTimeoutMillis; }
    public int getSocketTimeoutMillis() { return socketTimeoutMillis; }
    public void setSocketTimeoutMillis(int socketTimeoutMillis) { this.socketTimeoutMillis = socketTimeoutMillis; }
    public int getMaxConnTotal() { return maxConnTotal; }
    public void setMaxConnTotal(int maxConnTotal) { this.maxConnTotal = maxConnTotal; }
    public int getMaxConnPerRoute() { return maxConnPerRoute; }
    public void setMaxConnPerRoute(int maxConnPerRoute) { this.maxConnPerRoute = maxConnPerRoute; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
