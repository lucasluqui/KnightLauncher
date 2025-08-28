package com.lucasluqui.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestUtil
{

  public static JSONObject makeRequest (String method, String endpoint, String[] params)
      throws Exception
  {
    StringBuilder result = new StringBuilder();

    // parse params
    if(params.length > 0) {
      endpoint += "?" + params[0];
    }
    if(params.length > 1) {
      for(int i = 1; params.length > i; i++) {
        endpoint += "&" + params[i];
      }
    }

    URL url = new URL(endpoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(method);
    try (BufferedReader reader = new BufferedReader(
      new InputStreamReader(conn.getInputStream()))) {
      for (String line; (line = reader.readLine()) != null; ) {
        result.append(line);
      }
    }

    return new JSONObject(result.toString());
  }

}
