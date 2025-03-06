package com.luuqui.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

  public static String getDateAsString() {
    Date date = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
    return dateFormat.format(date);
  }

  public static String getDayNumberWithSuffix(int date) {
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
