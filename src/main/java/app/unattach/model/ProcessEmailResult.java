package app.unattach.model;

import java.util.Set;

public class ProcessEmailResult {
  private final Set<String> filenames;

  ProcessEmailResult(Set<String> filenames) {
    this.filenames = filenames;
  }
}
