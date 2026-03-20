package com.github.lihaans.esimporter.checkpoint;

import com.github.lihaans.esimporter.model.FileTask;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteCheckpointStore implements CheckpointStore {
    private final String dbPath;

    public SqliteCheckpointStore(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void init() {
        File parent = new File(dbPath).getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Connection conn = connection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS file_task ("
                    + "job_name TEXT NOT NULL,"
                    + "file_path TEXT NOT NULL,"
                    + "file_size INTEGER NOT NULL,"
                    + "status TEXT NOT NULL,"
                    + "processed_lines INTEGER NOT NULL DEFAULT 0,"
                    + "success_lines INTEGER NOT NULL DEFAULT 0,"
                    + "failed_lines INTEGER NOT NULL DEFAULT 0,"
                    + "last_success_line INTEGER NOT NULL DEFAULT 0,"
                    + "last_error TEXT,"
                    + "PRIMARY KEY(job_name, file_path)"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize sqlite checkpoint store", e);
        }
    }

    @Override
    public void registerFiles(String jobName, List<FileTask> files) {
        String sql = "INSERT OR IGNORE INTO file_task(job_name,file_path,file_size,status) VALUES (?,?,?,?)";
        try (Connection conn = connection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (FileTask file : files) {
                ps.setString(1, jobName);
                ps.setString(2, file.getFilePath());
                ps.setLong(3, file.getFileSize());
                ps.setString(4, "NEW");
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register files", e);
        }
    }

    @Override
    public List<FileTask> loadPendingFiles(String jobName) {
        String sql = "SELECT file_path,file_size,status,processed_lines,success_lines,failed_lines,last_success_line FROM file_task WHERE job_name=? AND status IN ('NEW','FAILED','PARTIAL','RUNNING') ORDER BY file_path";
        List<FileTask> tasks = new ArrayList<FileTask>();
        try (Connection conn = connection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jobName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FileTask fileTask = new FileTask(jobName, rs.getString(1), rs.getLong(2));
                    fileTask.setStatus(rs.getString(3));
                    fileTask.setProcessedLines(rs.getLong(4));
                    fileTask.setSuccessLines(rs.getLong(5));
                    fileTask.setFailedLines(rs.getLong(6));
                    fileTask.setLastSuccessLine(rs.getLong(7));
                    tasks.add(fileTask);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load pending files", e);
        }
        return tasks;
    }

    @Override
    public void markRunning(String jobName, String filePath) {
        updateStatus(jobName, filePath, "RUNNING", null);
    }

    @Override
    public void updateReadProgress(String jobName, String filePath, long processedLines, long failedLines) {
        String sql = "UPDATE file_task SET status='PARTIAL', processed_lines=?, failed_lines=? WHERE job_name=? AND file_path=?";
        try (Connection conn = connection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, processedLines);
            ps.setLong(2, failedLines);
            ps.setString(3, jobName);
            ps.setString(4, filePath);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update read progress", e);
        }
    }

    @Override
    public void addWriteResult(String jobName, String filePath, long successDelta, long failedDelta, long lastSuccessLine) {
        String sql = "UPDATE file_task SET status='PARTIAL', success_lines=success_lines+?, failed_lines=failed_lines+?, last_success_line=CASE WHEN last_success_line < ? THEN ? ELSE last_success_line END WHERE job_name=? AND file_path=?";
        try (Connection conn = connection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, successDelta);
            ps.setLong(2, failedDelta);
            ps.setLong(3, lastSuccessLine);
            ps.setLong(4, lastSuccessLine);
            ps.setString(5, jobName);
            ps.setString(6, filePath);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add write result", e);
        }
    }

    @Override
    public void markDone(String jobName, String filePath) {
        String sql = "UPDATE file_task SET status='DONE', last_error=NULL WHERE job_name=? AND file_path=?";
        try (Connection conn = connection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jobName);
            ps.setString(2, filePath);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark done", e);
        }
    }

    @Override
    public void markFailed(String jobName, String filePath, String errorMessage) {
        updateStatus(jobName, filePath, "FAILED", errorMessage);
    }

    private void updateStatus(String jobName, String filePath, String status, String errorMessage) {
        String sql = "UPDATE file_task SET status=?, last_error=? WHERE job_name=? AND file_path=?";
        try (Connection conn = connection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, errorMessage);
            ps.setString(3, jobName);
            ps.setString(4, filePath);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status", e);
        }
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}
