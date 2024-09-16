package com.luuqui.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadingUtil {

  public static void executeWithDelay(Runnable command, long delay) {
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    executor.schedule(command, delay, TimeUnit.MILLISECONDS);
  }

}
