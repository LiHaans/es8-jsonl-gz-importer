package com.github.lihaans.esimporter.job;

import com.github.lihaans.esimporter.checkpoint.SqliteCheckpointStore;
import com.github.lihaans.esimporter.config.ConfigLoader;
import com.github.lihaans.esimporter.config.JobConfig;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java -jar es8-jsonl-gz-importer.jar <config.json>");
            System.exit(1);
        }
        JobConfig config = ConfigLoader.load(args[0]);
        SqliteCheckpointStore checkpointStore = new SqliteCheckpointStore(config.getCheckpointDbPath());
        checkpointStore.init();
        new ImportJobRunner(config, checkpointStore).run();
    }
}
