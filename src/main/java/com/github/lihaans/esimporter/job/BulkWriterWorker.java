package com.github.lihaans.esimporter.job;

import com.github.lihaans.esimporter.checkpoint.CheckpointStore;
import com.github.lihaans.esimporter.config.JobConfig;
import com.github.lihaans.esimporter.es.BulkIndexer;
import com.github.lihaans.esimporter.model.BulkBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BulkWriterWorker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BulkWriterWorker.class);
    private final JobConfig config;
    private final BlockingQueue<BulkBatch> queue;
    private final BulkIndexer bulkIndexer;
    private final CheckpointStore checkpointStore;
    private volatile boolean stop;

    public BulkWriterWorker(JobConfig config, BlockingQueue<BulkBatch> queue, BulkIndexer bulkIndexer, CheckpointStore checkpointStore) {
        this.config = config;
        this.queue = queue;
        this.bulkIndexer = bulkIndexer;
        this.checkpointStore = checkpointStore;
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop || !queue.isEmpty()) {
            try {
                BulkBatch batch = queue.poll(2, TimeUnit.SECONDS);
                if (batch == null) {
                    continue;
                }
                BulkIndexer.BulkWriteResult result = bulkIndexer.write(batch);
                checkpointStore.updateProgress(config.getJobName(), batch.getFilePath(), batch.getEndLine(), result.getSuccessCount(), result.getFailedCount(), result.getLastSuccessLine());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("bulk writer failed", e);
            }
        }
    }
}
