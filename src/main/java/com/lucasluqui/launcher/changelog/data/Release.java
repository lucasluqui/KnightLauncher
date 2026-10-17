package com.lucasluqui.launcher.changelog.data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

public class Release
{
  public Release (String tag, String publishedAt, String body)
  {
    this.tag = tag;
    this.publishedAt = publishedAt;
    this.body = body;
  }

  public String getTag ()
  {
    return tag;
  }

  public String getBody ()
  {
    return body;
  }

  public String getFormattedDate ()
  {
    if (publishedAt == null || publishedAt.isEmpty()) {
      return "";
    }

    try {
      return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
        .format(Instant.parse(publishedAt));
    } catch (DateTimeParseException e) {
      return "";
    }
  }

  private final String tag;
  private final String publishedAt;
  private final String body;
}
