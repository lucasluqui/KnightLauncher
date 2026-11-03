package com.lucasluqui.launcher.setting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdditionalArgsFilterTest
{
  private static final String NL = System.lineSeparator();

  @Test
  public void extraPerfModeDropsHeapArguments ()
  {
    // This used to throw UnsupportedOperationException (removing from Arrays.asList).
    String filtered = SettingsManager.filterAdditionalArgs("-Xmx4G\n-XX:+UseG1GC\n-Xms1G\r\n-Dfoo=bar", true);
    assertEquals("-XX:+UseG1GC" + NL + "-Dfoo=bar", filtered);
  }

  @Test
  public void otherModesKeepEverything ()
  {
    assertEquals("-Xmx4G" + NL + "-Dfoo=bar", SettingsManager.filterAdditionalArgs("-Xmx4G\n-Dfoo=bar", false));
  }

  @Test
  public void emptyInputStaysEmpty ()
  {
    assertEquals("", SettingsManager.filterAdditionalArgs("", true));
  }
}
