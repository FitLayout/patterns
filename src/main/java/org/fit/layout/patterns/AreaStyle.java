/**
 * AreaStyle.java
 *
 * Created on 16.3.2016, 15:02:43 by burgetr
 */
package org.fit.layout.patterns;

import java.awt.Color;
import java.util.Vector;

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
    
    /**
     * Computes the style of an area node.
     * @param area
     */
    public AreaStyle(Area area)
    {
        Vector<Box> boxes = area.getAllBoxes();
        
        fontSize = area.getFontSize();
        style = area.getFontStyle();
        weight = area.getFontWeight();
        if (!boxes.isEmpty())
            color = boxes.firstElement().getColor();
        else
            color = Color.BLACK;
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
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
        if (color == null)
        {
            if (other.color != null) return false;
        }
        else if (!color.equals(other.color)) return false;
        if (Float.floatToIntBits(fontSize) != Float
                .floatToIntBits(other.fontSize)) return false;
        if (Float.floatToIntBits(style) != Float.floatToIntBits(other.style))
            return false;
        if (Float.floatToIntBits(weight) != Float.floatToIntBits(other.weight))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        String ret = "[fs:" + fontSize + " w:" + weight + " s:" + style + " c:";
        ret += String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        ret += "]";
        return ret;
    }
    
    public String toARFFString()
    {
        return fontSize + "," + weight + "," + style + "," 
                + (color.getRed() / 255.0) + "," + (color.getGreen() / 255.0) + "," + (color.getBlue() / 255.0);
    }
    
}
