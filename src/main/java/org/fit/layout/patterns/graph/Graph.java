/**
 * Graph.java
 *
 * Created on 2. 4. 2017, 22:48:24 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author burgetr
 */
public class Graph
{
    private long id;
    private String title;
    private Map<Long, Node> nodes;
    private List<Edge> edges;
    
    
    public Graph()
    {
        nodes = new HashMap<>();
        edges = new ArrayList<>();
    }
    
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
    
    public Map<Long, Node> getNodes()
    {
        return nodes;
    }
    
    public List<Edge> getEdges()
    {
        return edges;
    }
    
    public void addNode(Node node)
    {
        nodes.put(node.getId(), node);
    }
    
    public void addEdge(Edge edge)
    {
        edges.add(edge);
    }
    
}
