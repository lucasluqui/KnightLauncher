package com.lucasluqui.launcher;

public class PlayerCount
{
  public PlayerCount (int prodCount, int previewCount)
  {
    this.prodCount = prodCount;
    this.previewCount = previewCount;
  }

  public int getProdCount ()
  {
    return prodCount;
  }

  public int getPreviewCount ()
  {
    return previewCount;
  }

  public int getProdEstimated ()
  {
    return Math.round(getProdCount() * ESTIMATION_MULT);
  }

  public int getPreviewEstimated ()
  {
    return Math.round(getPreviewCount() * ESTIMATION_MULT);
  }

  public int getSum ()
  {
    return prodCount + previewCount;
  }

  public int getEstimated ()
  {
    return getProdEstimated() + getPreviewEstimated();
  }

  private int prodCount;
  private int previewCount;

  private final float ESTIMATION_MULT = 1.4f;
}
