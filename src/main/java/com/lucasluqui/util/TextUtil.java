package com.lucasluqui.util;

import java.util.Random;

public class TextUtil
{
  public static String extractNumericFromString (String input)
  {
    input = input.replaceAll("[^0-9 ]", "").replaceAll(" +", " ").trim();
    return input;
  }

  public static String getRandomAlphanumeric (int length)
  {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    Random random = new Random();

    return random.ints(leftLimit, rightLimit + 1)
      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
      .limit(length)
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString();
  }

  public static boolean isAlphanumeric (String input)
  {
    return input.matches("^[a-zA-Z0-9]+$");
  }
}
