package com.lucasluqui.util;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.lucasluqui.util.Log.log;

public class ThreadingUtil
{
  /**
   * Runs the task on a new thread of its own.
   * Named so it can be told apart in thread dumps, and anything it throws
   * gets logged rather than silently ending it.
   * <p>
   * Note: Keeps the launcher running until it's done.
   */
  public static Thread start (String name, Runnable task)
  {
    Thread thread = new Thread(task, name);
    thread.setUncaughtExceptionHandler(LOG_UNCAUGHT);
    thread.start();
    return thread;
  }

  /**
   * Like {@link #start}, but the thread doesn't keep the launcher from exiting.
   * For things that only matter while we're running anyway.
   */
  public static Thread startDaemon (String name, Runnable task)
  {
    Thread thread = new Thread(task, name);
    thread.setDaemon(true);
    thread.setUncaughtExceptionHandler(LOG_UNCAUGHT);
    thread.start();
    return thread;
  }

  /**
   * Runs the task in the background after the given delay in milliseconds.
   * Everything delayed this way shares a single thread, so tasks shouldn't take long.
   * Anything touching the UI should use {@link #invokeLater} instead.
   */
  public static void executeWithDelay (Runnable command, long delay)
  {
    SCHEDULER.schedule(() -> {
      try {
        command.run();
      } catch (Throwable e) {
        log.error("Delayed task failed", e);
      }
    }, delay, TimeUnit.MILLISECONDS);
  }

  /**
   * Runs the task on the event dispatch thread after the given delay in milliseconds.
   *
   * @return The timer running it, which can be stopped to cancel it or restarted to push it back.
   */
  public static Timer invokeLater (Runnable task, int delay)
  {
    Timer timer = new Timer(delay, e -> task.run());
    timer.setRepeats(false);
    timer.start();
    return timer;
  }

  private static final Thread.UncaughtExceptionHandler LOG_UNCAUGHT =
    (thread, e) -> log.error("Uncaught exception in thread", "thread", thread.getName(), e);

  private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(task -> {
    Thread thread = new Thread(task, "DelayedTasks");
    thread.setDaemon(true);
    thread.setUncaughtExceptionHandler(LOG_UNCAUGHT);
    return thread;
  });

}
