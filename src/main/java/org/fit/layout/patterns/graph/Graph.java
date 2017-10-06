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
import java.util.stream.Collectors;

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
    
    public boolean containsEdgeBetween(Node n1, Node n2)
    {
        Set<Edge> cands = edgeIndex.get(n1.getId());
        for (Edge e : cands)
        {
            if (e.getDstId() == n2.getId())
                return true;
        }
        return false;
    }
    
    public boolean contains1xEdgeBetween(Node n1, Node n2)
    {
        Set<Edge> cands = edgeIndex.get(n1.getId());
        for (Edge e : cands)
        {
            if (!e.isSrcMany() && e.getDstId() == n2.getId())
                return true;
        }
        return false;
    }
    
    //===============================================================================
    
    public List<Path> getPathsFrom(Node start)
    {
        //recursively find all paths from the starting node
        Path current = new Path(start);
        List<Path> ret = new ArrayList<>();
        recursiveAppendNext(current, ret);
        //disambiguate the paths (if there are more paths to the same node)
        //TODO
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
    
    private void recursiveAppendNext(Path current, List<Path> dest)
    {
        Node last = current.getLast();
        Set<EdgeNodePair> nextNodes = getNeighborsOf(last);
        for (EdgeNodePair next : nextNodes)
        {
            if (!current.contains(next.getNode()))
            {
                Path newpath = new Path(current, next.getNode(), next.isSrcMany(), next.isDstMany(), next.isDstOptional());
                dest.add(newpath);
                recursiveAppendNext(newpath, dest);
            }
        }
    }
    
    //===============================================================================

    public void getGroups()
    {
        //create a group for every datatype node
        Set<Group> groups = new HashSet<>();
        List<Node> nodeList = new ArrayList<>(nodes.values());
        for (Iterator<Node> it = nodeList.iterator(); it.hasNext();)
        {
            Node n = it.next();
            if (!n.isObject())
            {
                groups.add(new Group(n));
                it.remove();
            }
        }
        //try to group the groups using object nodes
        boolean change = true;
        while (change)
        {
            change = false;
            for (Iterator<Node> it = nodeList.iterator(); it.hasNext() && !change;)
            {
                Node n = it.next();
                if (isOnMSide(n))
                {
                    System.out.println(n + " is on M side ");
                    List<Group> subGroups = groups.stream()
                            .filter(g -> contains1xEdgeBetween(n, g.getRoot()))
                            .collect(Collectors.toList());
                    System.out.println("sub: " + subGroups);
                    if (subGroups.size() > 1 && subGroups.size() < groups.size())
                    {
                        Group newgroup = new Group(n);
                        newgroup.setSubGroups(subGroups);
                        groups.removeAll(subGroups);
                        groups.add(newgroup);
                        change = true;
                    }
                    System.out.println("removing " + n);
                    it.remove();
                }
            }
        }
        System.out.println(groups);
        System.out.println("remain: " + nodeList);
    }
    
    /**
     * Check whether a node is on the MANY side of any edge
     * @param n
     * @return
     */
    private boolean isOnMSide(Node n)
    {
        Set<EdgeNodePair> pairs = getNeighborsOf(n);
        for (EdgeNodePair pair : pairs)
        {
            if (pair.isSrcMany())
                return true;
        }
        return false;
    }
    
}
