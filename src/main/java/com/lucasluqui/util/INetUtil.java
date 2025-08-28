package com.lucasluqui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import static com.lucasluqui.util.Log.log;

public class INetUtil
{

  public static String getWebpageContent (String url)
  {
    try {
      URLConnection connection = new URL(url).openConnection();
      connection
              .setRequestProperty("User-Agent",
                      "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
      connection.setDefaultUseCaches(false);
      connection.setUseCaches(false);
      connection.setConnectTimeout(10000);
      connection.setReadTimeout(10000);
      connection.connect();

      BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(),
              StandardCharsets.UTF_8));

      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = r.readLine()) != null) {
        sb.append(line);
      }

      return sb.toString();
    } catch (IOException e) {
      log.error(e);
    }
    return null;
  }

}
