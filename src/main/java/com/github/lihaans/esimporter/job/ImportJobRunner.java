package com.github.lihaans.esimporter.job;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.github.lihaans.esimporter.checkpoint.CheckpointStore;
import com.github.lihaans.esimporter.config.JobConfig;
import com.github.lihaans.esimporter.deadletter.DeadLetterWriter;
import com.github.lihaans.esimporter.es.BulkIndexer;
import com.github.lihaans.esimporter.es.ElasticsearchClientFactory;
import com.github.lihaans.esimporter.metrics.MetricsCollector;
import com.github.lihaans.esimporter.metrics.MetricsReporter;
import com.github.lihaans.esimporter.model.BulkBatch;
import com.github.lihaans.esimporter.model.FileTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImportJobRunner {
    private final JobConfig config;
    private final CheckpointStore checkpointStore;

    public ImportJobRunner(JobConfig config, CheckpointStore checkpointStore) {
        this.config = config;
        this.checkpointStore = checkpointStore;
    }

    public void run() throws Exception {
        FileScanner scanner = new FileScanner();
        List<FileTask> discovered = scanner.scan(config.getJobName(), config.getInputDir());
        checkpointStore.registerFiles(config.getJobName(), discovered);
        List<FileTask> tasks = checkpointStore.loadPendingFiles(config.getJobName());

        BlockingQueue<BulkBatch> queue = new ArrayBlockingQueue<BulkBatch>(config.getQueueCapacity());
        MetricsCollector metrics = new MetricsCollector();
        MetricsReporter reporter = new MetricsReporter();
        DeadLetterWriter deadLetterWriter = new DeadLetterWriter(config.getDeadLetterDir());
        ElasticsearchClient client = new ElasticsearchClientFactory().create(config);
        BulkIndexer bulkIndexer = new BulkIndexer(client, config, deadLetterWriter, metrics);

        reporter.start(config.getJobName(), metrics, config.getMetricsLogIntervalSeconds());

        ExecutorService readerPool = Executors.newFixedThreadPool(config.getFileParallelism());
        ExecutorService writerPool = Executors.newFixedThreadPool(config.getWriterParallelism());
        List<BulkWriterWorker> writers = new ArrayList<BulkWriterWorker>();
        for (int i = 0; i < config.getWriterParallelism(); i++) {
            BulkWriterWorker worker = new BulkWriterWorker(config, queue, bulkIndexer, checkpointStore);
            writers.add(worker);
            writerPool.submit(worker);
        }

        for (FileTask task : tasks) {
            readerPool.submit(new FileImportWorker(config, task, checkpointStore, queue, deadLetterWriter, metrics));
        }

        readerPool.shutdown();
        readerPool.awaitTermination(7, TimeUnit.DAYS);
        while (!queue.isEmpty()) {
            Thread.sleep(1000L);
        }
        for (BulkWriterWorker worker : writers) {
            worker.stop();
        }
        writerPool.shutdown();
        writerPool.awaitTermination(1, TimeUnit.HOURS);
        reporter.shutdown();
    }
}
