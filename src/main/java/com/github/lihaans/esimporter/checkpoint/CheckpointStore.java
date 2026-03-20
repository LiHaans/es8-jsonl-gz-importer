package com.github.lihaans.esimporter.checkpoint;

import com.github.lihaans.esimporter.model.FileTask;

import java.util.List;

public interface CheckpointStore {
    void init();
    void registerFiles(String jobName, List<FileTask> files);
    List<FileTask> loadPendingFiles(String jobName);
    void markRunning(String jobName, String filePath);
    void updateProgress(String jobName, String filePath, long processedLines, long successLines, long failedLines, long lastSuccessLine);
    void markDone(String jobName, String filePath, long processedLines, long successLines, long failedLines, long lastSuccessLine);
    void markFailed(String jobName, String filePath, String errorMessage);
}
