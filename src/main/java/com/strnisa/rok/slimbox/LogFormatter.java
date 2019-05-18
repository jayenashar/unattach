package com.strnisa.rok.slimbox;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public final class LogFormatter extends Formatter {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

  @Override
  public String format(LogRecord logRecord) {
    StringBuilder sb = new StringBuilder();
    Date logDate = new Date(logRecord.getMillis());
    String formattedLogDate = SIMPLE_DATE_FORMAT.format(logDate);
    String paddedLogLevel = String.format("%1$-7s", logRecord.getLevel());
    String abbreviatedLoggerName = ClassUtils.getAbbreviatedName(logRecord.getLoggerName(), 10);
    String paddedAbbreviatedLoggerName = String.format("%1$-35s", abbreviatedLoggerName);
    sb.append(formattedLogDate)
        .append(" ")
        .append(paddedLogLevel)
        .append(" ")
        .append(paddedAbbreviatedLoggerName)
        .append(" ")
        .append(formatMessage(logRecord))
        .append(LINE_SEPARATOR);
    if (logRecord.getThrown() != null) {
      sb.append(ExceptionUtils.getStackTrace(logRecord.getThrown()));
    }
    return sb.toString();
  }
}
