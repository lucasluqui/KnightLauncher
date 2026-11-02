package com.lucasluqui.launcher.runtime;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Small window showing the progress of the runtime download.
 * <p>
 * This shows up before the launcher itself is running, possibly on Java 8, so it's plain Swing
 * and compiled for Java 8 (see {@code src/main/java8}).
 */
public final class RuntimeWindow
{
  private RuntimeWindow ()
  {}

  /**
   * Installs a runtime, showing its progress. Blocks until it's done or failed.
   *
   * @return The installed runtime's home directory, or null if it failed.
   */
  public static File install (RuntimeSource source)
  {
    if (GraphicsEnvironment.isHeadless()) {
      try {
        return RuntimeInstaller.install(source, null, null);
      } catch (Exception e) {
        System.err.println("Failed to install launcher runtime: " + e);
        return null;
      }
    }

    AtomicReference<File> result = new AtomicReference<>();
    CountDownLatch done = new CountDownLatch(1);

    JFrame frame = new JFrame("Knight Launcher");
    JLabel statusLabel = new JLabel("Preparing the Java runtime for Knight Launcher...");
    JProgressBar progressBar = new JProgressBar(0, 1000);

    SwingUtilities.invokeLater(() -> {
      progressBar.setIndeterminate(true);

      JPanel content = new JPanel(new BorderLayout(0, 10));
      content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
      content.add(statusLabel, BorderLayout.NORTH);
      content.add(progressBar, BorderLayout.CENTER);

      frame.setContentPane(content);
      // The window can't be closed, it goes away by itself once done.
      frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      frame.setResizable(false);
      frame.setSize(420, 110);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });

    Thread worker = new Thread(() -> {
      try {
        result.set(RuntimeInstaller.install(source, null, new RuntimeInstaller.ProgressListener()
        {
          @Override
          public void onDownloadProgress (long downloaded, long total)
          {
            SwingUtilities.invokeLater(() -> {
              if (total > 0) {
                progressBar.setIndeterminate(false);
                progressBar.setValue((int) (downloaded * 1000 / total));
              }
            });
          }

          @Override
          public void onStatus (String status)
          {
            SwingUtilities.invokeLater(() -> {
              statusLabel.setText(status);
              if (status.startsWith("Extracting")) progressBar.setIndeterminate(true);
            });
          }
        }));
      } catch (Exception e) {
        System.err.println("Failed to install launcher runtime: " + e);
      } finally {
        done.countDown();
      }
    }, "RuntimeInstaller");
    worker.start();

    try {
      done.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    SwingUtilities.invokeLater(frame::dispose);
    return result.get();
  }
}
