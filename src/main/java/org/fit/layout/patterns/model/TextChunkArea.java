/**
 * TextChunkArea.java
 *
 * Created on 26. 6. 2018, 13:52:54 by burgetr
 */
package org.fit.layout.patterns.model;

import java.awt.Color;

import org.fit.layout.impl.DefaultArea;
import org.fit.layout.model.Area;
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
    

    public TextChunkArea(Rectangular r, Area sourceArea, Box sourceBox)
    {
        super(r);
        text = "";
        this.sourceArea = sourceArea;
        this.sourceBox = sourceBox;
        copyStyle(sourceBox);
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
    
    protected void copyStyle(Box src)
    {
        setBackgroundColor((src.getBackgroundColor() == null) ? null : new Color(src.getBackgroundColor().getRed(), src.getBackgroundColor().getGreen(), src.getBackgroundColor().getBlue()));
        setUnderline(src.getUnderline());
        setLineThrough(src.getLineThrough());
        setFontSize(src.getFontSize());
        setFontWeight(src.getFontWeight());
        setBackgroundSeparated(src.isBackgroundSeparated());
    }
}
