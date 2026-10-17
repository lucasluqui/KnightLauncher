package com.lucasluqui.launcher;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BooleanSupplier;

import static com.lucasluqui.launcher.Log.log;

/**
 * Lets a launcher that's starting up while another one is already running ask that one to pop back up
 * from the tray, so it can quietly go away instead. The running launcher listens on a local port, which
 * it leaves in a file inside the launcher's directory for others to find.
 */
public class InstanceBeacon
{
  /**
   * Starts listening for other instances asking us to pop back up. Meant for the instance holding the lock.
   *
   * @param onSummoned Called whenever another instance asks us to pop back up. Returns whether we did.
   */
  public static void listen (BooleanSupplier onSummoned)
  {
    try {
      ServerSocket server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());

      // We start listening before the launcher sets up its directories, so it might not be there yet on a first run.
      Files.createDirectories(INSTANCE_FILE_PATH.getParent());
      Files.write(INSTANCE_FILE_PATH, String.valueOf(server.getLocalPort()).getBytes(StandardCharsets.UTF_8));

      // Stop answering as soon as we start exiting. Restarts start the new instance right before exiting,
      // and it shouldn't mistake us for a launcher that's sticking around.
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          server.close();
        } catch (IOException ignored) {
        }
      }));

      Thread listener = new Thread(() -> {
        while (!server.isClosed()) {
          try (Socket socket = server.accept()) {
            socket.setSoTimeout(TIMEOUT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            if (!SUMMON.equals(in.readLine())) continue;

            boolean shown = onSummoned.getAsBoolean();
            log.info("Another launcher instance asked us to pop back up", "shown", shown);

            Writer out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            out.write((shown ? SHOWN : NOT_HIDDEN) + "\n");
            out.flush();
          } catch (IOException e) {
            if (!server.isClosed()) log.error("Failed to answer another launcher instance", e);
          }
        }
      }, "InstanceBeacon");
      listener.setDaemon(true);
      listener.start();
    } catch (IOException e) {
      log.error("Failed to start listening for other launcher instances", e);
    }
  }

  /**
   * Asks the running launcher instance to pop back up from the tray.
   *
   * @return Whether it popped back up, false if it wasn't hidden away to begin with, or null if nobody answered.
   */
  public static Boolean summon ()
  {
    int port;
    try {
      port = Integer.parseInt(new String(Files.readAllBytes(INSTANCE_FILE_PATH), StandardCharsets.UTF_8).trim());
    } catch (Exception e) {
      // Nobody left a port behind, likely an older version that doesn't listen.
      return null;
    }

    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), TIMEOUT);
      socket.setSoTimeout(TIMEOUT);

      Writer out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
      out.write(SUMMON + "\n");
      out.flush();

      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      String reply = in.readLine();
      if (SHOWN.equals(reply)) return true;
      if (NOT_HIDDEN.equals(reply)) return false;
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  private static final Path INSTANCE_FILE_PATH = Paths.get(LauncherGlobals.USER_DIR, "KnightLauncher", ".instance");

  private static final String SUMMON = "KnightLauncher:summon";
  private static final String SHOWN = "KnightLauncher:shown";
  private static final String NOT_HIDDEN = "KnightLauncher:not_hidden";

  private static final int TIMEOUT = 2000;
}
