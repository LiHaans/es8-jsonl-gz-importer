package com.github.lihaans.esimporter.job;

import com.github.lihaans.esimporter.model.FileTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {
    public List<FileTask> scan(String jobName, String inputDir) {
        List<FileTask> tasks = new ArrayList<FileTask>();
        scanRecursive(jobName, new File(inputDir), tasks);
        return tasks;
    }

    private void scanRecursive(String jobName, File dir, List<FileTask> tasks) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanRecursive(jobName, file, tasks);
            } else if (file.getName().endsWith(".jsonl.gz")) {
                tasks.add(new FileTask(jobName, file.getAbsolutePath(), file.length()));
            }
        }
    }
}
