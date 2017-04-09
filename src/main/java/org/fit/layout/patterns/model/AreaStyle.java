/**
 * AreaStyle.java
 *
 * Created on 16.3.2016, 15:02:43 by burgetr
 */
package org.fit.layout.patterns.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.Box;

/**
 * A simple node style structure used for the comparison of the area styles.
 * @author burgetr
 */
public class AreaStyle
{
    private float fontSize;
    private float style;
    private float weight;
    private Color color;
    
    private Color bgColor;
    private boolean backgroundSeparated;
    private float width;
    private float height;
    
    
    /**
     * Computes the style of an area node.
     * @param area
     */
    public AreaStyle(Area area)
    {
        /*if (area.getId() == 18)
            System.out.println("jo!");*/
        
        fontSize = area.getFontSize();
        style = area.getFontStyle();
        weight = area.getFontWeight();
        color = getAverageColor(area);
        bgColor = area.getEffectiveBackgroundColor();
        backgroundSeparated = area.isBackgroundSeparated();
        width = area.getWidth() / (float) area.getPage().getWidth();
        height = area.getHeight() / (float) area.getPage().getHeight();
    }
    
    /**
     * Creates a copy of the node style.
     * @param src the source node
     */
    public AreaStyle(AreaStyle src)
    {
        this.fontSize = src.fontSize;
        this.style = src.style;
        this.weight = src.weight;
        this.color = new Color(src.color.getRed(), src.color.getGreen(), src.color.getGreen(), src.color.getAlpha());
        if (src.bgColor == null)
            this.bgColor = null;
        else
            this.bgColor = new Color(src.bgColor.getRed(), src.bgColor.getGreen(), src.bgColor.getGreen(), src.bgColor.getAlpha());
        this.width = src.width;
        this.height = src.height;
        this.backgroundSeparated = src.backgroundSeparated;
    }
    
    public float getFontSize()
    {
        return fontSize;
    }

    public float getStyle()
    {
        return style;
    }

    public float getWeight()
    {
        return weight;
    }

    public Color getColor()
    {
        return color;
    }

    public Color getBgColor()
    {
        return bgColor;
    }

    public boolean isBackgroundSeparated()
    {
        return backgroundSeparated;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bgColor == null) ? 0 : bgColor.hashCode());
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + Float.floatToIntBits(fontSize);
        result = prime * result + Float.floatToIntBits(style);
        result = prime * result + Float.floatToIntBits(weight);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AreaStyle other = (AreaStyle) obj;
        if (bgColor == null)
        {
            if (other.bgColor != null) return false;
        }
        else if (!bgColor.equals(other.bgColor)) return false;
        if (color == null)
        {
            if (other.color != null) return false;
        }
        else if (!color.equals(other.color)) return false;
        if (Float.floatToIntBits(fontSize) != Float
                .floatToIntBits(other.fontSize))
            return false;
        if (Float.floatToIntBits(style) != Float.floatToIntBits(other.style))
            return false;
        if (Float.floatToIntBits(weight) != Float.floatToIntBits(other.weight))
            return false;
        return true;
    }

    public int getEditingDistance(AreaStyle other)
    {
        int dif = 0;
        if (fontSize != other.fontSize) dif++;
        if (weight != other.weight) dif++;
        if (style != other.style) dif++;
        if (!color.equals(other.color)) dif++;
        if (!bgColor.equals(other.bgColor)) dif++;
        //if (backgroundSeparated != null || !backgroundSeparated.equals(other.backgroundSeparated)) dif++;
        return dif;
    }

    public void generalizeToFit(AreaStyle other)
    {
        if (fontSize != other.fontSize) fontSize = -1; 
        if (weight != other.weight) weight = -1;
        if (style != other.style) style = -1;
        if (!color.equals(other.color)) color = null;
        if (!bgColor.equals(other.bgColor)) bgColor = null;
        
    }
    
    @Override
    public String toString()
    {
        String ret = "[fs:" + fontSize + " w:" + weight + " s:" + style;
        ret += " c:" + formatColor(color);
        ret += " bg:" + formatColor(bgColor);
        ret += "]";
        return ret;
    }
    
    private String formatColor(Color color)
    {
        return (color == null ? "null" : String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
    }
    
    private Color getAverageColor(Area a)
    {
        List<Box> boxes = new ArrayList<Box>();
        getLeafBoxes(a, boxes);
        return getAverageBoxColor(boxes);
    }
    
    private void getLeafBoxes(Area a, List<Box> dest)
    {
        if (a.isLeaf())
            dest.addAll(a.getBoxes());
        else
        {
            for (Area sub : a.getChildAreas())
                getLeafBoxes(sub, dest);
        }
    }
    
    private Color getAverageBoxColor(List<Box> list)
    {
        int r = 0;
        int g = 0;
        int b = 0;
        int length = 0;
        
        for (Box box : list)
        {
            int len = box.getText().trim().length();
            Color color = box.getColor();
            r += color.getRed() * len;
            g += color.getGreen() * len;
            b += color.getBlue() * len;
            length += len;
        }
        if (length == 0)
            return Color.BLACK;
        else
            return new Color(r / length, g / length, b / length);
    }
    
}
