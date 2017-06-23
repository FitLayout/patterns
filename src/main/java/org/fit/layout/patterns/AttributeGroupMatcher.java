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
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.AreaStyle;
import org.fit.layout.patterns.model.ConnectionList;
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
    
    //list of best configurations obtained by configure()
    private List<Configuration> best;
    private Configuration usedConf;
    
    //testing configuration
    private Configuration tconf;

    
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
    
    public List<Attribute> getAttrs()
    {
        return attrs;
    }

    public List<Configuration> getBestConfigurations()
    {
        return best;
    }
    
    public void setUsedConf(int index)
    {
        if (best != null && index >= 0 && index < best.size())
            usedConf = best.get(index);
        else
            log.error("Cannot used non-existing configuration index {}", index);
    }
    
    /**
     * Checks the possible configurations on a list of areas and chooses the best ones. 
     * @param areas
     */
    public void configure(List<Area> areas)
    {
        this.areas = areas;
        gatherStatistics();
        
        tconf = createTestingConfiguration(areas);
        log.debug("TC: {}", tconf);
        
        log.debug("Styles:");
        for (int i = 0; i < attrs.size(); i++)
        {
            log.debug("Styles {}: {}", attrs.get(i).getTag(), styleStats.get(i));
        }
        
        best = scanDisambiguations();
        for (Configuration conf : best)
            log.debug("Best:{}", conf);
    }
    
    @Override
    public List<List<Area>> match(List<Area> areas)
    {
        if (best == null)
            log.error("Matcher not configured");
        else if (usedConf == null)
            log.error("No configuration selected");
        else
        {
            log.info("Using conf {}", usedConf);
        }
            
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
    
    /**
     * Scans all possible configurations and finds the ones that cover the largest number of areas.
     * @return The list of configurations that cover the larhest number of areas.
     */
    private List<Configuration> scanDisambiguations()
    {
        //generate all possible configurations
        List<Configuration> all = generateConfigurations();
        log.debug("{} total configurations", all.size());
        //System.out.println(all.get(0));
        
        //find the best coverage
        int bestCoverage = 0;
        int i = 0;
        for (Configuration conf : all)
        {
            //if (tconf != null && !tconf.equals(conf))
            //    continue;
            
            log.debug("Checking conf {}/{}: {}", (++i), all.size(), conf);
            
            StyleAnalyzer sa = new StyleAnalyzerFixed(conf.getStyleMap());
            Disambiguator dis = new Disambiguator(sa, null, 0.3f); //TODO minSupport?
            int coverage = checkCovering(conf, dis);
            conf.setCoverage(coverage);
            if (coverage > bestCoverage)
                bestCoverage = coverage;
            
            log.debug("Result {}", coverage);
        }
        
        //select the best configurations
        List<Configuration> best = new ArrayList<>();
        if (bestCoverage > 0)
        {
            for (Configuration conf : all)
            {
                if (conf.getCoverage() == bestCoverage)
                    best.add(conf);
            }
        }
        
        return best;
    }
    
    /**
     * Generates all the possible configurations that are applicable for the current area list.
     * @return The list of configurations.
     */
    private List<Configuration> generateConfigurations()
    {
        List<Configuration> ret = new ArrayList<>();
        List<Map<Tag, AreaStyle>> styleMaps = generateStyleMaps(0.1f);
        List<List<List<TagConnection>>> tagPairs = generateTagPairs(0.75f);
        for (Map<Tag, AreaStyle> styles : styleMaps) //for all style maps
        {
            for (List<List<TagConnection>> perm : tagPairs) //for all attr permutations
            {
                for (List<TagConnection> conns : perm) //for all coverings of attr permutations by tag connections
                {
                    Configuration conf = new Configuration(styles, conns, 0);
                    ret.add(conf);
                }
            }
        }
        return ret;
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
    
    /**
     * Generates all possible mappings from tags to styles for the given minimal frequency of tag instances with the given style.
     * @param minFrequency the minimal frequency of tags required to consider the style for that tag
     * @return A list of style mappings.
     */
    private List<List<List<TagConnection>>> generateTagPairs(float minFrequency)
    {
        TagConnectionList all = pa.getTagConnections();

        //source attribute permutations
        List<List<Attribute>> perms = findAttributePermutations();
        
        log.debug("Attribute permutations: {}", perms.size());
        List<List<List<TagConnection>>> ret = new ArrayList<>(perms.size());
        int total = 0;
        for (List<Attribute> perm : perms)
        {
            log.debug("P: " + perm);
            List<List<TagConnection>> mappings = findMappings(perm, all, minFrequency);
            ret.add(mappings);
            total += mappings.size();
        }
        log.debug("Total mappings {}", total);
        
        return ret;
    }
    
    private List<List<TagConnection>> findMappings(List<Attribute> attlist, TagConnectionList conn, float minFrequency)
    {
        List<List<TagConnection>> ret = new ArrayList<>();
        List<List<TagConnection>> lists = new ArrayList<>(attlist.size() - 1);
        //find candidates for every pair
        for (int i = 0; i < attlist.size() - 1; i++)
        {
            Attribute a1 = attlist.get(i + 1);
            Attribute a2 = attlist.get(i);
            ConnectionList<Tag, TagConnection> cands = conn.filterForPair(a1.getTag(), a2.getTag());
            PatternCounter<TagConnection> cnt = new PatternCounter<>(cands, 1.0f);
            lists.add(cnt.getFrequent(minFrequency));
            log.debug("    for {}-{} : {}", a1, a2, cnt);
        }
        //iterate over all candidates
        int[] indices = new int[lists.size()];
        Arrays.fill(indices, 0);
        final int lastcnt = lists.get(lists.size() - 1).size();
        while (indices[indices.length-1] != lastcnt)
        {
            List<TagConnection> newItem = new ArrayList<>(indices.length);
            for (int i = 0; i < indices.length; i++)
                newItem.add(lists.get(i).get(indices[i]));
            ret.add(newItem);
            
            //increment the indices
            indices[0]++;
            for (int i = 0; i < indices.length - 1; i++)
            {
                if (indices[i] >= lists.get(i).size())
                {
                    indices[i] = 0;
                    indices[i+1]++;
                }
            }
        }
        return ret;
    }
    
    //===========================================================================================
    
    /**
     * Finds all permutations of the attributes while ignoring the attribute order.
     * @return A list of all different attribute permutations.
     */
    public List<List<Attribute>> findAttributePermutations()
    {
        //find all permutations
        List<List<Attribute>> perms = permutateAttributes(new ArrayList<>(attrs));
        //filter out reverse permutations
        List<List<Attribute>> seqs = new ArrayList<>(perms.size() / 2);
        while (!perms.isEmpty())
        {
            List<Attribute> item = perms.remove(0);
            List<Attribute> rev = new ArrayList<>(item);
            Collections.reverse(rev);
            perms.remove(rev);
            seqs.add(item);
        }
        return seqs;
    }
    
    private List<List<Attribute>> permutateAttributes(List<Attribute> original)
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
    
    private int checkCovering(Configuration conf, Disambiguator dis)
    {
        Map<Tag, Set<Area>> areaMap = new HashMap<>();
        Map<Tag, Float> supportMap = new HashMap<>();
        for (Attribute a : getAttrs())
        {
            areaMap.put(a.getTag(), new HashSet<Area>());
            supportMap.put(a.getTag(), a.getMinSupport());
        }
        
        //start with all areas
        for (Area a : areas)
        {
            for (Map.Entry<Tag, Set<Area>> entry : areaMap.entrySet())
            {
                if (a.hasTag(entry.getKey(), supportMap.get(entry.getKey())))
                    entry.getValue().add(a);
            }
        }
        
        return checkCovering(conf, areaMap, dis);
    }
    
    private int checkCovering(Configuration conf, Map<Tag, Set<Area>> areaMap, Disambiguator dis)
    {
        Set<Area> matchedAreas = new HashSet<Area>();
        List<TagConnection> pairs = new ArrayList<>(conf.getPairs()); //pairs to go
        TagConnection curPair = pairs.remove(0);
        Set<Area> srcSet = areaMap.get(curPair.getA2());
        for (Area a : srcSet)
        {
            recursiveFindMatchesFor(a, curPair, areaMap, pairs, dis, matchedAreas);
        }
        return matchedAreas.size();
    }
    
    private boolean recursiveFindMatchesFor(Area a, TagConnection curPair, Map<Tag, Set<Area>> areaMap, List<TagConnection> pairs, Disambiguator dis, Set<Area> matchedAreas)
    {
        List<Area> inrel = getAreasInBestRelation(a, curPair.getRelation(), curPair.getA2(), curPair.getA1(), dis);
        Set<Area> destSet = areaMap.get(curPair.getA1());
        boolean anyMatched = false;
        for (Area b : inrel)
        {
            if (destSet.contains(b))
            {
                boolean matched = false;
                if (!pairs.isEmpty()) //some pairs are remaining?
                {
                    //find a subsequent pair
                    List<TagConnection> nextPairs = new ArrayList<>(pairs);
                    TagConnection nextPair = nextPairs.remove(0);
                    /*for (int i = 0; nextPair == null && i < nextPairs.size(); i++)
                        if (nextPairs.get(i).getA2().equals(curPair.getA1()))
                            nextPair = nextPairs.remove(i);*/
                    if (nextPair != null)
                    {
                        matched = recursiveFindMatchesFor(b, nextPair, areaMap, nextPairs, dis, matchedAreas);
                    }
                    else
                    {
                        log.error("No next pair found but some are remaining?");
                        matched = false;
                    }
                }
                else //no pairs remaining -- a complete match
                    matched = true;
                
                if (matched) //successfully matched until the end of the sequence
                {
                    destSet.remove(b);
                    matchedAreas.add(b);
                    anyMatched = true;
                }
            }
        }
        return anyMatched;
    }
    
    /**
     * Obtains all the area that are in the given relation with the given area and there exists
     * no better source area for this with the same destination area and a higher weight.
     * E.g. all areas below {@code a}.
     * Only the areas with specified tags are taken into account, the tags are inferred using
     * a disambiguator.
     * @param a the area to compare
     * @param r the relation to use.
     * @param srcTag the tag required for the source areas (incl. {@code a})
     * @param destTag the tag required for the destination areas
     * @param dis the disambiguator used for assigning the tags to areas
     * @return the list of corresponding areas
     */
    private List<Area> getAreasInBestRelation(Area a, Relation r, Tag srcTag, Tag destTag, Disambiguator dis)
    {
        List<AreaConnection> dest = pa.getConnections(null, r, a, -1.0f);
        List<Area> ret = new ArrayList<Area>(dest.size());
        for (AreaConnection cand : dest)
        {
            if (destTag.equals(dis.getAreaTag(cand.getA1())))
            {
                //find the source nodes that are closer
                List<AreaConnection> better = pa.getConnections(cand.getA1(), r, null, cand.getWeight());
                boolean foundBetter = false;
                for (AreaConnection betterCand : better)
                {
                    if (srcTag.equals(dis.getAreaTag(betterCand.getA2())))
                    {
                        foundBetter = true;
                        break;
                    }
                }
                if (!foundBetter)
                    ret.add(cand.getA1()); //a1 has no "better" source area, use it
            }
        }
        return ret;
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
    public class Configuration
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
    
    private Configuration createTestingConfiguration(List<Area> areas)
    {
        Map<Tag, AreaStyle> styleMap = new HashMap<>();
        Area asession = null;
        Area atitle = null;
        Area apersons = null;
        Area apages = null;
        for (Area a : areas)
        {
            if (a.getText().equals("Technical papers"))
                asession = a;
            else if (a.getText().equals("A categorical approach to ontology alignment"))
                atitle = a;
            else if (a.getText().contains("Mossakowski"))
                apersons = a;
            else if (a.getText().equals("1-12"))
                apages = a;
        }
        Tag tsession = findTagByName("session");
        Tag ttitle = findTagByName("title");
        Tag tpersons = findTagByName("persons");
        Tag tpages = findTagByName("pages");
        styleMap.put(tsession, new AreaStyle(asession));
        styleMap.put(ttitle, new AreaStyle(atitle));
        styleMap.put(tpersons, new AreaStyle(apersons));
        styleMap.put(tpages, new AreaStyle(apages));
        
        List<TagConnection> conn = new ArrayList<>();
        conn.add(new TagConnection(tsession, ttitle, new RelationBelow(true), 1.0f));
        conn.add(new TagConnection(tpersons, ttitle, new RelationBelow(false), 1.0f));
        conn.add(new TagConnection(tpages, ttitle, new RelationSide(true), 1.0f));
        
        return new Configuration(styleMap, conn, 0);
    }
    
    private Tag findTagByName(String name)
    {
        for (Attribute a : attrs)
            if (a.getTag().getValue().equals(name))
                return a.getTag();
        return null;
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
