/**
 * Match.java
 *
 * Created on 12. 7. 2017, 19:32:29 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.AreaUtils;

/**
 * This class represents a single result of matching. It is generally a map that
 * assigns a list of visual areas to the individual tags. Moreover, the area
 * relations used in this match may be stored as well for further statistical
 * evaluation.
 * 
 * @author burgetr
 */
public class Match extends HashMap<Tag, List<Area>>
{
    private static final long serialVersionUID = 1L;
    
    private List<AreaConnection> areaConnections1;
    private List<AreaConnection> areaConnectionsM1;
    private List<AreaConnection> areaConnections1M;
    private List<Match> subMatches;
    
    /**
     * Creates a new empty match.
     */
    public Match()
    {
        super();
        areaConnections1 = new ArrayList<>();
        areaConnectionsM1 = new ArrayList<>();
        areaConnections1M = new ArrayList<>();
        subMatches = new ArrayList<>();
    }
    
    /**
     * Creates a copy of another match.
     * @param src The source match.
     */
    public Match(Match src)
    {
        super(src);
        areaConnections1 = new ArrayList<>(src.getAreaConnections1());
        areaConnectionsM1 = new ArrayList<>(src.getAreaConnectionsM1());
        areaConnections1M = new ArrayList<>(src.getAreaConnections1M());
        subMatches = new ArrayList<>(src.getSubMatches());
    }

    /**
     * Assigns a single area to a tag. This is a convenience method that creates
     * a simple list containing the single area and assigns it to the given tag.
     * If there were other areas assigned to the same tag, they will be removed.
     * @param tag The tag that we assign to.
     * @param a The single area that will be assigned.
     */
    public void putSingle(Tag tag, Area a)
    {
        List<Area> list = new ArrayList<>(1);
        list.add(a);
        put(tag, list);
    }
    
    /**
     * Obtains the first area assigned to a given tag.
     * @param tag The tag.
     * @return The area or {@code null} when there are no areas assigned to the tag.
     */
    public Area getSingle(Tag tag)
    {
        List<Area> list = get(tag);
        if (list == null)
            return null;
        else
        {
            if (list.isEmpty())
                return null;
            else
                return list.get(0);
        }
    }
    
    public void addAreaConnection(AreaConnection con, boolean a1Many, boolean a2Many)
    {
        if (a1Many)
            areaConnectionsM1.add(con);
        else if (a2Many)
            areaConnections1M.add(con);
        else
            areaConnections1.add(con);
    }
    
    public List<AreaConnection> getAreaConnections1()
    {
        return areaConnections1;
    }
    
    public List<AreaConnection> getAreaConnectionsM1()
    {
        return areaConnectionsM1;
    }
    
    public List<AreaConnection> getAreaConnections1M()
    {
        return areaConnections1M;
    }
    
    public List<AreaConnection> getConnectionsForTag(Tag t)
    {
        List<AreaConnection> ret = new ArrayList<>();
        List<Area> areas = get(t);
        for (Area a : areas)
        {
            for (AreaConnection con : areaConnections1)
            {
                if (con.getA1().equals(a))
                    ret.add(con);
            }
            for (AreaConnection con : areaConnectionsM1)
            {
                if (con.getA1().equals(a))
                    ret.add(con);
            }
            for (AreaConnection con : areaConnections1M)
            {
                if (con.getA1().equals(a))
                    ret.add(con);
            }
        }
        return ret;
    }
    
    public float getAverageConnectionWeight()
    {
        final int cnt = areaConnections1.size() + areaConnectionsM1.size() + areaConnections1M.size();
        if (cnt > 0)
        {
            float sum = 0;
            for (AreaConnection con : areaConnections1)
                sum += con.getWeight();
            for (AreaConnection con : areaConnectionsM1)
                sum += con.getWeight();
            for (AreaConnection con : areaConnections1M)
                sum += con.getWeight();
            return AreaUtils.statRound(sum / cnt);
        }
        else
            return 0;
    }
    
    public void dumpConnectionWeights()
    {
        System.out.println("Match " + this);
        for (AreaConnection con : areaConnections1)
            System.out.println("  1:1 " + con.getWeight() + " [" + con + "]");
        for (AreaConnection con : areaConnectionsM1)
            System.out.println("  M:1 " + con.getWeight() + " [" + con + "]");
        for (AreaConnection con : areaConnections1M)
            System.out.println("  1:M " + con.getWeight() + " [" + con + "]");
    }
    
    public List<Match> getSubMatches()
    {
        return subMatches;
    }

    public void addSubMatch(Match subMatch)
    {
        subMatches.add(subMatch);
    }

    /**
     * Creates an union of this match with another match. Adds all the areas assigned
     * by the source match to this match.
     * @param other The source match.
     */
    public void union(Match other)
    {
        for (Tag t : other.keySet())
        {
            List<Area> current = get(t);
            if (current == null)
            {
                put(t, new ArrayList<Area>(other.get(t)));
            }
            else
            {
                for (Area a : other.get(t))
                {
                    if (!current.contains(a))
                        current.add(a);
                }
            }
        }
        
        areaConnections1.addAll(other.getAreaConnections1());
        areaConnectionsM1.addAll(other.getAreaConnectionsM1());
        areaConnections1M.addAll(other.getAreaConnections1M());
        subMatches.addAll(other.getSubMatches());
    }
    
    /**
     * Finds the tag that is matched to a given area by this match or its sub-matches.
     * @param a The matched area.
     * @return The corresponding tag or {@code null} when this match does not include the given area.
     */
    public Tag findArea(Area a)
    {
        //try local tags
        for (Map.Entry<Tag, List<Area>> entry : this.entrySet())
        {
            if (entry.getValue().contains(a))
                return entry.getKey();
        }
        //include dependencies
        for (Match sub : subMatches)
        {
            final Tag t = sub.findArea(a);
            if (t != null)
                return t;
        }
        return null;
    }
    
    /**
     * Checks whether the match or its sub-matches include the given area.
     * @param a The matched area.
     * @return {@code true} when this match includes the given area.
     */
    public boolean containsArea(Area a)
    {
        return (findArea(a) != null);
    }
    
    /**
     * Adds all the matched areas to a specified destination collection.
     * @param dest the destination collection
     */
    public void addAllAreasTo(Collection<Area> dest)
    {
        for (List<Area> matchAreas : values())
            dest.addAll(matchAreas);
        for (Match sub : subMatches)
            sub.addAllAreasTo(dest);
    }
    
    /**
     * Obtains a set of all the matched areas.
     * @return the set of areas
     */
    public Set<Area> getAllAreas()
    {
        Set<Area> ret = new HashSet<>();
        addAllAreasTo(ret);
        return ret;
    }
    
    /**
     * Checks whether the set of matched areas is disjoint with another collection.
     * @param areas The collection to compare with.
     * @return {@code true} when this match does not contain any area from the given collection
     */
    public boolean isDisjointWith(Collection<Area> areas)
    {
        return Collections.disjoint(areas, getAllAreas());
    }
    
}