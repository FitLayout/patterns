/**
 * AttributeGroupMatcher.java
 *
 * Created on 7. 4. 2017, 11:17:05 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.fit.layout.patterns.model.ConnectionPattern;
import org.fit.layout.patterns.model.MatcherConfiguration;
import org.fit.layout.patterns.model.TagConnection;
import org.fit.layout.patterns.model.TagConnectionList;
import org.fit.layout.patterns.model.TagPair;
import org.fit.layout.patterns.model.TagPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author burgetr
 */
public class AttributeGroupMatcher extends BaseMatcher
{
    private static Logger log = LoggerFactory.getLogger(AttributeGroupMatcher.class);

    private List<Attribute> attrs; //list of all attributes
    private List<Area> areas;
    private Map<Tag, Set<Area>> tagAreas;
    private List<StyleCounter<AreaStyle>> styleStats;
    private RelationAnalyzer pa;
    
    //list of best configurations obtained by configure()
    private List<MatcherConfiguration> best;
    private MatcherConfiguration usedConf;
    
    //testing configuration
    private MatcherConfiguration tconf;

    
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
    
    public Attribute getAttrForTag(Tag tag)
    {
        for (Attribute attr : attrs)
            if (attr.getTag().equals(tag))
                return attr;
        return null;
    }

    public List<MatcherConfiguration> getBestConfigurations()
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
        for (MatcherConfiguration conf : best)
            log.debug("Best:{}", conf);
        log.debug("Confiuration completed.");
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
    private List<MatcherConfiguration> scanDisambiguations()
    {
        //generate all possible configurations
        List<MatcherConfiguration> all = generateConfigurations();
        log.debug("{} total configurations", all.size());
        //System.out.println(all.get(0));
        
        //find the best coverage
        int bestCoverage = 0;
        int i = 0;
        for (MatcherConfiguration conf : all)
        {
            /*String s = conf.toString();
            if (s.contains("session-above-title") && s.contains("persons-below-title") && s.contains("pages-onRight-title"))
                System.out.println("jo!");*/
            
            if (tconf != null)
            {
                if (tconf.getPattern().equals(conf.getPattern()))
                    log.debug("Partial match {}", conf);
                if (!tconf.equals(conf))
                    continue;
            }
            
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
        List<MatcherConfiguration> best = new ArrayList<>();
        if (bestCoverage > 0)
        {
            for (MatcherConfiguration conf : all)
            {
                if (conf.getCoverage() == bestCoverage)
                    best.add(conf);
            }
        }
        
        return best;
    }
    
    /**
     * Generates all the possible configurations that are applicable for the current area list.
     * Creates all possible combinations of connection patterns and style maps supported
     * by the underlying data.
     * @return A list of configurations.
     */
    private List<MatcherConfiguration> generateConfigurations()
    {
        List<MatcherConfiguration> ret = new ArrayList<>();
        List<Map<Tag, AreaStyle>> styleMaps = generateStyleMaps(0.1f);
        Set<ConnectionPattern> patterns = generateConnectionPatterns(0.75f);
        for (Map<Tag, AreaStyle> styles : styleMaps) //for all style maps
        {
            for (ConnectionPattern conns : patterns)
            {
                MatcherConfiguration conf = new MatcherConfiguration(styles, conns, 0);
                ret.add(conf);
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
     * Generates all supported connection patterns for the given minimal frequency of tag instances.
     * @param minFrequency the minimal frequency of tag connections required to consider the tag connection
     * @return A set of generated connection patterns.
     */
    private Set<ConnectionPattern> generateConnectionPatterns(float minFrequency)
    {
        TagConnectionList all = pa.getTagConnections();

        //Avoid M:1 relationships because our matcher only expects 1:1 or 1:M.
        //The M:1 relationships may be expressed as 1:M for swapped tags.
        //Create a blacklist for M:1 tags
        Set<Tag> tagBlacklist = new HashSet<>();
        for (Attribute att : attrs)
        {
            if (att.isSrcMany() && !att.isMany())
                tagBlacklist.add(att.getTag());
        }
        
        Set<TagPattern> patterns = findConnectedTagPatterns(attrs, tagBlacklist, all);
        log.debug("Attribute patterns: {}", patterns.size());
        
        Set<ConnectionPattern> ret = new HashSet<>();
        int total = 0;
        for (TagPattern pattern : patterns)
        {
            log.debug("P: " + pattern);
            
            /*String s = pattern.toString();
            if (s.contains("<session, title>") && s.contains("<persons, title>") && s.contains("<title, pages>"))
                System.out.println("jo!");*/
            
            List<ConnectionPattern> mappings = findMappings(pattern, all, minFrequency);
            ret.addAll(mappings);
            total += mappings.size();
        }
        log.debug("Connection patterns: found {}, unique {}", total, ret.size());
        
        return ret;
    }
    
    /**
     * Finds all the connection patterns that match a given tag pattern based on the existing connection list.
     * This generates all the supported combinations of relations between the tag pairs.
     * @param pattern The tag pattern to be used
     * @param allConnections List of all connections to be used
     * @param minFrequency Minimal frequency of the connection that should be considered
     * @return A list of all the connection patterns that correspond to the tag pattern
     */
    private List<ConnectionPattern> findMappings(TagPattern pattern, TagConnectionList allConnections, float minFrequency)
    {
        List<ConnectionPattern> ret = new ArrayList<>();
        //find candidates for every pair
        List<List<TagConnection>> lists = new ArrayList<>(pattern.size());
        for (TagPair pair : pattern)
        {
            ConnectionList<Tag, TagConnection> cands = allConnections.filterForPair(pair);
            PatternCounter<TagConnection> cnt = new PatternCounter<>(cands, 1.0f);
            List<TagConnection> frequent = cnt.getFrequent(minFrequency); 
            lists.add(frequent);
            log.debug("    for {}-{} : {}", pair.getO1(), pair.getO2(), cnt);
            log.debug("      used {}", frequent);
        }
        //iterate over all combinarions of candidates
        int[] indices = new int[lists.size()];
        Arrays.fill(indices, 0);
        final int lastcnt = lists.get(lists.size() - 1).size();
        while (indices[indices.length-1] != lastcnt)
        {
            ConnectionPattern newItem = new ConnectionPattern(indices.length);
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
        log.debug("    {} combinarions used", ret.size());
        return ret;
    }
    
    //===========================================================================================
    
    /**
     * Discovers all usable tag patterns from the list of connections.
     * @param attlist the list of attributes to consider
     * @param allConnections the connections to consider
     * @return a list of tag patterns
     */
    private Set<TagPattern> findConnectedTagPatterns(List<Attribute> attlist, Set<Tag> tagBlacklist, TagConnectionList allConnections)
    {
        //find the pairs that are not blacklisted
        Set<TagPattern> ret = new HashSet<>();
        for (Attribute att : attlist)
        {
            Tag tag = att.getTag();
            //select distinct pairs for the tag
            Set<TagPair> pairs = findDistinctPairsForStartTag(tag, allConnections);
            //recursively scan other connections
            for (TagPair pair : pairs)
            {
                if (!tagBlacklist.contains(pair.getO1()))
                {
                    TagPattern seed = new TagPattern(attlist.size() - 1);
                    seed.add(pair);
                    recursiveAddConnected(seed, attlist, tagBlacklist, allConnections, ret);
                }
                else
                    log.debug("Blacklisted (M:1): {}", pair);
            }
        }
        return ret;
    }
    
    /**
     * Takes a tag pattern and recursively scans for all pairs that may be added based on the existing connection list.
     * When the pattern is complete, it is stored to a destination pattern collection. 
     * @param current current (incomplete) tag pattern
     * @param attlist list of attributes to consider
     * @param tagBlacklist blacklisted tags that should not be the first in the pair
     * @param allConnections list of tag connections to consider
     * @param dest the destination pattern collection
     */
    private void recursiveAddConnected(TagPattern current, List<Attribute> attlist, Set<Tag> tagBlacklist, TagConnectionList allConnections, Collection<TagPattern> dest)
    {
        //try to connect all the tags already covered by the tag pattern
        for (Tag tag : current.getTags())
        {
            Set<TagPair> pairs = findDistinctPairsForStartTag(tag, allConnections);
            for (TagPair pair : pairs)
            {
                if (!tagBlacklist.contains(pair.getO1()) && current.mayAdd(pair)) //a new pair may be added
                {
                    TagPattern next = new TagPattern(current);
                    next.add(pair);
                    if (next.size() >= attlist.size() - 1) //the pattern is complete, store it
                    {
                        dest.add(next);
                    }
                    else //incomplete pair, continue the search recursively
                    {
                        recursiveAddConnected(next, attlist, tagBlacklist, allConnections, dest);
                    }
                }
            }
        }
    }
    
    /**
     * Finds all distinct pairs of tags in a connection list with the given source tag.
     * @param sourceTag the source tag (the second tag in the connection)
     * @param allConnections the list of tag connections to use
     * @return Resulting set of pairs.
     */
    private Set<TagPair> findDistinctPairsForStartTag(Tag sourceTag, TagConnectionList allConnections)
    {
        Set<TagPair> pairs = new HashSet<>();
        for (TagConnection cand : allConnections.filterForSecondNode(sourceTag))
        {
            if (!cand.getA1().equals(cand.getA2())) //exclude reflexive pairs
                pairs.add(cand.toPair());
        }
        return pairs;
    }
    
    //===========================================================================================
    
    /**
     * Checks how many visual areas are covered with the given configuration.
     * @param conf The configuration to check.
     * @param dis The disambiguator for mapping areas to tags.
     * @return The number of visual areas that match the given configuration.
     */
    private int checkCovering(MatcherConfiguration conf, Disambiguator dis)
    {
        Set<Area> matchedAreas = new HashSet<Area>();
        List<TagConnection> pairs = new ArrayList<>(conf.getPattern()); //pairs to go
        Map<Tag, Area> match = new HashMap<>(); 
        List<Map<Tag, Area>> matches = new ArrayList<>();
        TagConnection curPair = pairs.remove(0);
        Set<Area> srcSet = tagAreas.get(curPair.getA2());
        //System.out.println("src set: " + srcSet.size());
        for (Area a : srcSet)
        {
            match.put(curPair.getA2(), a);
            recursiveFindMatchesFor(a, curPair, pairs, dis, match, matches, matchedAreas);
        }
        if (matches.size() > 100)
        {
            for (Map<Tag, Area> m : matches)
                System.out.println("  Match: " + m);
            System.out.println("jo!");
        }
        return matches.size();
    }
    
    private boolean recursiveFindMatchesFor(Area a, TagConnection curPair, List<TagConnection> pairs, Disambiguator dis, Map<Tag, Area> curMatch, List<Map<Tag, Area>> matches, Set<Area> matchedAreas)
    {
        List<Area> inrel = getAreasInBestRelation(a, curPair.getRelation(), curPair.getA2(), curPair.getA1(), dis);
        Set<Area> destSet = tagAreas.get(curPair.getA1());
        boolean anyMatched = false;
        for (Area b : inrel)
        {
            if (destSet.contains(b) && !curMatch.containsValue(b) && !matchedAreas.contains(b))
            {
                //create the new candidate match
                Map<Tag, Area> nextMatch = new HashMap<Tag, Area>(curMatch);
                nextMatch.put(curPair.getA1(), b);
                
                //test if the match is complete
                boolean matched = false;
                if (!pairs.isEmpty()) //some pairs are remaining -- continue recursvely
                {
                    //find the next pair
                    List<TagConnection> nextPairs = new ArrayList<>(pairs);
                    TagConnection nextPair = null;
                    for (int i = 0; nextPair == null && i < nextPairs.size(); i++)
                    {
                        Tag destTag = nextPairs.get(i).getA2();
                        if (nextMatch.containsKey(destTag)) //found a connected pair
                            nextPair = nextPairs.remove(i);
                    }
                    if (nextPair != null)
                    {
                        Area seed = nextMatch.get(nextPair.getA2());
                        matched = recursiveFindMatchesFor(seed, nextPair, nextPairs, dis, nextMatch, matches, matchedAreas);
                    }
                    else
                    {
                        log.error("No next pair found but some are remaining?");
                        matched = false;
                    }
                }
                else //no pairs remaining -- a complete match
                {
                    log.debug("Adding: " + nextMatch);
                    matched = true;
                    matches.add(nextMatch);
                    matchedAreas.addAll(nextMatch.values());
                }
                
                if (matched) //successfully matched until the end of the sequence
                {
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
        //create the tagt to area mapping
        tagAreas = createAttrTagMap();
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
    
    /**
     * Creates a mapping from the tags specified by the individual attributes to sets of related areas.
     * @return a mapping from tags to sets of areas
     */
    private Map<Tag, Set<Area>> createAttrTagMap()
    {
        Map<Tag, Set<Area>> areaMap = new HashMap<>(attrs.size());
        for (Attribute attr : attrs)
        {
            Tag tag = attr.getTag();
            Set<Area> areaSet = new HashSet<Area>();
            for (Area a : areas)
            {
                if (a.hasTag(attr.getTag(), attr.getMinSupport()))
                    areaSet.add(a);
            }
            areaMap.put(tag, areaSet);
        }
        return areaMap;
    }
    
    //===========================================================================================
    
    /**
     * Creates a testing configuration used for debugging on Vol-1317.
     * @param areas
     * @return
     */
    private MatcherConfiguration createTestingConfiguration(List<Area> areas)
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
        
        ConnectionPattern conn = new ConnectionPattern(3);
        //conn.add(new TagConnection(tsession, ttitle, new RelationBelow(true), 1.0f));
        conn.add(new TagConnection(ttitle, tsession, new RelationBelow(false), 1.0f));
        conn.add(new TagConnection(tpersons, ttitle, new RelationBelow(false), 1.0f));
        conn.add(new TagConnection(tpages, ttitle, new RelationSameLine(), 1.0f));
        
        return new MatcherConfiguration(styleMap, conn, 0);
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
        boolean srcMany;
        
        /**
         * Creates a new attribute with the given properties.
         * @param tag the associated tag
         * @param minSupport
         * @param required is the attribute required for the entity?
         * @param many are there multiple values acceptable for a single entity?
         * @param srcMany may multiple entities share an identical value of the attribute?
         */
        public Attribute(Tag tag, float minSupport, boolean required, boolean many, boolean srcMany)
        {
            this.tag = tag;
            this.minSupport = minSupport;
            this.required = required;
            this.many = many;
            this.srcMany = srcMany;
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
        
        public boolean isSrcMany()
        {
            return srcMany;
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
