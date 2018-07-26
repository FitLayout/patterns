/**
 * PatternGenerator.java
 *
 * Created on 20. 6. 2018, 11:02:14 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.ConnectionList;
import org.fit.layout.patterns.model.ConnectionPattern;
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
public class PatternGenerator
{
    private static Logger log = LoggerFactory.getLogger(PatternGenerator.class);

    private AttributeGroupMatcher matcher;
    private RelationAnalyzer pa;
    
    
    public PatternGenerator(AttributeGroupMatcher matcher, RelationAnalyzer pa)
    {
        this.matcher = matcher;
        this.pa = pa;
    }

    //===========================================================================================

    /**
     * Generates all supported connection patterns for the given minimal frequency of tag instances.
     * @param minFrequency the minimal frequency of tag connections required to consider the tag connection
     * @return A set of generated connection patterns.
     */
    public Set<ConnectionPattern> generateConnectionPatterns(float minFrequency)
    {
        TagConnectionList all = pa.getTagConnections();
        
        Set<TagPattern> patterns = findConnectedTagPatterns(all, matcher.getUsedTags(), matcher.getTagsWithDependencies());
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
                    if (matcher.getPairBlacklist().contains(pair))
                    {
                        log.debug("Blacklisted (M:N): {}", pair);
                    }
                    else if (matcher.getTagBlacklist().contains(pair.getO1()))
                    {
                        log.debug("Blacklisted (M:1): {}", pair);
                    }
                    else
                    {
                        TagPattern seed = new TagPattern(matcher.getUsedTags().size() - 1);
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
            TagConnection dep = matcher.getDependencyTagConnection(pair);
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
                TagConnection rdep = matcher.getDependencyTagConnection(pair.reverse());
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
                    if (!matcher.getTagBlacklist().contains(pair.getO1()) && !matcher.getPairBlacklist().contains(pair) && current.mayAdd(pair)) //a new pair may be added
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
    
}
