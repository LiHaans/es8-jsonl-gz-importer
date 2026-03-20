package com.github.lihaans.esimporter.checkpoint;

import com.github.lihaans.esimporter.model.FileTask;

import java.util.List;

public interface CheckpointStore {
    void init();
    void registerFiles(String jobName, List<FileTask> files);
    List<FileTask> loadPendingFiles(String jobName);
    void markRunning(String jobName, String filePath);
    void updateReadProgress(String jobName, String filePath, long processedLines, long failedLines);
    void addWriteResult(String jobName, String filePath, long successDelta, long failedDelta, long lastSuccessLine);
    void markDone(String jobName, String filePath);
    void markFailed(String jobName, String filePath, String errorMessage);
}
