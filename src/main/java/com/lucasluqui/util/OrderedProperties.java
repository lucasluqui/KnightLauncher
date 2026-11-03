package com.lucasluqui.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Properties that are stored with their keys in a given order, instead of the hash or alphabetical order
 * {@link Properties#store} uses by default. Keys are stored in this order:
 * <ol>
 *   <li>Keys in the order, in that order.</li>
 *   <li>Suffixed keys ({@code <key in the order>_<suffix>}, like server-specific settings), one group per suffix,
 *   suffixes sorted alphabetically, and keys within each in the order.</li>
 *   <li>Anything else, alphabetically.</li>
 * </ol>
 */
public class OrderedProperties extends Properties
{
  public OrderedProperties (List<String> order)
  {
    for (int i = 0; i < order.size(); i++) {
      _order.putIfAbsent(order.get(i), i);
    }
  }

  // Properties.store writes entries in the order entrySet() iterates them when a subclass overrides it.
  @Override
  public Set<Map.Entry<Object, Object>> entrySet ()
  {
    List<Map.Entry<Object, Object>> entries = new ArrayList<>(super.entrySet());
    entries.sort(Comparator.comparing(e -> placeOf(String.valueOf(e.getKey()))));
    return Collections.unmodifiableSet(new LinkedHashSet<>(entries));
  }

  private Place placeOf (String key)
  {
    Integer index = _order.get(key);
    if (index != null) return new Place(0, "", index, key);

    // The longest key in the order it's a suffixed version of, since keys themselves can contain '_'.
    String base = null;
    for (String candidate : _order.keySet()) {
      if (key.length() > candidate.length() + 1 && key.startsWith(candidate) && key.charAt(candidate.length()) == '_'
        && (base == null || candidate.length() > base.length())) {
        base = candidate;
      }
    }
    if (base != null) return new Place(1, key.substring(base.length() + 1), _order.get(base), key);

    return new Place(2, "", 0, key);
  }

  private static final class Place implements Comparable<Place>
  {
    Place (int tier, String suffix, int index, String key)
    {
      this.tier = tier;
      this.suffix = suffix;
      this.index = index;
      this.key = key;
    }

    @Override
    public int compareTo (Place other)
    {
      if (tier != other.tier) return Integer.compare(tier, other.tier);
      int c = suffix.compareTo(other.suffix);
      if (c != 0) return c;
      if (index != other.index) return Integer.compare(index, other.index);
      return key.compareTo(other.key);
    }

    final int tier;
    final String suffix;
    final int index;
    final String key;
  }

  /**
   * Reads the keys of a properties file in the order they appear.
   */
  public static List<String> readKeyOrder (InputStream in) throws IOException
  {
    List<String> keys = new ArrayList<>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) continue;
      int sep = line.length();
      for (char c : new char[]{'=', ':', ' ', '\t'}) {
        int idx = line.indexOf(c);
        if (idx >= 0 && idx < sep) sep = idx;
      }
      keys.add(line.substring(0, sep));
    }
    return keys;
  }

  private final Map<String, Integer> _order = new HashMap<>();
}
