/**
 * Graph.java
 *
 * Created on 2. 4. 2017, 22:48:24 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An extraction graph.
 * 
 * @author burgetr
 */
public class Graph
{
    private long id;
    private String title;
    private Map<Long, Node> nodes;
    private List<Edge> edges;
    private Map<Long, Set<Edge>> edgeIndex;
    private Map<String, Node> nodeUriIndex;
    
    public Graph()
    {
        nodes = new HashMap<>();
        edges = new ArrayList<>();
        edgeIndex = new HashMap<>();
        nodeUriIndex = new HashMap<>();
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
        for (String uri : node.getUris())
            nodeUriIndex.put(uri, node);
    }
    
    public void addEdge(Edge edge)
    {
        edges.add(edge);
        Set<Edge> indexItem = edgeIndex.get(edge.getSrcId());
        if (indexItem == null)
        {
            indexItem = new HashSet<Edge>();
            edgeIndex.put(edge.getSrcId(), indexItem);
        }
        indexItem.add(edge);
        indexItem = edgeIndex.get(edge.getDstId());
        if (indexItem == null)
        {
            indexItem = new HashSet<Edge>();
            edgeIndex.put(edge.getDstId(), indexItem);
        }
        indexItem.add(edge);
    }
    
    //===============================================================================

    public Node findNodeByUri(String uri)
    {
        return nodeUriIndex.get(uri);
    }
    
    public Set<EdgeNodePair> getNeighborsOf(Node node)
    {
        Set<EdgeNodePair> ret = new HashSet<>();
        Set<Edge> edges = edgeIndex.get(node.getId());
        if (edges != null)
        {
            for (Edge e : edges)
            {
                Node dest;
                if (node.getId() == e.getSrcId())
                    dest = nodes.get(e.getDstId());
                else
                    dest = nodes.get(e.getSrcId());
                if (dest != null)
                    ret.add(new EdgeNodePair(e, dest));
            }
        }
        return ret;
    }
    
    public List<Path> getPathsFrom(Node start)
    {
        Path current = new Path(start);
        List<Path> ret = new ArrayList<>();
        appendNext(current, ret);
        return ret;
    }
    
    public List<Path> getDatatypePathsFrom(Node start)
    {
        List<Path> ret = getPathsFrom(start);
        for (Iterator<Path> it = ret.iterator(); it.hasNext();)
        {
            Path path = it.next();
            if (path.getLast().isObject())
                it.remove();
        }
        return ret;
    }
    
    public void appendNext(Path current, List<Path> dest)
    {
        Node last = current.getLast();
        Set<EdgeNodePair> nextNodes = getNeighborsOf(last);
        for (EdgeNodePair next : nextNodes)
        {
            if (!current.contains(next.getNode()))
            {
                Path newpath = new Path(current, next.getNode(), next.isSrcMany(), next.isDstMany());
                dest.add(newpath);
                appendNext(newpath, dest);
            }
        }
    }
    
    
}
