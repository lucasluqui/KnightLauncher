package com.lucasluqui.util;

import java.io.IOException;

import static com.lucasluqui.util.Log.log;

public class INetUtil
{

  /**
   * @return The page's content, or null if it couldn't be fetched.
   */
  public static String getWebpageContent (String url)
  {
    try {
      return HttpUtil.getString(url);
    } catch (IOException | IllegalArgumentException e) {
      log.error(e);
    }
    return null;
  }

}
