package com.lucasallegri.util;

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

}
