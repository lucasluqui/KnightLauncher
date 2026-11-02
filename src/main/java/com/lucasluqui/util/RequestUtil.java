package com.lucasluqui.util;

import org.json.JSONObject;

import java.net.URL;

public class RequestUtil
{

  public static JSONObject makeRequest (String method, String endpoint, String[] params)
    throws Exception
  {
    StringBuilder url = new StringBuilder(endpoint);
    for (int i = 0; i < params.length; i++) {
      url.append(i == 0 ? '?' : '&').append(params[i]);
    }

    // Going through URL first escapes anything URIs don't allow, like spaces in user-entered values.
    return new JSONObject(HttpUtil.send(method, HttpUtil.toURI(new URL(url.toString()))));
  }

}
