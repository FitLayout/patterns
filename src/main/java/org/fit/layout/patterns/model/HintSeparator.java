/**
 * HintSeparator.java
 *
 * Created on 29. 6. 2018, 15:38:44 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fit.layout.classify.TagOccurrence;
import org.fit.layout.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This hint tries to improve the recall of the tag occurrence discovery by applying
 * some regular separators.
 * 
 * @author burgetr
 */
public class HintSeparator extends DefaultHint
{
    private static Logger log = LoggerFactory.getLogger(HintSeparator.class);
    
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
        List<TagOccurrence> splitOccurrences = findOccurrencesBySeparators(boxText.toString());
        List<String> splits = new ArrayList<>(splitOccurrences.size());
        for (TagOccurrence occ : splitOccurrences)
            splits.add(occ.getText());
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
                    log.debug("Found missing by separators: {}" , splits.get(is));
                    occurrences.add(splitOccurrences.get(is));
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
        return occurrences;
    }

    private List<TagOccurrence> findOccurrencesBySeparators(String text)
    {
        List<TagOccurrence> ret = new ArrayList<>();
        Matcher match = pattern.matcher(text);
        int last = 0;
        while (match.find())
        {
            TagOccurrence occ = findOccurenceBetweeen(text, last, match.start() - 1);
            ret.add(occ);
            last = match.end();
        }
        if (last < text.length() - 1)
        {
            TagOccurrence occ = findOccurenceBetweeen(text, last, text.length() - 1);
            ret.add(occ);
        }
        return ret;
    }
    
    private TagOccurrence findOccurenceBetweeen(String text, int firstPos, int lastPos)
    {
        //trim whitespaces
        int begin = firstPos;
        while (begin < text.length() && Character.isWhitespace(text.charAt(begin)))
            begin++;
        int end = lastPos;
        while (end >= begin && Character.isWhitespace(text.charAt(end)))
            end--;
        //create occurrence
        return new TagOccurrence(text.substring(begin, end + 1), begin, 1.0f);
    }
    
}
