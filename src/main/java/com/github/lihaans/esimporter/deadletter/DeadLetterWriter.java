package com.github.lihaans.esimporter.deadletter;

import com.github.lihaans.esimporter.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeadLetterWriter {
    private final File baseDir;

    public DeadLetterWriter(String deadLetterDir) {
        this.baseDir = new File(deadLetterDir);
        if (!this.baseDir.exists() && !this.baseDir.mkdirs()) {
            throw new IllegalStateException("Failed to create dead letter dir: " + deadLetterDir);
        }
    }

    public synchronized void write(String jobName, String filePath, long lineNo, String raw, String errorType, String errorMessage) {
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File dayDir = new File(baseDir, jobName + File.separator + day);
        if (!dayDir.exists() && !dayDir.mkdirs()) {
            throw new IllegalStateException("Failed to create dead letter day dir: " + dayDir.getAbsolutePath());
        }
        File outFile = new File(dayDir, FileUtils.safeFileName(new File(filePath).getName()) + ".dlq.jsonl");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, true))) {
            writer.write(toJson(filePath, lineNo, raw, errorType, errorMessage));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dead letter", e);
        }
    }

    private String toJson(String filePath, long lineNo, String raw, String errorType, String errorMessage) {
        return "{"
                + "\"filePath\":\"" + escape(filePath) + "\"," 
                + "\"lineNo\":" + lineNo + ","
                + "\"errorType\":\"" + escape(errorType) + "\"," 
                + "\"errorMessage\":\"" + escape(errorMessage) + "\"," 
                + "\"raw\":\"" + escape(raw) + "\""
                + "}";
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
