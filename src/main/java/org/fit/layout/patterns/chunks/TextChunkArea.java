/**
 * TextChunkArea.java
 *
 * Created on 26. 6. 2018, 13:52:54 by burgetr
 */
package org.fit.layout.patterns.chunks;

import java.awt.Color;

import org.fit.layout.impl.DefaultArea;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;

/**
 * An area representing a text chunk. The text of the chunk is not given by boxes; it is specified explicitly.
 * @author burgetr
 */
public class TextChunkArea extends DefaultArea
{
    private String text;
    private Area sourceArea;
    private Box sourceBox;
    private AreaTopology layerTopology;
    private Color effectiveBackgroundColor;
    

    public TextChunkArea(Rectangular r, Area sourceArea, Box sourceBox)
    {
        super(r);
        text = "";
        this.sourceArea = sourceArea;
        this.sourceBox = sourceBox;
        addBox(sourceBox); //the box is used for computing the text color of the are (e.g. in AreaStyle)
        copyStyle(sourceArea);
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public String getText(String separator)
    {
        return text;
    }

    public Area getSourceArea()
    {
        return sourceArea;
    }

    public Box getSourceBox()
    {
        return sourceBox;
    }
    
    public AreaTopology getLayerTopology()
    {
        return layerTopology;
    }

    public void setLayerTopology(AreaTopology layerTopology)
    {
        this.layerTopology = layerTopology;
    }

    @Override
    public Color getEffectiveBackgroundColor()
    {
        return effectiveBackgroundColor;
    }

    public void setEffectiveBackgroundColor(Color effectiveBackgroundColor)
    {
        this.effectiveBackgroundColor = effectiveBackgroundColor;
    }

    protected void copyStyle(Area src)
    {
        setBackgroundColor((src.getBackgroundColor() == null) ? null : new Color(src.getBackgroundColor().getRed(), src.getBackgroundColor().getGreen(), src.getBackgroundColor().getBlue()));
        setEffectiveBackgroundColor(src.getEffectiveBackgroundColor());
        setBackgroundSeparated(src.isBackgroundSeparated());
        setUnderline(src.getUnderline());
        setLineThrough(src.getLineThrough());
        setFontSize(src.getFontSize());
        setFontWeight(src.getFontWeight());
        setFontStyle(src.getFontStyle());
        setBackgroundSeparated(src.isBackgroundSeparated());
    }
}
