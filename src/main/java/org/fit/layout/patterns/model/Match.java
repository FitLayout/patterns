/**
 * Match.java
 *
 * Created on 12. 7. 2017, 19:32:29 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

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
    
    private List<AreaConnection> areaConnections;
    private List<Match> subMatches;
    
    /**
     * Creates a new empty match.
     */
    public Match()
    {
        super();
        areaConnections = new ArrayList<>();
        subMatches = new ArrayList<>();
    }
    
    /**
     * Creates a copy of another match.
     * @param src The source match.
     */
    public Match(Match src)
    {
        super(src);
        areaConnections = new ArrayList<>(src.getAreaConnections());
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
    
    public void addAreaConnection(AreaConnection con)
    {
        areaConnections.add(con);
    }
    
    public List<AreaConnection> getAreaConnections()
    {
        return areaConnections;
    }
    
    public List<AreaConnection> getConnectionsForTag(Tag t)
    {
        List<AreaConnection> ret = new ArrayList<>();
        List<Area> areas = get(t);
        for (Area a : areas)
        {
            for (AreaConnection con : areaConnections)
            {
                if (con.getA1().equals(a))
                    ret.add(con);
            }
        }
        return ret;
    }
    
    public float getAverageConnectionWeight()
    {
        if (areaConnections.size() > 0)
        {
            float sum = 0;
            for (AreaConnection con : areaConnections)
                sum += con.getWeight();
            return sum / areaConnections.size();
        }
        else
            return 0;
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
        
        areaConnections.addAll(other.getAreaConnections());
        subMatches.addAll(other.getSubMatches());
    }
}