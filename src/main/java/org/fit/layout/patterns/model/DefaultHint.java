/**
 * DefaultHint.java
 *
 * Created on 6. 11. 2018, 13:47:56 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;
import java.util.Set;

import org.fit.layout.classify.TagOccurrence;
import org.fit.layout.model.Area;
import org.fit.layout.model.Box;

/**
 * A default no-op presentation hint.
 * @author burgetr
 */
public class DefaultHint implements PresentationHint
{
    private String name;
    
    public DefaultHint(String name)
    {
        this.name = name;
    }

    @Override
    public List<Box> extractBoxes(Area a, List<Box> current, Set<Area> processed)
    {
        //no changes are performed 
        return current;
    }

    @Override
    public List<TagOccurrence> processOccurrences(BoxText boxText, List<TagOccurrence> occurrences)
    {
        //no changes are performed
        return occurrences;
    }

    @Override
    public List<Area> processChunks(Area src, List<Area> chunks)
    {
        //no changes are performed
        return chunks;
    }

    @Override
    public List<Area> postprocessChunks(List<Area> src)
    {
        //no changes are performed
        return src;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DefaultHint other = (DefaultHint) obj;
        return name.equals(other.name);
    }
    
}
