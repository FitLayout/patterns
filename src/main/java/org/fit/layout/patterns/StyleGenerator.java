/**
 * StyleGenerator.java
 *
 * Created on 20. 6. 2018, 10:34:26 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.AttributeGroupMatcher.Attribute;
import org.fit.layout.patterns.model.AreaStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generator of styles and their combinations based on the source area styles.
 * 
 * @author burgetr
 */
public class StyleGenerator
{
    private static Logger log = LoggerFactory.getLogger(StyleGenerator.class);

    private List<Attribute> attrs;
    private List<Area> areas;
    private int maxWildcards;
    
    private List<StyleCounter<AreaStyle>> styleStats;

    
    public StyleGenerator(List<Attribute> attrs, List<Area> areas, int maxWildcards)
    {
        this.attrs = attrs;
        this.areas = areas;
        this.maxWildcards = maxWildcards;
        gatherStatistics();
    }

    public List<Attribute> getAttrs()
    {
        return attrs;
    }

    public List<Area> getAreas()
    {
        return areas;
    }
    
    public int getMaxWildcards()
    {
        return maxWildcards;
    }

    public List<StyleCounter<AreaStyle>> getStyleStats()
    {
        return styleStats;
    }

    //=========================================================================
    
    /**
     * Generates all possible mappings from tags to styles for the given minimal frequency of tag instances with the given style.
     * @param minFrequency the minimal frequency of tags required to consider the style for that tag
     * @return A list of style mappings.
     */
    public List<Map<Tag, AreaStyle>> generateStyleMaps(float minFrequency)
    {
        //lists of used styles for the individual tags
        int totalStyles = 1;
        AreaStyle styles[][] = new AreaStyle[attrs.size()][];
        for (int i = 0; i < attrs.size(); i++)
        {
            final Attribute attr = attrs.get(i);
            
            List<AreaStyle> variants = new ArrayList<AreaStyle>(styleStats.get(i).getFrequentSyles(minFrequency));
            if (getMaxWildcards() > 0)
                variants.addAll(createStyleCombinations(variants, getMaxWildcards()));
            if (variants.isEmpty())
            {
                log.error("No styles found for {}", attr.getTag());
                return new ArrayList<>();
            }
            log.debug("Trying for {}: {}", attr.getTag(), variants);
            styles[i] = variants.toArray(new AreaStyle[0]);
            totalStyles = totalStyles * styles[i].length;
        }
        //generate style combinations
        List<Map<Tag, AreaStyle>> styleMaps = new ArrayList<>(totalStyles);
        int indices[] = new int[attrs.size()];
        Arrays.fill(indices, 0);
        while (indices[indices.length - 1] < styles[indices.length - 1].length)
        {
            //create the style map for this iteration
            Map<Tag, AreaStyle> curStyles = new HashMap<Tag, AreaStyle>(attrs.size());
            for (int i = 0; i < attrs.size(); i++)
                curStyles.put(attrs.get(i).getTag(), styles[i][indices[i]]);
            styleMaps.add(curStyles);
            
            //increment the indices
            indices[0]++;
            for (int i = 0; i < indices.length - 1; i++)
            {
                if (indices[i] >= styles[i].length)
                {
                    indices[i] = 0;
                    indices[i+1]++;
                }
            }
        }
        log.debug("{} style combinations", totalStyles);
        return styleMaps;
    }
    
    /**
     * Takes all pairs from the given list of styles and creates a set of generalized styles 
     * corresponding to the pairs using at most {@code maxWildcards} wildcards.
     * @param styles the list of styles
     * @param maxWildcards maximal number of wildcards used
     * @return a set of generalized styles containing at most {@code maxWildcards} wildcards
     */
    private Set<AreaStyle> createStyleCombinations(List<AreaStyle> styles, int maxWildcards)
    {
        Set<AreaStyle> ret = new HashSet<>();
        for (int i = 0; i < styles.size(); i++)
        {
            AreaStyle s1 = styles.get(i);
            for (int j = 0; j < styles.size(); j++)
            {
                if (i != j)
                {
                    AreaStyle s2 = styles.get(j);
                    if (s1.getEditingDistance(s2) <= maxWildcards)
                    {
                        AreaStyle gen = new AreaStyle(s1);
                        gen.generalizeToFit(s2);
                        ret.add(gen);
                    }
                }
            }
        }
        return ret;
    }
    
    //=========================================================================
    
    private void gatherStatistics()
    {
        //count styles
        styleStats = new ArrayList<>(attrs.size());
        for (int i = 0; i < attrs.size(); i++)
            styleStats.add(new StyleCounter<AreaStyle>());
        for (Area a : areas)
        {
            for (int i = 0; i < attrs.size(); i++)
            {
                if (a.hasTag(attrs.get(i).getTag(), attrs.get(i).getMinSupport()))
                    styleStats.get(i).add(new AreaStyle(a));
            }
        }
    }
    
}
