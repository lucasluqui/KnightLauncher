package com.luuqui.util;

public class TextUtil
{

  public static String extractNumericFromString (String input)
  {
    input = input.replaceAll("[^0-9 ]", "").replaceAll(" +", " ").trim();
    return input;
  }

}
