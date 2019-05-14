/**
 * AreaConnectionList.java
 *
 * Created on 9. 4. 2017, 12:10:30 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;

import org.fit.layout.model.Area;

/**
 * 
 * @author burgetr
 */
public class AreaConnectionList extends ConnectionList<Area, AreaConnection>
{
    private static final long serialVersionUID = 1L;
    
    public AreaConnectionList()
    {
        super();
    }
    
    public AreaConnectionList(int size)
    {
        super(size);
    }
    
    public AreaConnectionList(List<AreaConnection> src)
    {
        super(src);
    }

}
