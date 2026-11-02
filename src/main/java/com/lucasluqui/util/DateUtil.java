package com.lucasluqui.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil
{
  public static String getFormattedTimeRemaining (long timestamp)
  {
    Duration remaining = Duration.ofMillis(timestamp - System.currentTimeMillis());

    if (remaining.toDays() > 0L) {
      return remaining.toDays() + " {d}";
    } else if (remaining.toHours() > 0L) {
      return remaining.toHours() + " {h}";
    } else if (remaining.toMinutes() > 0L) {
      return remaining.toMinutes() + " {m}";
    } else {
      return "";
    }
  }

  /**
   * Formats a timestamp in the user's time zone.
   */
  public static String getFormattedTime (long timestamp)
  {
    ZonedDateTime time = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
    return WEEKDAY_AND_MONTH.format(time) + " "
      + getDayNumberWithSuffix(time.getDayOfMonth()) + " "
      + HOUR_AND_MINUTE.format(time);
  }

  private static String getDayNumberWithSuffix (int day)
  {
    String suffix = switch (day) {
      case 1, 21, 31 -> "st";
      case 2, 22 -> "nd";
      case 3, 23 -> "rd";
      default -> "th";
    };
    return day + suffix;
  }

  // The day number goes in between with a suffix, which formatters can't do, so it's added separately.
  private static final DateTimeFormatter WEEKDAY_AND_MONTH = DateTimeFormatter.ofPattern("EEEE, MMMM", Locale.ENGLISH);
  private static final DateTimeFormatter HOUR_AND_MINUTE = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

}
