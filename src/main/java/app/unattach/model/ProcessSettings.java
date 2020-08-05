package app.unattach.model;

import java.io.File;

public class ProcessSettings {
  final ProcessOption processOption;
  final File targetDirectory;
  final String filenameSchema;
  final boolean addMetadata;

  public ProcessSettings(ProcessOption processOption, File targetDirectory, String filenameSchema,
                         boolean addMetadata) {
    this.processOption = processOption;
    this.targetDirectory = targetDirectory;
    this.filenameSchema = filenameSchema;
    this.addMetadata = addMetadata;
  }
}
