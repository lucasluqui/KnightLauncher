package com.lucasluqui.launcher.flamingo.data;

public class Event
{
  public Event (String type, String banner, String content, String link, long startsAt, long endsAt)
  {
    this.type = type;
    this.banner = banner;
    this.content = content;
    this.link = link == null || link.equalsIgnoreCase("null") ? null : link;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
  }

  public String type;

  public String banner;

  public String content;

  public String link;

  public long startsAt;

  public long endsAt;
}
