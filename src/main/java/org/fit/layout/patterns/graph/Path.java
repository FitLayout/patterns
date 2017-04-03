/**
 * Path.java
 *
 * Created on 3. 4. 2017, 13:48:32 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.ArrayList;

/**
 * 
 * @author burgetr
 */
public class Path extends ArrayList<Node>
{
    private static final long serialVersionUID = 1L;

    public Path()
    {
        super();
    }
    
    public Path(Node node)
    {
        super(1);
        add(node);
    }
    
    public Path(Path src)
    {
        super(src);
    }
    
    public Path(Path src, Node next)
    {
        super(src.size() + 1);
        addAll(src);
        add(next);
    }
    
    public Node getFirst()
    {
        if (isEmpty())
            return null;
        else
            return get(0);
    }
    
    public Node getLast()
    {
        if (isEmpty())
            return null;
        else
            return get(size() - 1);
    }
    
}
