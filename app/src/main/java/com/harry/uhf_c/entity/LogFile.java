package com.harry.uhf_c.entity;

public class LogFile {
    private String fileName;
    private String filePath;

    public LogFile(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
}
