/**
 * Group.java
 *
 * Created on 4. 10. 2017, 15:03:41 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 
 * @author burgetr
 */
public class Group
{
    private Node root;
    private List<Group> subGroups;
    
    public Group(Node root)
    {
        this.root = root;
        this.subGroups = new ArrayList<>();
    }

    public Node getRoot()
    {
        return root;
    }

    public void setRoot(Node root)
    {
        this.root = root;
    }

    public List<Group> getSubGroups()
    {
        return subGroups;
    }

    public void setSubGroups(List<Group> subGroups)
    {
        this.subGroups = subGroups;
    }

    public void addSubGroup(Group group)
    {
        subGroups.add(group);
    }

    @Override
    public String toString()
    {
        String sub = subGroups.stream().map(g -> g.toString()).collect(Collectors.joining(","));
        return root.toString() + "[" + sub + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((root == null) ? 0 : root.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Group other = (Group) obj;
        if (root == null)
        {
            if (other.root != null) return false;
        }
        else if (!root.equals(other.root)) return false;
        return true;
    }
    
}
