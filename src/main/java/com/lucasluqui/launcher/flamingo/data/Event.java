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

  /**
   * Whether the event is already running and ends within the next two days.
   */
  public boolean isEndingSoon ()
  {
    long now = System.currentTimeMillis();
    return endsAt != 0L && startsAt <= now && now < endsAt && endsAt - now < ENDING_SOON_THRESHOLD;
  }

  public String type;

  public String banner;

  public String content;

  public String link;

  public long startsAt;

  public long endsAt;

  private static final long ENDING_SOON_THRESHOLD = 2 * 24 * 60 * 60 * 1000L;
}
