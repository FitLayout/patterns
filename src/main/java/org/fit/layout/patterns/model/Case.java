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

import org.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
public class Case
{
    private int size;
    private Map<Tag, AreaStyle> styles;
    private AreaConnectionList conns;
    
    public Case(int size)
    {
        this.size = size;
        styles = new HashMap<>();
        conns = new AreaConnectionList(size - 1);
    }

    public int getSize()
    {
        return size;
    }

    
}
