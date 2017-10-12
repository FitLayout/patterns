/**
 * Graph.java
 *
 * Created on 2. 4. 2017, 22:48:24 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An extraction graph.
 * 
 * @author burgetr
 */
public class Graph
{
    private long id;
    private String title;
    private Node primaryNode;
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
        primaryNode = null;
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
    
    public Node getPrimaryNode()
    {
        return primaryNode;
    }

    public void setPrimaryNode(Node primaryNode)
    {
        this.primaryNode = primaryNode;
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
        if (node.isPrimary() && getPrimaryNode() == null)
            setPrimaryNode(node);
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
    
    public List<Edge> getEdgesBetween(Node n1, Node n2)
    {
        List<Edge> ret = new ArrayList<>();
        Set<Edge> cands = edgeIndex.get(n1.getId());
        for (Edge e : cands)
        {
            if (e.getDstId() == n2.getId())
                ret.add(e);
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
    
    public Node[] getNodesForEdge(Edge e)
    {
        Node[] ret = new Node[2];
        ret[0] = nodes.get(e.getSrcId());
        ret[1] = nodes.get(e.getDstId());
        return ret;
    }
    
    /**
     * Joins the object node pairs that are only connected with a 1:1 relationship to a single nodes.
     * @return a new graph with the nodes collapsed
     */
    public Graph collapse()
    {
        Set<Node> newNodes = new HashSet<Node>(nodes.values());
        Set<Edge> newEdges = new HashSet<Edge>(edges);
        boolean change = true;
        while (change)
        {
            change = false;
            for (Iterator<Edge> it = newEdges.iterator(); it.hasNext();)
            {
                Edge e = it.next();
                if (!e.isSrcMany() && !e.isDstMany())
                {
                    Node[] n = getNodesForEdge(e);
                    if (n[0].isObject() && n[1].isObject())
                    {
                        List<Edge> ebetween =  getEdgesBetween(n[0], n[1]);
                        if (ebetween.size() == 1)
                        {
                            //System.out.println("Collapsing " + n[0] + " = " + n[1]);
                            //System.out.println("Removing " + e);
                            it.remove();
                            newNodes.remove(n[0]);
                            newNodes.remove(n[1]);
                            Node nn = createJoinedNode(n[0], n[1]);
                            newNodes.add(nn);
                            //reconnect edges
                            for (Edge ne : newEdges)
                            {
                                if (ne.getSrcId() == n[1].getId())
                                    ne.setSrcId(n[0].getId());
                                else if (ne.getDstId() == n[1].getId())
                                    ne.setDstId(n[0].getId());
                            }
                            change = true;
                            break;
                        }
                    }
                }
            }
        }
        //create the new graph
        Graph ret = new Graph();
        for (Node n : newNodes)
            ret.addNode(n);
        for (Edge e : newEdges)
            ret.addEdge(e);
        return ret;
    }
    
    private Node createJoinedNode(Node n1, Node n2)
    {
        Node nn = new Node();
        nn.setId(n1.getId());
        nn.setObject(true);
        nn.setUris(Stream.concat(Arrays.stream(n1.getUris()), Arrays.stream(n2.getUris())).toArray(size -> new String[size]));
        nn.setTitle(n1.getTitle()+"="+n2.getTitle());
        nn.setPrimary(n1.isPrimary() || n2.isPrimary());
        return nn;
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
                    
                    Group newgroup = new Group(n);
                    newgroup.setMany(true);
                    newgroup.setSubGroups(subGroups);
                    groups.removeAll(subGroups);
                    groups.add(newgroup);
                    change = true;
                    
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
