package com.strnisa.rok.slimbox.model;

public interface Config {
    String getSearchQuery();
    String getTargetDirectory();
    void saveSearchQuery(String query);
    void saveTargetDirectory(String path);
}
