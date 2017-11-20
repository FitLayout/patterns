/**
 * AttributeGroupMatcher.java
 *
 * Created on 7. 4. 2017, 11:17:05 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.graph.Group;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.AreaStyle;
import org.fit.layout.patterns.model.ConnectionList;
import org.fit.layout.patterns.model.ConnectionPattern;
import org.fit.layout.patterns.model.Match;
import org.fit.layout.patterns.model.MatchResult;
import org.fit.layout.patterns.model.MatchStatistics;
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
    
    /** Maximal number of best performing configurations returned */
    private static final int BEST_CANDIDATE_LIMIT = 200;
    /** Minimal support of a style in the source data for using it in generated configurations */
    private static final float MIN_SUPPORT_STYLE = 0.1f;
    /** Minimal support of the relation in the source data for using it in generated configurations */
    private static final float MIN_SUPPORT_RELATIONS = 0.45f;

    private List<Attribute> attrs; //list of all attributes
    private List<AttributeGroupMatcher> dependencies; //already configured group matchers
    private Group group; //the group this matcher was created for (for tracking cardinalities etc.)
    
    private Set<Tag> usedTags; //set of tags used by the attributes
    private int keyAttr; //key attribute index or -1 if none
    private Set<Tag> tagBlacklist; //tags that should not be the first one in the pairs in order to avoid M:1 connections
    private Set<TagPair> pairBlacklist; //disallowed tag pairs in order to avoid M:N connections
    
    //areas and statistics used for configuration
    private List<Area> areas;
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
        scanAttributes();
    }

    public List<AttributeGroupMatcher> getDependencies()
    {
        return dependencies;
    }

    public void setDependencies(List<AttributeGroupMatcher> dependencies)
    {
        this.dependencies = dependencies;
    }

    public Group getGroup()
    {
        return group;
    }

    public void setGroup(Group group)
    {
        this.group = group;
    }

    public Set<Tag> getUsedTags()
    {
        return usedTags;
    }
    
    public List<Attribute> getAttrs()
    {
        return attrs;
    }
    
    public Attribute getKeyAttr()
    {
        if (keyAttr == -1)
            return null;
        else
            return attrs.get(keyAttr);
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
        {
            usedConf = best.get(index);
            usedConf.getResult().dumpMatchAverages();
        }
        else
            log.error("Cannot use non-existing configuration index {}", index);
    }
    
    public MatcherConfiguration getUsedConf()
    {
        return usedConf;
    }
    
    /**
     * Finds a style for the given tag in dependencies that have been already resolved.
     * @param tag The tag to look for.
     * @return The area style or {@code null} when no such tag has been found in dependencies
     */
    public AreaStyle getDependencyStyle(Tag tag)
    {
        if (dependencies != null)
        {
            for (AttributeGroupMatcher dep : dependencies)
            {
                MatcherConfiguration dconf = dep.getUsedConf();
                if (dconf != null && dconf.getStyleMap().containsKey(tag))
                    return dconf.getStyleMap().get(tag);
            }
        }
        return null;
    }
    
    /**
     * Finds a tag connection for the given tag pair in dependencies that have been already resolved.
     * @param pair The tag pair to look for
     * @return The tag connection or {@code null} when no such tag pair has been found in dependencies
     */
    public TagConnection getDependencyTagConnection(TagPair pair)
    {
        if (dependencies != null)
        {
            for (AttributeGroupMatcher dep : dependencies)
            {
                MatcherConfiguration dconf = dep.getUsedConf();
                if (dconf != null)
                {
                    TagConnection con = dconf.getPattern().findForPair(pair);
                    if (con != null)
                        return con;
                }
            }
        }
        return null;
    }
    
    /**
     * Obtains all tags recognized by the dependent taggers.
     * @return the set of recognized tags
     */
    public Set<Tag> getDependencyTags()
    {
        Set<Tag> ret = new HashSet<>();
        if (dependencies != null)
        {
            for (AttributeGroupMatcher dep : dependencies)
                ret.addAll(dep.getUsedTags());
        }
        return ret;
    }
    
    /**
     * Obtains all the tags recognized by this tagger and the dependent taggers.
     * @return the set of tags
     */
    public Set<Tag> getTagsWithDependencies()
    {
        Set<Tag> ret = new HashSet<>(getUsedTags());
        if (dependencies != null)
        {
            for (AttributeGroupMatcher dep : dependencies)
                ret.addAll(dep.getUsedTags());
        }
        return ret;
    }
    
    public Map<Tag, List<Match>> getDependencyMatches(List<Area> areas, Disambiguator dis, Map<Tag, Set<Area>> tagAreas)
    {
        Map<Tag, List<Match>> ret = new HashMap<>();
        if (dependencies != null)
        {
            for (AttributeGroupMatcher dep : dependencies)
            {
                List<Match> result = dep.match(areas/*, dis, tagAreas*/); //TODO this should work but it does not
                for (Tag t : dep.getUsedTags())
                    ret.put(t, result);
            }
        }
        return ret;
    }
    
    public AttributeGroupMatcher findDependencyByTag(Tag tag)
    {
        if (dependencies != null)
        {
            for (AttributeGroupMatcher dep : dependencies)
            {
                Attribute local = dep.getAttrForTag(tag); 
                if (local != null)
                    return dep;
                else
                    return dep.findDependencyByTag(tag);
            }
        }
        return null;
    }
    
    public Map<Tag, AreaStyle> getCompleteUsedStyleMap()
    {
        return getCompleteStyleMap(usedConf.getStyleMap());
    }
    
    public Map<Tag, AreaStyle> getCompleteStyleMap(Map<Tag, AreaStyle> localStyleMap)
    {
        Map<Tag, AreaStyle> ret = new HashMap<>(localStyleMap);
        if (dependencies != null)
        {
            for (AttributeGroupMatcher dep : dependencies)
            {
                ret.putAll(dep.getCompleteUsedStyleMap());
            }
        }
        return ret;
    }
    
    public List<Attribute> getAllAttrs()
    {
        List<Attribute> ret = new ArrayList<>(getAttrs());
        if (dependencies != null)
        {
            for (AttributeGroupMatcher dep : dependencies)
            {
                ret.addAll(dep.getAllAttrs());
            }
        }
        return ret;
    }
    
    @Override
    public boolean isTagMany(Tag tag)
    {
        Attribute attr = getAttrForTag(tag);
        if (attr != null)
            return attr.isMany(); //local attribute
        else 
        {
            AttributeGroupMatcher dep = findDependencyByTag(tag);
            if (dep != null)
                return dep.getGroup().isMany();
            else
                return false;
        }
    }
    
    /**
     * Sets the configuration used for testing. When set, the configuration lookup will be limited
     * to the given configuration only, other configurations will be skipped.
     * @param conf The testing configuration.
     */
    public void setTestingConfiguration(MatcherConfiguration conf)
    {
        tconf = conf;
    }
    
    @Override
    public String toString()
    {
        return getAttrs().toString();
    }

    //===========================================================================================================
    
    /**
     * Checks the possible configurations on a list of areas and chooses the best ones. 
     * @param areas
     */
    public void configure(List<Area> areas)
    {
        this.areas = areas;
        gatherStatistics();
        
        if (tconf != null)
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
    public List<Match> match(List<Area> areas)
    {
        if (best == null)
        {
            log.error("Matcher not configured");
            return null;
        }
        else if (usedConf == null)
        {
            log.error("No configuration selected");
            return null;
        }
        else
        {
            log.info("Using conf {}", usedConf);
            StyleAnalyzer sa = new StyleAnalyzerFixed(getCompleteUsedStyleMap());
            Disambiguator dis = new Disambiguator(sa, null, 0.09f);
            Map<Tag, Set<Area>> tagAreas = createAttrTagMap(areas, dis);
            
            List<Match> result = match(areas, dis, tagAreas);
            return result;
        }
    }
    
    protected List<Match>  match(List<Area> areas, Disambiguator dis, Map<Tag, Set<Area>> tagAreas)
    {
        if (usedConf == null)
        {
            log.error("No configuration selected");
            return null;
        }
        else
        {
            Map<Tag, List<Match>> depMatches = getDependencyMatches(areas, dis, tagAreas);
            MatchResult result = findMatches(usedConf, dis, tagAreas, depMatches);
            /*if (getKeyAttr() != null)
                result.groupByKey(getKeyAttr().getTag());*/
            return result.getMatches();
        }
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
        MatchStatistics stats = new MatchStatistics();
        MatchResult bestMatch = null;
        int i = 0;
        for (MatcherConfiguration conf : all)
        {
            /*String s = conf.toString();
            if (s.contains("title-below-session") && s.contains("fs:24"))
                System.out.println("jo!");*/
            
            if (tconf != null)
            {
                if (tconf.getPattern().equals(conf.getPattern()))
                    log.debug("Partial match {}", conf);
                if (!tconf.equals(conf))
                    continue;
            }
            
            log.debug("Checking conf {}/{}: {}", (++i), all.size(), conf);
            
            StyleAnalyzer sa = new StyleAnalyzerFixed(getCompleteStyleMap(conf.getStyleMap()));
            Disambiguator dis = new Disambiguator(sa, null, 0.2f);
            Map<Tag, Set<Area>> tagAreas = createAttrTagMap(areas, dis);
            Map<Tag, List<Match>> depMatches = getDependencyMatches(areas, dis, tagAreas);
            MatchResult match = findMatches(conf, dis, tagAreas, depMatches);
            //check whether the match is consistent
            ConnectionPattern constraints = inferConsistencyConstraints(conf, match);
            if (constraints.size() > 0)
            {
                //some more constraints are necessary for ensuring the match consistency
                conf.setConstraints(constraints);
                match = findMatches(conf, dis, tagAreas, depMatches);
            }
            match.setStats(stats);
            conf.setResult(match);
            if (bestMatch == null || bestMatch.compareTo(match) < 0)
                bestMatch = match;
            
            log.debug("Result {}", match);
            //if (i > 100) break;
        }
        
        //select the best configurations
        List<MatcherConfiguration> best = new ArrayList<>();
        if (bestMatch != null)
        {
            //sort by result score
            all.sort(new Comparator<MatcherConfiguration>()
            {
                @Override
                public int compare(MatcherConfiguration o1, MatcherConfiguration o2)
                {
                    MatchResult r1 = o1.getResult();
                    MatchResult r2 = o2.getResult();
                    if (r1 == null && r2 == null)
                        return 0;
                    else if (r1 == null)
                        return 1;
                    else if (r2 == null)
                        return -1;
                    else
                        return r2.compareTo(r1);
                }
            });
            
            //choose first few
            best = all.subList(0, Math.min(BEST_CANDIDATE_LIMIT, all.size()));
            
            /*for (MatcherConfiguration conf : all)
            {
                if (conf.getResult() != null && conf.getResult().compareTo(bestMatch) == 0)
                    best.add(conf);
            }*/
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
        List<Map<Tag, AreaStyle>> styleMaps = generateStyleMaps(MIN_SUPPORT_STYLE);
        Set<ConnectionPattern> patterns = generateConnectionPatterns(MIN_SUPPORT_RELATIONS);
        for (Map<Tag, AreaStyle> styles : styleMaps) //for all style maps
        {
            for (ConnectionPattern conns : patterns)
            {
                MatcherConfiguration conf = new MatcherConfiguration(styles, conns, null);
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
            final Attribute attr = attrs.get(i);
            
            List<AreaStyle> variants = new ArrayList<AreaStyle>(styleStats.get(i).getFrequentSyles(minFrequency));
            if (getUseStyleWildcards() > 0)
                variants.addAll(createStyleCombinations(variants, getUseStyleWildcards()));
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
    
    /**
     * Generates all supported connection patterns for the given minimal frequency of tag instances.
     * @param minFrequency the minimal frequency of tag connections required to consider the tag connection
     * @return A set of generated connection patterns.
     */
    private Set<ConnectionPattern> generateConnectionPatterns(float minFrequency)
    {
        TagConnectionList all = pa.getTagConnections();
        Set<Tag> allInclDeps = getTagsWithDependencies();
        
        Set<TagPattern> patterns = findConnectedTagPatterns(all, usedTags, allInclDeps);
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
        int total = 1; //total combinations expected
        for (TagPair pair : pattern)
        {
            TagConnection dep = getDependencyTagConnection(pair);
            if (dep != null) //tag connection was already resolved in dependencies
            {
                List<TagConnection> depList = new ArrayList<>(1);
                depList.add(dep);
                lists.add(depList);
                log.debug("    for {}-{}", pair.getO1(), pair.getO2());
                log.debug("      used dependency {}", depList);
            }
            else //not resolved
            {
                TagConnection rdep = getDependencyTagConnection(pair.reverse());
                if (rdep != null) //reverse connection was resolved; do not resolve this pair at all
                {
                    List<TagConnection> depList = Collections.emptyList();
                    lists.add(depList);
                    total = 0;
                    log.debug("    for {}-{}", pair.getO1(), pair.getO2());
                    log.debug("      found reverse dependency, using {}", depList);
                }
                else //not resolved yet, try the most supported connections found in the page
                {
                    ConnectionList<Tag, TagConnection> cands = allConnections.filterForPair(pair);
                    PatternCounter<TagConnection> cnt = new PatternCounter<>(cands, 1.0f);
                    List<TagConnection> frequent = cnt.getFrequent(minFrequency); 
                    lists.add(frequent);
                    total = total * frequent.size();
                    log.debug("    for {}-{} : {}", pair.getO1(), pair.getO2(), cnt);
                    log.debug("      used {}", frequent);
                }
            }
        }
        //iterate over all combinarions of candidates
        if (total > 0)
        {
            int[] indices = new int[lists.size()];
            Arrays.fill(indices, 0);
            final int lastcnt = lists.get(lists.size() - 1).size();
            while (indices[indices.length-1] < lastcnt)
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
    private Set<TagPattern> findConnectedTagPatterns(TagConnectionList allConnections, Set<Tag> localTags, Set<Tag> allowedTags)
    {
        //find the pairs that are not blacklisted
        Set<TagPattern> ret = new HashSet<>();
        for (Tag tag : allowedTags)
        {
            //select distinct pairs for the tag
            Set<TagPair> pairs = findDistinctPairsForStartTag(tag, allConnections);
            //recursively scan other connections
            for (TagPair pair : pairs)
            {
                if ((localTags.contains(pair.getO1()) || localTags.contains(pair.getO2())) //one of the tags must be local
                        && allowedTags.contains(pair.getO1()) && allowedTags.contains(pair.getO2())) //all must be allowed
                {
                    if (pairBlacklist.contains(pair))
                    {
                        log.debug("Blacklisted (M:N): {}", pair);
                    }
                    else if (tagBlacklist.contains(pair.getO1()))
                    {
                        log.debug("Blacklisted (M:1): {}", pair);
                    }
                    else
                    {
                        TagPattern seed = new TagPattern(usedTags.size() - 1);
                        seed.add(pair);
                        if (localTags.size() <= 2)
                            ret.add(seed); //only two tags, no further search necessary
                        else
                            recursiveAddConnected(seed, allConnections, localTags, ret);
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * Takes a tag pattern and recursively scans for all pairs that may be added based on the existing connection list.
     * When the pattern is complete, it is stored to a destination pattern collection. 
     * @param current current (incomplete) tag pattern
     * @param attlist list of attributes to consider
     * @param allConnections list of tag connections to consider
     * @param dest the destination pattern collection
     */
    private void recursiveAddConnected(TagPattern current, TagConnectionList allConnections, Set<Tag> localTags, Collection<TagPattern> dest)
    {
        //try to connect all the tags already covered by the tag pattern
        for (Tag tag : current.getTags())
        {
            Set<TagPair> pairs = findDistinctPairsForStartTag(tag, allConnections);
            for (TagPair pair : pairs)
            {
                if (localTags.contains(pair.getO1())) //exclude unused tags
                {
                    if (!tagBlacklist.contains(pair.getO1()) && !pairBlacklist.contains(pair) && current.mayAdd(pair)) //a new pair may be added
                    {
                        TagPattern next = new TagPattern(current);
                        next.add(pair);
                        if (next.size() >= localTags.size() - 1) //the pattern is complete, store it
                        {
                            dest.add(next);
                        }
                        else //incomplete pair, continue the search recursively
                        {
                            recursiveAddConnected(next, allConnections, localTags, dest);
                        }
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
    private MatchResult findMatches(MatcherConfiguration conf, Disambiguator dis, Map<Tag, Set<Area>> tagAreas, Map<Tag, List<Match>> depMatches)
    {
        Set<Area> matchedAreas = new HashSet<Area>();
        List<TagConnection> pairs = new ArrayList<>(conf.getPattern()); //pairs to go
        List<Match> matches = new ArrayList<>();
        TagConnection curPair = pairs.remove(0);
        List<Match> deps = depMatches.get(curPair.getA2());
        if (deps != null)
        {
            for (Match dmatch : deps)
            {
                if (dmatch.isDisjointWith(matchedAreas)) //the match does not contain any already matched area
                {
                    for (Area a : dmatch.get(curPair.getA2()))
                    {
                        Match match = new Match(); 
                        //match.putSingle(curPair.getA2(), a);
                        match.addSubMatch(dmatch);
                        recursiveFindMatchesFor(a, curPair, pairs, match, conf.getConstraints(), matches, matchedAreas, dis, tagAreas, depMatches);
                    }
                }
            }
        }
        else
        {
            Set<Area> srcSet = tagAreas.get(curPair.getA2());
            //System.out.println("src set: " + srcSet.size());
            for (Area a : srcSet)
            {
                Match match = new Match(); 
                match.putSingle(curPair.getA2(), a);
                recursiveFindMatchesFor(a, curPair, pairs, match, conf.getConstraints(), matches, matchedAreas, dis, tagAreas, depMatches);
            }
        }
        return new MatchResult(matches, matchedAreas);
    }
    
    private boolean recursiveFindMatchesFor(Area a, TagConnection curPair, List<TagConnection> pairs, Match curMatch, ConnectionPattern constraints,
            List<Match> matches, Set<Area> matchedAreas, Disambiguator dis, Map<Tag, Set<Area>> tagAreas, Map<Tag, List<Match>> depMatches)
    {
        final boolean a1Many = isTagMany(curPair.getA1());
        final boolean a2Many = isTagMany(curPair.getA2());
        boolean anyMatched = false;
        List<Match> deps = depMatches.get(curPair.getA1());
        /*if (curPair.toString().contains("session"))
            System.out.println("jo!");*/
        List<AreaConnection> inrel = getAreasInBestRelation(a, curPair.getRelation(), curPair.getA2(), curPair.getA1(), a1Many, dis);
        if (deps != null)
        {
            //look for dependency matches related to the current area
            for (Match match : deps)
            {
                if (match.isDisjointWith(matchedAreas))
                {
                    final List<Area> ref = match.get(curPair.getA1());
                    //is some of the referenced areas in the relationship?
                    for (AreaConnection con : inrel)
                    {
                        if (ref.contains(con.getA1()))
                        {
                            Area b = con.getA1();
                            boolean mayUse = a1Many || !matchedAreas.contains(b); //check repeated match of a single area depending on cardinality (is B already assigned to another A)
                            if (mayUse && !curMatch.containsArea(b))
                            {
                                //create the new candidate match
                                Match nextMatch = new Match(curMatch);
                                //nextMatch.putSingle(curPair.getA1(), b);
                                nextMatch.addSubMatch(match);
                                nextMatch.addAreaConnection(con, a1Many, false);
                                
                                anyMatched |= tryNewMatch(nextMatch, pairs, constraints, matches, matchedAreas,
                                        dis, tagAreas, depMatches);
                            }
                        }
                    }
                }
            }
        }
        else
        {
            Set<Area> destSet = tagAreas.get(curPair.getA1());
            for (AreaConnection con : inrel)
            {
                Area b = con.getA1();
                boolean mayUse = a2Many || !matchedAreas.contains(b); //check repeated match of a single area depending on cardinality (is B already assigned to another A)
                if (mayUse && destSet.contains(b) && !curMatch.containsArea(b))
                {
                    //create the new candidate match
                    Match nextMatch = new Match(curMatch);
                    nextMatch.putSingle(curPair.getA1(), b);
                    nextMatch.addAreaConnection(con, false, a2Many);
                    
                    anyMatched |= tryNewMatch(nextMatch, pairs, constraints, matches, matchedAreas,
                            dis, tagAreas, depMatches);
                }
            }
        }
        return anyMatched;
    }

    private boolean tryNewMatch(Match nextMatch,
            List<TagConnection> pairs, ConnectionPattern constraints, List<Match> matches,
            Set<Area> matchedAreas, Disambiguator dis, Map<Tag, Set<Area>> tagAreas, Map<Tag, List<Match>> depMatches)
    {
        //test if the match is complete
        boolean matched = false;
        if (!pairs.isEmpty()) //some pairs are remaining -- continue recursively
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
                Area seed = nextMatch.getSingle(nextPair.getA2());
                matched = recursiveFindMatchesFor(seed, nextPair, nextPairs, nextMatch, constraints, matches, matchedAreas, dis, tagAreas, depMatches);
            }
            else
            {
                log.error("No next pair found but some are remaining?");
                matched = false;
            }
        }
        else //no pairs remaining -- a complete match
        {
            matched = true;
            if (constraints == null || matchesConstraints(nextMatch, constraints))
            {
                //log.debug("Adding: {}", nextMatch);
                matches.add(nextMatch);
                nextMatch.addAllAreasTo(matchedAreas);
            }
            else
                log.debug("Skipping inconsistent match: {}", nextMatch);
        }
        
        return matched;
    }
    
    /**
     * Obtains the best area connections with the specified second area and the
     * specified relation. The best connection means that there does not exist  
     * any better source area for this with the same destination area and a higher weight.
     * E.g. all areas below {@code a}.
     * Only the areas with specified tags are taken into account, the tags are inferred using
     * a disambiguator.
     * @param a the area to be used as {@code A2} in the area connections.
     * @param r the relation to be uses.
     * @param srcTag the tag required for the source areas (incl. {@code a})
     * @param destTag the tag required for the destination areas
     * @param allowMany when set to {@code true}, all 'best' connections will be returned. Otherwise, only the one with the greatest weight will be returned.
     * @param dis the disambiguator used for assigning the tags to areas
     * @return the list of best area connections that correspond to the above criteria
     */
    private List<AreaConnection> getAreasInBestRelation(Area a, Relation r, Tag srcTag, Tag destTag, boolean allowMany, Disambiguator dis)
    {
        Collection<AreaConnection> all = pa.getConnections(null, r, a, -1.0f);
        //if only a single match is allowed, sort the matches in order to start with the best candidates
        if (!allowMany)
        {
            List<AreaConnection> clist = (all instanceof List) ? (List<AreaConnection>) all : new ArrayList<>(all);
            Collections.sort(clist, new Comparator<AreaConnection>()
            {
                @Override
                public int compare(AreaConnection o1, AreaConnection o2)
                {
                    if (o1.getWeight() < o2.getWeight())
                        return 1;
                    else if (o1.getWeight() > o2.getWeight())
                        return -1;
                    else
                        return 0;
                }
            });
            all = clist;
        }
        //scan the candidates
        List<AreaConnection> ret = new ArrayList<>(all.size());
        for (AreaConnection cand : all)
        {
            if (destTag.equals(dis.getAreaTag(cand.getA1())))
            {
                //find the source nodes that are closer
                Collection<AreaConnection> better = pa.getConnections(cand.getA1(), r, null, cand.getWeight());
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
                {
                    ret.add(cand); //a1 has no "better" source area, use it
                    if (!allowMany)
                        break; //we have found the best one
                }
            }
        }
        return ret;
    }
    
    /**
     * Checks the consistency of the matchig result and ensures that all the matches have the same
     * relationships between each pair of attributes. If the matching result is inconsistent (there are
     * different relations used for the same pair of tags accross the matches), a set of constraints
     * (additional matching rules) is generated in order to preserve the most supported matches and
     * remove the inconsistent ones.
     * @param conf the matcher configuration used for obtaining the matching result
     * @param result the matching result to be checked
     * @return An additional connection pattern to be used to make the match result consistent. Empty
     * for fully consistnt match results.
     */
    private ConnectionPattern inferConsistencyConstraints(MatcherConfiguration conf, MatchResult result)
    {
        Tag[] tags = conf.getTags().toArray(new Tag[0]);
        Set<TagPair> mainPairs = conf.getPattern().getPairs();
        Set<TagPair> newPairs = new HashSet<>();
        for (int i = 0; i < tags.length; i++)
        {
            Tag t1 = tags[i];
            for (int j = 0; j < tags.length; j++)
            {
                Tag t2 = tags[j];
                TagPair cand = new TagPair(t1, t2);
                if (!mainPairs.contains(cand) && !mainPairs.contains(cand.reverse()))
                {
                    newPairs.add(cand);
                }
            }
        }        
        
        //scan relations between tag pairs
        ConnectionPattern constraints = new ConnectionPattern(newPairs.size());
        for (TagPair pair : newPairs)
        {
            //gather statistics
            PatternCounter<Relation> stats = new PatternCounter<>();
            for (Match match : result.getMatches())
            {
                Set<Relation> rels = getMatchRelations(match, pair.getO1(), pair.getO2());
                stats.addAll(rels, 1.0f);
            }
            //retain only the most supported matches
            if (stats.getAll().size() > 1) //if some constraint is necessary
            {
                Relation best = stats.getMostFrequent();
                log.warn("Inconsistency found for {}, best relations is {}", pair, best);
                constraints.add(new TagConnection(pair.getO1(), pair.getO2(), best, 1.0f));
            }
        }
        return constraints;
    }
    
    /**
     * Checks whether the given match complies with the given constraints.
     * @param match the match to be checked
     * @param constraints the constraints
     * @return {@code true} when the match complies with the constraints, {@code false} otherwise. 
     */
    private boolean matchesConstraints(Match match, ConnectionPattern constraints)
    {
        for (TagConnection con : constraints)
        {
            Set<Relation> found = getMatchRelations(match, con.getA1(), con.getA2());
            if (!found.contains(con.getRelation()))
                return false;
        }
        return true;
    }
    
    /**
     * Finds all different relations among two areas in the page that are specified with a particular
     * match and their tags.
     * @param match The match to be used.
     * @param t1 The tag of the first area.
     * @param t2 The tag of the second area.
     * @return A set of relationships among the first and second area that are mapped to {@code t1} and
     * {@code t2} in the {@code match}.
     */
    private Set<Relation> getMatchRelations(Match match, Tag t1, Tag t2)
    {
        Area a1 = match.getSingle(t1);
        Area a2 = match.getSingle(t2);
        if (a1 != null && a2 != null)
            return pa.getRelationsFor(a1, a2, -1.0f);
        else
            return Collections.emptySet();
    }
    
    //===========================================================================================

    /**
     * Scans the extracted attributes and updates the blacklists of disallowed tags and pairs.
     */
    private void scanAttributes()
    {
        /* M:1 relationships should be avoided because our matcher only expects 1:1 or 1:M.
         * The M:1 relationships may be expressed as 1:M for swapped tags.
         * We create a blacklist for M:1 tags */
        tagBlacklist = new HashSet<>();
        for (Attribute att : attrs)
        {
            if (att.isSrcMany() && !att.isMany())
            {
                tagBlacklist.add(att.getTag());
                log.debug("Blacklisted M tag {}", att.getTag());
            }
        }
        
        /* M:N relationships should be avoided for the same reason.
         * We create a blacklist for M:N pairs. */
        pairBlacklist = new HashSet<>();
        for (Attribute a1 : attrs)
        {
            for (Attribute a2 : attrs)
            {
                if (a1 != a2)
                {
                    if (a1.isSrcMany() && a2.isMany())
                    {
                        TagPair pair = new TagPair(a2.getTag(), a1.getTag());
                        pairBlacklist.add(pair);
                        log.debug("Blacklisted M:N pair {}", pair);
                    }
                    else if (a1.isMany())
                    {
                        TagPair pair = new TagPair(a2.getTag(), a1.getTag());
                        pairBlacklist.add(pair);
                        log.debug("Blacklisted M:* pair {}", pair);
                    }
                }
            }
        }
        
        //Find a key attribute if possible
        keyAttr = -1;
        for (int i = 0; i < attrs.size() && keyAttr == -1; i++)
        {
            Attribute att = attrs.get(i);
            if (att.isRequired() && !att.isSrcMany() && !att.isMany())
                keyAttr = i;
        }
        if (keyAttr == -1)
            log.warn("No candidate for key attribute found. The results may be ambiguous.");
        else
            log.info("Using {} as the key attribute", getKeyAttr());
        
        //update the tag sets
        usedTags = findAllTags();
    }
    
    private Set<Tag> findAllTags()
    {
        Set<Tag> ret = new HashSet<>();
        for (Attribute a : attrs)
            ret.add(a.getTag());
        return ret;
    }
    
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
        //pa = new RelationAnalyzer(areas);
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
    private Map<Tag, Set<Area>> createAttrTagMap(List<Area> areas, Disambiguator dis)
    {
        final List<Attribute> allAttrs = getAllAttrs();
        Map<Tag, Set<Area>> areaMap = new HashMap<>(allAttrs.size());
        for (Attribute attr : allAttrs)
            areaMap.put(attr.getTag(), new HashSet<Area>());
        
        for (Area a : areas)
        {
            Tag areaTag = dis.getAreaTag(a);
            if (areaTag != null)
            {
                Set<Area> tareas = areaMap.get(areaTag);
                if (tareas != null)
                    tareas.add(a);
            }
        }
        
        return areaMap;
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
            final String[] card = new String[] {"?", "", "*", "+"};
            String ret =
                    (isSrcMany() ? ">":"")
                    + tag.toString()
                    + card[(isMany()?1:0) * 2  + (isRequired()?1:0)];
            return ret;
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
