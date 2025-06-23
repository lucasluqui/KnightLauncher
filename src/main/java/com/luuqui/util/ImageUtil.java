package com.luuqui.util;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import static com.luuqui.util.Log.log;

public class ImageUtil
{

  public static Image getImageFromURL (URL url, int width, int height)
  {
    Image image = null;

    try {
      image = ImageIO.read(url);
      image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    } catch (IOException e) {
      log.error(e);
    }

    return image;
  }

  public static Image getImageFromURL (String url, int width, int height)
  {
    try {
      return getImageFromURL(new URL(url), width, height);
    } catch (MalformedURLException e) {
      log.error(e);
      return null;
    }
  }

  public static byte[] getAnimatedImageFromURL (String url)
  {
    InputStream is = null;
    byte[] imageBytes = null;
    try {
      URL _url = new URL(url);
      is = _url.openStream();
      imageBytes = IOUtils.toByteArray(is);
    }
    catch (IOException e) {
      log.error(e);
      // Perform any other exception handling that's appropriate.
    }
    finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          log.error(e);
        }
      }
    }

    return imageBytes;
  }

  /*
   * Based on Philipp Reichart's response
   * https://stackoverflow.com/questions/7603400/how-to-make-a-rounded-corner-image-in-java
   */
  public static Image addRoundedCorners (Image image, int radius)
  {
    int w = image.getWidth(null);
    int h = image.getHeight(null);
    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2 = output.createGraphics();

    // This is what we want, but it only does hard-clipping, i.e. aliasing
    // g2.setClip(new RoundRectangle2D ...)

    // so instead fake soft-clipping by first drawing the desired clip shape
    // in fully opaque white with antialiasing enabled...
    g2.setComposite(AlphaComposite.Src);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.WHITE);
    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

    // ... then compositing the image on top,
    // using the white shape from above as alpha source
    g2.setComposite(AlphaComposite.SrcIn);
    g2.drawImage(image, 0, 0, null);

    g2.dispose();

    return output;
  }

  public static BufferedImage loadImageWithinJar (String fileName)
  {
    BufferedImage buff = null;
    try {
      buff = ImageIO.read(ImageUtil.class.getResourceAsStream(fileName));
    } catch (IOException e) {
      log.error(e);
      return null;
    }
    return buff;
  }

  public static ImageIcon imageStreamToIcon (InputStream imageStream)
  {
    BufferedImage image = null;
    try {
      image = ImageIO.read(imageStream);
    } catch (IOException e) {
      log.error(e);
    }
    ImageIcon icon = new ImageIcon(image);
    return icon;
  }

  public static BufferedImage loadImageFromBase64 (String data)
  {
    if(data == null) return null;

    String base64Image = data.contains(",") ? data.split(",")[1] : data;
    byte[] imageBytes = DatatypeConverter.parseBase64Binary(base64Image);
    BufferedImage img = null;
    try {
      img = ImageIO.read(new ByteArrayInputStream(imageBytes));
    } catch (Exception e) {
      log.error(e);
    }
    return img;
  }

  public static String imageToBase64 (BufferedImage image)
      throws UncheckedIOException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", os);
      return Base64.getEncoder().encodeToString(os.toByteArray());
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static BufferedImage resizeImage (BufferedImage originalImage, int targetWidth, int targetHeight)
  {
    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = resizedImage.createGraphics();
    graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    graphics2D.dispose();
    return resizedImage;
  }

  public static BufferedImage resizeImagePreserveTransparency (BufferedImage originalImage, int targetWidth, int targetHeight)
  {
    Image tmp = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = newImage.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();
    return newImage;
  }

  // https://stackoverflow.com/questions/43106992/how-do-i-fade-the-edges-of-an-image-in-java-example-given
  public static BufferedImage fadeEdges (BufferedImage sourceImage, double intensity)
  {
    for(int i = 0; i < sourceImage.getWidth(); i++) { // i is the x coord
      for(int j = 0; j < sourceImage.getHeight(); j++) { // j is the y coord
        int color = sourceImage.getRGB(i, j);
        int r = (color >> 16) & 0xff; // extract red value
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;
        //pixel's distance from center
        double dist = Math.sqrt( Math.pow(i - sourceImage.getWidth() / 2, 2) + Math.pow(j - sourceImage.getHeight() / 2, 2) );
        r = (int) Math.max(0, r - dist * intensity); // r - dist * intensity makes px darker
        g = (int) Math.max(0, g - dist * intensity); // Math.max makes sure r is always >= 0
        b = (int) Math.max(0, b - dist * intensity);
        int newRGB = (r << 16) + (g << 8) + b; // convert r,g,b to single int
        sourceImage.setRGB(i, j, newRGB); // finally, update rgb value
      }
    }
    return sourceImage;
  }

  public static BufferedImage toBufferedImage (Image img)
  {
    if (img instanceof BufferedImage)
    {
      return (BufferedImage) img;
    }

    // Create a buffered image with transparency
    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

    // Draw the image on to the buffered image
    Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();

    // Return the buffered image
    return bimage;
  }

  public static BufferedImage generatePlainColorImage (int width, int height, Color color)
  {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(color);
    g2d.fillRect(0, 0, width, height);
    return image;
  }

  public static void setAlpha (BufferedImage input, byte alpha)
  {
    alpha %= 0xff;
    for (int cx=0;cx<input.getWidth();cx++) {
      for (int cy=0;cy<input.getHeight();cy++) {
        int color = input.getRGB(cx, cy);

        int mc = (alpha << 24) | 0x00ffffff;
        int newcolor = color & mc;
        input.setRGB(cx, cy, newcolor);

      }

    }
  }

}
