/**
 * Node.java
 *
 * Created on 2. 4. 2017, 22:49:58 by burgetr
 */
package org.fit.layout.patterns.graph;

/**
 * A node of an extraction graph.
 * 
 * @author burgetr
 */
public class Node
{
    private long id;
    private String[] uris;
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
    
    public String[] getUris()
    {
        return uris;
    }
    
    public void setUris(String[] uris)
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Node other = (Node) obj;
        if (id != other.id) return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        return getId() + ":" + getTitle();
    }
    
}
