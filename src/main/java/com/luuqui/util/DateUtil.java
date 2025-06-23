package com.luuqui.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class DateUtil
{
  private static final String TIMEZONE = "PST"; // Game's timezone

  public static String getFormattedTimeRemaining (long timestamp)
  {
    long remainingMillis = timestamp - System.currentTimeMillis();
    long minutesRemaining = (remainingMillis / 1000L) / 60L;
    long hoursRemaining = minutesRemaining / 60L;
    long daysRemaining = hoursRemaining / 24L;

    if(daysRemaining > 0L) {
      return daysRemaining + " {d}";
    } else if(hoursRemaining > 0L) {
      return hoursRemaining + " {h}";
    } else if(minutesRemaining > 0L) {
      return minutesRemaining + " {m}";
    } else {
      return "";
    }
  }

  public static String getFormattedTime (long timestamp, String timezone)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp);
    calendar.setTimeZone(TimeZone.getTimeZone(timezone));

    // Only the month, we'll concatenate the rest later.
    // Also, force it to be in English locale.
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", java.util.Locale.ENGLISH);

    return dateFormat.format(calendar.getTime()) + " "
        + getDayNumberWithSuffix(calendar.get(Calendar.DATE)) + " "
        + calendar.get(Calendar.HOUR_OF_DAY) + ":" + getMinuteWithTrailingZero(calendar.get(Calendar.MINUTE)) + " "
        + (calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM") + " "
        + TIMEZONE;
  }

  private static String getDayNumberWithSuffix (int date)
  {
    String suffix = "th";
    switch (date) {
      case 1:
      case 21:
      case 31:
        suffix = "st";
        break;
      case 2:
      case 22:
        suffix = "nd";
        break;
      case 3:
      case 23:
        suffix = "rd";
        break;
    }
    return date + suffix;
  }

  private static String getMinuteWithTrailingZero (int minute)
  {
    return minute < 10 ? "0" + minute : "" + minute;
  }

}
