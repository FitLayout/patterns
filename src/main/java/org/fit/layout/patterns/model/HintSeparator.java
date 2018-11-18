/**
 * HintSeparator.java
 *
 * Created on 29. 6. 2018, 15:38:44 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.fit.layout.classify.TagOccurrence;
import org.fit.layout.model.Tag;

/**
 * This hint tries to improve the recall of the tag occurrence discovery by applying
 * some regular separators.
 * 
 * @author burgetr
 */
public class HintSeparator extends DefaultHint
{
    private List<String> separators;
    private Tag tag;
    private Pattern pattern;

    
    public HintSeparator(Tag tag, List<String> separators)
    {
        this.tag = tag;
        this.separators = separators;
        
        String ps = "";
        for (String sep : separators)
        {
            if (ps.length() > 0)
                ps += "|";
            ps += Pattern.quote(sep);
        }
        pattern = Pattern.compile(ps, Pattern.CASE_INSENSITIVE);
    }

    public Tag getTag()
    {
        return tag;
    }
    
    public List<String> getSeparators()
    {
        return separators;
    }
    
    @Override
    public String toString()
    {
        return tag.toString() + " separated by '" + separators + "'";
    }

    @Override
    public List<TagOccurrence> processOccurrences(BoxText boxText, List<TagOccurrence> occurrences)
    {
        String[] regs = pattern.split(boxText.toString());
        List<String> splits = new ArrayList<>(regs.length);
        for (String s : regs)
            splits.add(s.trim());
        List<String> occ = new ArrayList<>(occurrences.size());
        for (TagOccurrence o : occurrences)
            occ.add(o.getText());
        
        int io = 0;
        int is = 0;
        while (io < occ.size() && is < splits.size())
        {
            String cur = occ.get(io);
            if (!cur.equals(splits.get(is)))
            {
                if (is + 1 < splits.size() && cur.equals(splits.get(is + 1))) //found a single missing occurrence
                {
                    System.out.println("Found missing: " + splits.get(is));
                    is++;
                }
                else //total mismatch, do nothing
                {
                    io++;
                    is++;
                }
            }
            else
            {
                io++;
                is++;
            }
        }
        //TODO add missing chunks
        return occurrences;
    }

}
