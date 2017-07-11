/**
 * EdgeNodePair.java
 *
 * Created on 5. 4. 2017, 12:49:40 by burgetr
 */
package org.fit.layout.patterns.graph;

/**
 * An edge that leads to a particular node.
 * 
 * @author burgetr
 */
public class EdgeNodePair
{
    private Edge edge;
    private Node node;
    
    public EdgeNodePair(Edge edge, Node node)
    {
        this.edge = edge;
        this.node = node;
    }

    public Edge getEdge()
    {
        return edge;
    }

    public void setEdge(Edge edge)
    {
        this.edge = edge;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }
    
    /**
     * Checks whether the associated edge is reversed, i.e. it has source in the given node instead of
     * having destination in the given node.
     * @return {@code true} if the edge is reversed
     */
    public boolean reversedEdge()
    {
        return (edge.getSrcId() == node.getId()); 
    }
    
    public boolean isSrcMany()
    {
        return reversedEdge() ? edge.isDstMany() : edge.isSrcMany();
    }
    
    public boolean isDstMany()
    {
        return reversedEdge() ? edge.isSrcMany() : edge.isDstMany();
    }
    
    public boolean isSrcOptional()
    {
        return reversedEdge() ? edge.isDstOptional() : edge.isSrcOptional();
    }
    
    public boolean isDstOptional()
    {
        return reversedEdge() ? edge.isSrcOptional() : edge.isDstOptional();
    }
    
}
