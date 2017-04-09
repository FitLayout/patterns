/**
 * AttributeGroupMatcher.java
 *
 * Created on 7. 4. 2017, 11:17:05 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.AreaStyle;
import org.fit.layout.patterns.model.TagConnection;
import org.fit.layout.patterns.model.TagConnectionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author burgetr
 */
public class AttributeGroupMatcher extends BaseMatcher
{
    private static Logger log = LoggerFactory.getLogger(AttributeGroupMatcher.class);

    private List<Attribute> attrs;
    private List<Area> areas;
    private List<StyleCounter<AreaStyle>> styleStats;
    private RelationAnalyzer pa;

    
    public AttributeGroupMatcher(List<Attribute> attrs)
    {
        this.attrs = attrs;
    }

    public List<Tag> getTags()
    {
        List<Tag> ret = new ArrayList<>(attrs.size());
        for (Attribute a : attrs)
            ret.add(a.getTag());
        return ret;
    }
    
    @Override
    public List<List<Area>> match(List<Area> areas)
    {
        this.areas = areas;
        gatherStatistics();
        
        log.debug("Styles:");
        for (int i = 0; i < attrs.size(); i++)
        {
            log.debug("Styles {}: {}", attrs.get(i).getTag(), styleStats.get(i));
        }
        
        List<Configuration> best = scanDisambiguations();
        for (Configuration conf : best)
            log.debug("Best:{}", conf);
        /*if (!best.isEmpty())
        {
            log.debug("Using:{}", best.get(0));
            
            StyleAnalyzerClassify cls = createClassifyingAnalyzer(best.get(0));
            cls.dumpToFile("/tmp/matches-" + srcTag[0].getValue() + "-" + srcTag[1].getValue() + ".arff");
            
            return getMatches(best.get(0));
        }
        else
            return null;*/
        return null;
    }
    
    //==============================================================================================
    
    private List<Configuration> scanDisambiguations()
    {
        List<Configuration> all = generateConfigurations();
        int bestCoverage = 0;
        
        //select the best configurations
        List<Configuration> best = new ArrayList<>();
        for (Configuration conf : all)
        {
            if (conf.getCoverage() == bestCoverage)
                best.add(conf);
        }
        
        return best;
    }
    
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
    
    private List<Configuration> generateConfigurations()
    {
        List<Map<Tag, AreaStyle>> styleMaps = generateStyleMaps(0.1f);
        List<List<TagConnection>> tagPairs = generateTagPairs(0.33f);
        for (Map<Tag, AreaStyle> styles : styleMaps)
        {
            //TODO
        }
        return new ArrayList<>();
    }
    
    /**
     * Generates all possible mappings from tags to styles for the given minimal frequency of tag instances with the given style.
     * @param minFrequency the minimal frequency of tags required to consider the style for that tag
     * @return A list of style mappings.
     */
    private List<Map<Tag, AreaStyle>> generateStyleMaps(float minFrequency)
    {
        //lists of used styles for the individual tags
        int totalStyles = 1;
        AreaStyle styles[][] = new AreaStyle[attrs.size()][];
        for (int i = 0; i < attrs.size(); i++)
        {
            List<AreaStyle> variants = new ArrayList<AreaStyle>(styleStats.get(i).getFrequentSyles(minFrequency));
            if (getUseStyleWildcards() > 0)
                variants.addAll(createStyleCombinations(variants, getUseStyleWildcards()));
            if (variants.isEmpty())
            {
                log.error("No styles found for {}", attrs.get(i).getTag());
                return new ArrayList<>();
            }
            log.debug("Trying for {}: {}", attrs.get(i).getTag(), variants);
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
     * Generates all possible mappings from tags to styles for the given minimal frequency of tag instances with the given style.
     * @param minFrequency the minimal frequency of tags required to consider the style for that tag
     * @return A list of style mappings.
     */
    private List<List<TagConnection>> generateTagPairs(float minFrequency)
    {
        TagConnectionList all = pa.getTagConnections();
        PatternCounter<TagConnection> ccon = new PatternCounter<>();
        ccon.addAll(all, 1.0f);
        
        TagConnectionList used = new TagConnectionList(ccon.getFrequent(minFrequency));
        
        log.debug("{} tag connections", ccon);
        log.debug("{} used", used.size());
        
        List<List<Attribute>> perms = permutateAttributes(attrs);
        
        List<List<Attribute>> seqs = new ArrayList<>(perms.size() / 2);
        while (!perms.isEmpty())
        {
            List<Attribute> item = perms.remove(0);
            List<Attribute> rev = new ArrayList<>(item);
            Collections.reverse(rev);
            perms.remove(rev);
            seqs.add(item);
        }
        System.out.println(seqs.size());
        for (List<Attribute> perm : seqs)
            System.out.println("P: " + perm);
        
        
        
        //TODO
        return null;
    }
    
    public List<List<Attribute>> permutateAttributes(List<Attribute> original)
    {
        if (original.size() == 0)
        {
            List<List<Attribute>> result = new ArrayList<>();
            result.add(new ArrayList<Attribute>());
            return result;
        }
        else
        {
            Attribute firstElement = original.remove(0);
            List<List<Attribute>> returnValue = new ArrayList<>();
            List<List<Attribute>> permutations = permutateAttributes(original);
            for (List<Attribute> smallerPermutated : permutations)
            {
                for (int index = 0; index <= smallerPermutated.size(); index++)
                {
                    List<Attribute> temp = new ArrayList<>(smallerPermutated);
                    temp.add(index, firstElement);
                    returnValue.add(temp);
                }
            }
            return returnValue;
        }
    }
    
    //===========================================================================================
    
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
        //create pattern analyzer
        pa = new RelationAnalyzerSymmetric(areas);
        //discover tag chains used for disambiguation
        /*ConsistentAreaAnalyzer ca = new ConsistentAreaAnalyzer(pa, getTags(), attrs.get(0).getMinSupport());
        chains = ca.findConsistentChains(new RelationUnder());
        chains.addAll(ca.findConsistentChains(new RelationSide()));*/
    }
    
    //===========================================================================================
    
    /**
     * An individual extraction configuration: tag styles and relationships.
     */
    private class Configuration
    {
        private Map<Tag, AreaStyle> styleMap;
        private List<TagConnection> pairs;
        private int coverage;
        
        public Configuration(Map<Tag, AreaStyle> styleMap, List<TagConnection> pairs, int coverage)
        {
            this.styleMap = styleMap;
            this.pairs = pairs;
            this.coverage = coverage;
        }

        public Map<Tag, AreaStyle> getStyleMap()
        {
            return styleMap;
        }

        public List<TagConnection> getPairs()
        {
            return pairs;
        }

        public int getCoverage()
        {
            return coverage;
        }
        
        public void setCoverage(int coverage)
        {
            this.coverage = coverage;
        }

        @Override
        public String toString()
        {
            return getPairs() + " " + getStyleMap() + " (" + getCoverage() + " matches)";
        }
        
    }
    
    //==============================================================================================
    
    public static class Attribute
    {
        Tag tag;
        float minSupport;
        boolean required;
        boolean many;
        
        public Attribute(Tag tag, float minSupport, boolean required, boolean many)
        {
            this.tag = tag;
            this.minSupport = minSupport;
            this.required = required;
            this.many = many;
        }

        public Tag getTag()
        {
            return tag;
        }

        public float getMinSupport()
        {
            return minSupport;
        }

        public boolean isRequired()
        {
            return required;
        }

        public boolean isMany()
        {
            return many;
        }
        
        @Override
        public String toString()
        {
            return tag.toString();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((tag == null) ? 0 : tag.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Attribute other = (Attribute) obj;
            if (tag == null)
            {
                if (other.tag != null) return false;
            }
            else if (!tag.equals(other.tag)) return false;
            return true;
        }
        
    }

}
