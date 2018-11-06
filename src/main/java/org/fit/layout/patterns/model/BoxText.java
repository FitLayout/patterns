/**
 * BoxText.java
 *
 * Created on 6. 11. 2018, 14:42:49 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;

import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;
import org.fit.layout.patterns.AreaUtils;

/**
 * A parto of text with connection to source boxes
 * @author burgetr
 */
public class BoxText
{
    private List<Box> boxes;
    /** Complete text of the string of boxes */
    private String text;
    /** The individual box string start positions in the complete text */
    private int[] offsets;
    
    
    public BoxText(List<Box> boxes)
    {
        this.boxes = boxes;
        computeOffsets();
    }
    
    public List<Box> getBoxes()
    {
        return boxes;
    }

    public String getText()
    {
        return text;
    }

    public int[] getOffsets()
    {
        return offsets;
    }

    public int getIndexForPosition(int pos)
    {
        if (pos >= 0 && pos < text.length())
        {
            int i = 0;
            while (i < offsets.length && offsets[i] < pos)
                i++;
            return i;
        }
        else
            return -1;
    }
    
    @Override
    public String toString()
    {
        return text;
    }

    //=================================================================

    private void computeOffsets()
    {
        StringBuilder sb = new StringBuilder();
        offsets = new int[boxes.size()];
        Box prev = null;
        int i = 0;
        for (Box box : boxes)
        {
            if (prev != null && boxesSeparated(prev, box))
                sb.append(' ');
            offsets[i++] = sb.length();
            sb.append(box.getOwnText());
            prev = box;
        }
        text = sb.toString();
    }
    
    /**
     * Determines whether two subsequent boxes (on the same line) are separated by a space 
     * @param box1 the first box
     * @param box2 the second box
     * @return {@code true} when there is a space between the boxes
     */
    private boolean boxesSeparated(Box box1, Box box2)
    {
        Rectangular r1 = box1.getBounds();
        Rectangular r2 = box2.getBounds();
        if (AreaUtils.isOnSameLine(r1, r2)) //boxes on the same line
        {
            //determine whether there is a space between the boxes
            int minSep = Math.round(Math.max(box1.getFontSize(), box2.getFontSize()) * 0.4f); //at least 0.4em
            //compute the distance
            int dist = box2.getX1() - box1.getX2();
            return (dist >= minSep);
        }
        else
            return true; //not on the same line, always separated
    }
    
}
