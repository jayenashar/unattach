package com.strnisa.rok.slimbox.model;

public interface Config {
    String getFilenameSchema();
    String getSearchQuery();
    String getTargetDirectory();
    void saveFilenameSchema(String schema);
    void saveSearchQuery(String query);
    void saveTargetDirectory(String path);
}
