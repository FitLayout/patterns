/**
 * Edge.java
 *
 * Created on 2. 4. 2017, 22:50:11 by burgetr
 */
package org.fit.layout.patterns.graph;


/**
 * An edge of an extraction graph.
 * 
 * @author burgetr
 */
public class Edge
{
    private long srcId;
    private long dstId;
    private String[] uris;
    private String title;
    private boolean dstMany;
    private boolean srcMany;
    private boolean dstOptional;
    private boolean srcOptional;
    
    public long getSrcId()
    {
        return srcId;
    }
    
    public void setSrcId(long srcId)
    {
        this.srcId = srcId;
    }
    
    public long getDstId()
    {
        return dstId;
    }
    
    public void setDstId(long dstId)
    {
        this.dstId = dstId;
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
    
    public boolean isDstMany()
    {
        return dstMany;
    }
    
    public void setDstMany(boolean dstMany)
    {
        this.dstMany = dstMany;
    }
    
    public boolean isSrcMany()
    {
        return srcMany;
    }
    
    public void setSrcMany(boolean srcMany)
    {
        this.srcMany = srcMany;
    }

    public boolean isDstOptional()
    {
        return dstOptional;
    }

    public void setDstOptional(boolean dstOptional)
    {
        this.dstOptional = dstOptional;
    }

    public boolean isSrcOptional()
    {
        return srcOptional;
    }

    public void setSrcOptional(boolean srcOptional)
    {
        this.srcOptional = srcOptional;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (dstId ^ (dstId >>> 32));
        result = prime * result + (int) (srcId ^ (srcId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Edge other = (Edge) obj;
        if (dstId != other.dstId) return false;
        if (srcId != other.srcId) return false;
        return true;
    }
    
}
