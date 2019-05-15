/**
 * Case.java
 *
 * Created on 13. 5. 2019, 14:39:29 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
public class Case
{
    private int size;
    private Map<Tag, Area> areas;
    private AreaConnectionList conns;
    
    public Case(int size)
    {
        this.size = size;
        areas = new HashMap<>();
        conns = new AreaConnectionList(size - 1);
    }

    public int getSize()
    {
        return size;
    }

    public void addConnection(TagPair pair, AreaConnection con)
    {
        areas.put(pair.getO1(), con.getA1());
        areas.put(pair.getO2(), con.getA2());
        conns.add(con);
    }
    
    public String toString()
    {
        String ret = "";
        for (AreaConnection con : conns)
        {
            if (!ret.isEmpty())
                ret += ",";
            ret += con;
        }
        return ret;
    }
    
}
