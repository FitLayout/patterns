/**
 * HintWholeLine.java
 *
 * Created on 31. 10. 2018, 11:29:21 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;

import org.fit.layout.model.Area;

/**
 * A hint that forces using the whole line for the corresponding chunk even if only part
 * of the line was detected as the chunk. 
 * @author burgetr
 */
public class HintWholeBox implements PresentationHint
{

    public HintWholeBox()
    {
    }
    
    @Override
    public List<Area> apply(List<Area> areas)
    {
        for (Area a : areas)
        {
            if (a instanceof TextChunkArea)
            {
                TextChunkArea chunk = (TextChunkArea) a;
                String ta = chunk.getText().trim();
                String boxText = chunk.getSourceBox().getOwnText();
                String tb = boxText.trim();
                if (ta.length() != tb.length())
                {
                    chunk.setText(tb);
                    chunk.setBounds(chunk.getSourceBox().getSubstringBounds(0, boxText.length()));
                    chunk.setName(chunk.getName() + "(ext)");
                }
            }
        }
        return areas;
    }

    @Override
    public String toString()
    {
        return "WholeLine";
    }
    
}
