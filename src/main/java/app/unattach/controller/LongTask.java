package app.unattach.controller;

public interface LongTask<T> {
  int getNumberOfSteps();

  boolean hasMoreSteps();
  T takeStep() throws LongTaskException;
}
