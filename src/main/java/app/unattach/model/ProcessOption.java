package app.unattach.model;

public enum ProcessOption {
  DOWNLOAD(true, false),
  DOWNLOAD_AND_REMOVE(true, true),
  REMOVE(false, true);

  private final boolean download;
  private final boolean remove;

  ProcessOption(boolean download, boolean remove) {
    this.download = download;
    this.remove = remove;
  }

  boolean shouldDownload() {
    return download;
  }

  boolean shouldRemove() {
    return remove;
  }
}
