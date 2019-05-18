package com.strnisa.rok.slimbox.controller;

public interface LongTask<T> {
  int getNumberOfSteps();

  boolean hasMoreSteps();
  T takeStep() throws LongTaskException;
}
