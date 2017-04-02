/**
 * Edge.java
 *
 * Created on 2. 4. 2017, 22:50:11 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.List;

/**
 * 
 * @author burgetr
 */
public class Edge
{
    private long srcId;
    private long dstId;
    private List<String> uris;
    private String title;
    private boolean dstMany;
    private boolean srcMany;
    
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
    
}
