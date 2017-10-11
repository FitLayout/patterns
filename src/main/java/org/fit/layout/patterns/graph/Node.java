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
    private boolean primary;
    private String tagger;
    
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
    
    public boolean hasUri(String uri)
    {
        for (String u : this.uris)
        {
            if (u.equals(uri))
                return true;
        }
        return false;
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

    public boolean isPrimary()
    {
        return primary;
    }

    public void setPrimary(boolean primary)
    {
        this.primary = primary;
    }

    public String getTagger()
    {
        return tagger;
    }

    public void setTagger(String tagger)
    {
        this.tagger = tagger;
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
