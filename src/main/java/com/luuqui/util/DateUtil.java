package com.luuqui.util;

import com.luuqui.launcher.Locale;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.luuqui.util.Log.log;

public class DateUtil {

  private static final String TIMEZONE = "PST"; // Game's timezone

  public static String getDateAsString() {
    Date date = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
    return dateFormat.format(date);
  }

  public static String getFormattedRemaining(long timestamp) {
    long remainingMillis = timestamp - System.currentTimeMillis();
    long minutesRemaining = (remainingMillis / 1000L) / 60L;
    long hoursRemaining = minutesRemaining / 60L;
    long daysRemaining = hoursRemaining / 24L;

    if(daysRemaining > 0L) {
      return Locale.getValue("m.timer_days", String.valueOf(daysRemaining));
    } else if(hoursRemaining > 0L) {
      return Locale.getValue("m.timer_hours", String.valueOf(hoursRemaining));
    } else if(minutesRemaining > 0L) {
      return Locale.getValue("m.timer_minutes", String.valueOf(minutesRemaining));
    } else {
      return Locale.getValue("m.timer_less_than_minute");
    }
  }

  public static String getFormattedMonthDay(long timestamp) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp);
    calendar.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM"); // Only the month, we'll concatenate the day later.

    return dateFormat.format(calendar.getTime()) + " " + DateUtil.getDayNumberWithSuffix(calendar.get(Calendar.DATE) - 1);
  }

  private static String getDayNumberWithSuffix(int date) {
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

}
