/**
 * TagConnectionList.java
 *
 * Created on 9. 4. 2017, 12:12:13 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;

import org.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
public class TagConnectionList extends ConnectionList<Tag, TagConnection>
{
    private static final long serialVersionUID = 1L;
    
    public TagConnectionList()
    {
        super();
    }
    
    public TagConnectionList(List<TagConnection> src)
    {
        super(src);
    }

    public TagConnectionList(int size)
    {
        super(size);
    }

}
