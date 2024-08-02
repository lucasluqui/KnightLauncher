package com.luuqui.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.luuqui.util.Log.log;

public class ImageUtil {

  public static Image getImageFromURL(String url, int width, int height) {

    Image image = null;

    try {
      URL _url = new URL(url);
      image = ImageIO.read(_url);
      image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    } catch (IOException e) {
      log.error(e);
    }

    return image;
  }

  /*
   * Based on Philipp Reichart's response
   * @ https://stackoverflow.com/questions/7603400/how-to-make-a-rounded-corner-image-in-java
   */
  public static Image addRoundedCorners(Image image, int radius) {

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
    g2.setComposite(AlphaComposite.SrcAtop);
    g2.drawImage(image, 0, 0, null);

    g2.dispose();

    return output;
  }

  public static BufferedImage loadImageWithinJar(String fileName) {

    BufferedImage buff = null;
    try {
      buff = ImageIO.read(ImageUtil.class.getResourceAsStream(fileName));
    } catch (IOException e) {
      log.error(e);
      return null;
    }
    return buff;
  }

  public static ImageIcon imageStreamToIcon(InputStream imageStream) {
    BufferedImage image = null;
    try {
      image = ImageIO.read(imageStream);
    } catch (IOException e) {
      log.error(e);
    }
    ImageIcon icon = new ImageIcon(image);
    return icon;
  }

  public static BufferedImage loadImageFromBase64(String data) {
    if(data == null) return null;

    String base64Image = data.contains(",") ? data.split(",")[1] : data;
    byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
    BufferedImage img = null;
    try {
      img = ImageIO.read(new ByteArrayInputStream(imageBytes));
    } catch (IOException e) {
      log.error(e);
    }
    return img;
  }

  public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = resizedImage.createGraphics();
    graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    graphics2D.dispose();
    return resizedImage;
  }

  // https://stackoverflow.com/questions/43106992/how-do-i-fade-the-edges-of-an-image-in-java-example-given
  public static BufferedImage fadeEdges(BufferedImage sourceImage, double intensity) {
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

  public static BufferedImage toBufferedImage(Image img)
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

}
