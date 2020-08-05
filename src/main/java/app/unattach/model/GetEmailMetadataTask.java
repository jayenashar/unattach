package app.unattach.model;

import app.unattach.controller.LongTask;
import app.unattach.controller.LongTaskException;

import java.io.IOException;
import java.util.List;

public class GetEmailMetadataTask implements LongTask<GetEmailMetadataTask.Result> {
  interface Worker {
    void getEmailMetadata(int startIndexInclusive, int endIndexExclusive) throws IOException;
  }

  public static class Result {
    public final int currentBatchNumber;

    Result(int currentBatchNumber) {
      this.currentBatchNumber = currentBatchNumber;
    }
  }

  private final List<String> emailIds;
  // (maximum batch size = 100)
  // batch size = 40 ==> batch quota units = 200 ==> 1 batch / second
  private final int batchSize = 40;
  private final int numberOfBatches;
  private final Worker worker;
  private int currentBatchNumber;

  GetEmailMetadataTask(List<String> emailIds, Worker worker) {
    this.emailIds = emailIds;
    numberOfBatches = (emailIds.size() + batchSize - 1) / batchSize;
    this.worker = worker;
  }

  @Override
  public int getNumberOfSteps() {
    return numberOfBatches;
  }

  @Override
  public boolean hasMoreSteps() {
    return currentBatchNumber < numberOfBatches;
  }

  @Override
  public Result takeStep() throws LongTaskException {
    try {
      if (currentBatchNumber != 0) {
        Thread.sleep(1000);
      }
      final int startIndexInclusive = currentBatchNumber * batchSize;
      final int endIndexExclusive = Math.min(emailIds.size(), (currentBatchNumber + 1) * batchSize);
      worker.getEmailMetadata(startIndexInclusive, endIndexExclusive);
      ++currentBatchNumber;
      return new Result(currentBatchNumber);
    } catch (Throwable t) {
      throw new LongTaskException(t);
    }
  }
}
