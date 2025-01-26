package rt.gui;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import base.Bool_0;
import base.False_0;
import base.List_1;
import base.True_0;
import base.Void_0;
import base.gui.Graphics_0;
import rt.Str;

public record Graphics(Graphics2D g)  implements Graphics_0 {

  @Override
  public Graphics_0 color$mut(long r_m$, long g_m$, long b_m$) {
    g.setColor(new java.awt.Color(Math.toIntExact(r_m$), 
        Math.toIntExact(g_m$), Math.toIntExact(b_m$)));
    return this;
  }

  @Override
  public Graphics_0 clearScreen$mut() {
    g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
    return this;
  }

  @Override
  public Graphics_0 drawText$mut(Str text_m$, long x_m$, long y_m$) {
    g.drawString(Str.toJavaStr(text_m$.utf8()), Math.toIntExact(x_m$), Math.toIntExact(y_m$));
    return this;
  }

  @Override
  public Graphics_0 font$mut(Str name_m$, Str style_m$, long size_m$) {
    int style =0;
    switch (Str.toJavaStr(style_m$.utf8())) {
      case "P" -> style =0;
      case "B" -> style =1;
      case "I" -> style =2;
      case "BI" -> style =3;
      case "IB" -> style =3;
      default -> style =0;};
    g.setFont(new Font(Str.toJavaStr(name_m$.utf8()), style, Math.toIntExact(size_m$)));// change the style to string where plain =0 , bold = 1 itelic = 2 and itelicbold=3
    return this;
  }

  @Override
  public Graphics_0 drawLine$mut(long x1_m$, long y1_m$, long x2_m$, long y2_m$) {
    g.drawLine(Math.toIntExact(x1_m$),Math.toIntExact(y1_m$),Math.toIntExact(x2_m$),Math.toIntExact(y2_m$));
    return this;
  }

  @Override
  public Graphics_0 stroke$mut(double width_m$) {
    g.setStroke(new BasicStroke((float) width_m$));
    return this;
  }

  @Override
  public Graphics_0 dashedStroke$mut(double width_m$) {
    float[] dashPattern = {10.0f, 5.0f}; 
    float dashPhase = 0.0f;
    g.setStroke(new BasicStroke((float) width_m$, 
        BasicStroke.CAP_SQUARE, 
        BasicStroke.JOIN_MITER, 
                         10.0f, 
                   dashPattern, 
                     dashPhase));
    return this;
  }

  @Override
  public Graphics_0 dottedStroke$mut(double width_m$) {
    float[] dotPattern = {2.0f, 5.0f};
    float dotPhase = 0.0f;
    g.setStroke(new BasicStroke((float) width_m$, 
        BasicStroke.CAP_ROUND, // Use round caps for circular dots
       BasicStroke.JOIN_MITER, 
                        10.0f, 
                   dotPattern, 
                   dotPhase));
    return this;
  }

  @Override
  public Graphics_0 drawOval$mut(long x_m$, long y_m$, long width_m$, long height_m$, Bool_0 fill_m$) {
    if(fill_m$.equals(True_0.$self)  ) {
      g.fillOval(Math.toIntExact(x_m$), Math.toIntExact(y_m$), Math.toIntExact(width_m$), Math.toIntExact(height_m$));
    }else {
      g.drawOval(Math.toIntExact(x_m$), Math.toIntExact(y_m$), Math.toIntExact(width_m$), Math.toIntExact(height_m$));
    }
    return this;
  }

  @Override
  public Graphics_0 drawRect$mut(long x_m$, long y_m$, long width_m$, long height_m$, Bool_0 fill_m$) {
    if(fill_m$.equals(True_0.$self)  ) {
      g.fillRect(Math.toIntExact(x_m$), Math.toIntExact(y_m$), Math.toIntExact(width_m$), Math.toIntExact(height_m$));
    }else {
      g.drawRect(Math.toIntExact(x_m$), Math.toIntExact(y_m$), Math.toIntExact(width_m$), Math.toIntExact(height_m$));
    }
    return this;
  }

  @Override
  public Graphics_0 drawPolygon$mut(List_1 xPoints_m$, List_1 yPoints_m$, Bool_0 fill_m$) {
    var xSize = xPoints_m$.size$read().intValue();
    var ySize = yPoints_m$.size$read().intValue();
    int[] xPoints = new int[xSize];
    int[] yPoints = new int[ySize];
    final int[] xindex = {0};
    xPoints_m$.iter$mut().for$mut(b -> {
      xPoints[xindex[0]++]=(Math.toIntExact((long) b));
      return Void_0.$self;
    });
    final int[] yindex = {0};
    yPoints_m$.iter$mut().for$mut(b -> {
      yPoints[yindex[0]++]=(Math.toIntExact((long) b));
      return Void_0.$self;
    });
    if(fill_m$.equals(True_0.$self)  ) {
      g.fillPolygon(xPoints, yPoints, xPoints.length);
    }else {
      g.drawPolygon(xPoints, yPoints, xPoints.length);
    }
    return this;
  }

  @Override
  public Graphics_0 image$mut(long x_m$, long y_m$, long width_m$, long height_m$, Str img_m$) {
    BufferedImage img;
    File file = new File(Str.toJavaStr(img_m$.utf8()));
    if(!file.exists()) {
      System.out.println("Image not found!");
      return this;}
    try {
      img = ImageIO.read(file);
      Image scaledImage = img.getScaledInstance(Math.toIntExact(width_m$), Math.toIntExact(height_m$), Image.SCALE_SMOOTH);
      g.drawImage(scaledImage, Math.toIntExact(x_m$), Math.toIntExact(y_m$), null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }

  @Override
  public Graphics_0 rotate$mut(double theta_m$) {
    g.rotate(theta_m$);
    return this;
  }

  @Override
  public Graphics_0 translate$mut(double tx_m$, double ty_m$) {
    g.translate(tx_m$, ty_m$);
    return this;
  }

}
