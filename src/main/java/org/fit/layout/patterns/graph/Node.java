/**
 * Node.java
 *
 * Created on 2. 4. 2017, 22:49:58 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.List;

/**
 * 
 * @author burgetr
 */
public class Node
{
    private long id;
    private List<String> uris;
    private String title;
    private boolean object;
    
    public long getId()
    {
        return id;
    }
    
    public void setId(long id)
    {
        this.id = id;
    }
    
    public List<String> getUris()
    {
        return uris;
    }
    
    public void setUris(List<String> uris)
    {
        this.uris = uris;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public boolean isObject()
    {
        return object;
    }
    
    public void setObject(boolean object)
    {
        this.object = object;
    }
}
