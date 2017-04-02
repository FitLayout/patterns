/**
 * Graph.java
 *
 * Created on 2. 4. 2017, 22:48:24 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.List;

/**
 * 
 * @author burgetr
 */
public class Graph
{
    private long id;
    private String title;
    private List<Node> nodes;
    private List<Edge> edges;
    
    public long getId()
    {
        return id;
    }
    
    public void setId(long id)
    {
        this.id = id;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public List<Node> getNodes()
    {
        return nodes;
    }
    
    public void setNodes(List<Node> nodes)
    {
        this.nodes = nodes;
    }
    
    public List<Edge> getEdges()
    {
        return edges;
    }
    
    public void setEdges(List<Edge> edges)
    {
        this.edges = edges;
    }
}
