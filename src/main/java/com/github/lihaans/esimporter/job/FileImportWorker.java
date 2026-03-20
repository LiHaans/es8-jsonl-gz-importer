package com.github.lihaans.esimporter.job;

import com.github.lihaans.esimporter.checkpoint.CheckpointStore;
import com.github.lihaans.esimporter.config.JobConfig;
import com.github.lihaans.esimporter.deadletter.DeadLetterWriter;
import com.github.lihaans.esimporter.metrics.MetricsCollector;
import com.github.lihaans.esimporter.model.BulkBatch;
import com.github.lihaans.esimporter.model.DocumentRecord;
import com.github.lihaans.esimporter.model.FileTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.zip.GZIPInputStream;

public class FileImportWorker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(FileImportWorker.class);

    private final JobConfig config;
    private final FileTask task;
    private final CheckpointStore checkpointStore;
    private final BlockingQueue<BulkBatch> queue;
    private final DeadLetterWriter deadLetterWriter;
    private final MetricsCollector metrics;
    private final DocumentParser parser;

    public FileImportWorker(JobConfig config,
                            FileTask task,
                            CheckpointStore checkpointStore,
                            BlockingQueue<BulkBatch> queue,
                            DeadLetterWriter deadLetterWriter,
                            MetricsCollector metrics) {
        this.config = config;
        this.task = task;
        this.checkpointStore = checkpointStore;
        this.queue = queue;
        this.deadLetterWriter = deadLetterWriter;
        this.metrics = metrics;
        this.parser = new DocumentParser(config);
    }

    @Override
    public void run() {
        checkpointStore.markRunning(config.getJobName(), task.getFilePath());
        long processed = task.getProcessedLines();
        long success = task.getSuccessLines();
        long failed = task.getFailedLines();
        long lastSuccessLine = task.getLastSuccessLine();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(new FileInputStream(task.getFilePath())), StandardCharsets.UTF_8), 64 * 1024)) {
            BulkBatchBuilder builder = new BulkBatchBuilder(config, task.getFilePath());
            String line;
            long lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (lineNo <= lastSuccessLine) {
                    continue;
                }
                processed++;
                metrics.incReadLines(1);
                try {
                    DocumentRecord record = parser.parse(task.getFilePath(), lineNo, line);
                    builder.add(record);
                    if (builder.shouldFlush()) {
                        queue.put(builder.buildAndReset());
                    }
                } catch (Exception e) {
                    failed++;
                    deadLetterWriter.write(config.getJobName(), task.getFilePath(), lineNo, line, "parse_error", e.getMessage());
                }
                if (processed % config.getCheckpointFlushEveryLines() == 0) {
                    checkpointStore.updateProgress(config.getJobName(), task.getFilePath(), processed, success, failed, lastSuccessLine);
                }
            }
            if (!builder.isEmpty()) {
                queue.put(builder.buildAndReset());
            }
            checkpointStore.updateProgress(config.getJobName(), task.getFilePath(), processed, success, failed, lastSuccessLine);
            log.info("finished reading file={}", task.getFilePath());
        } catch (Exception e) {
            checkpointStore.markFailed(config.getJobName(), task.getFilePath(), e.getMessage());
            log.error("read file failed file={}", task.getFilePath(), e);
        }
    }
}
