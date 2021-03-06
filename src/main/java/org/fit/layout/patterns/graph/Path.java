/**
 * Path.java
 *
 * Created on 3. 4. 2017, 13:48:32 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.util.ArrayList;

/**
 * A path in a graph represented as a sequence of nodes.
 * 
 * @author burgetr
 */
public class Path extends ArrayList<Node>
{
    private static final long serialVersionUID = 1L;
    private boolean srcMany;
    private boolean dstMany;
    private boolean optional;

    public Path()
    {
        super();
        srcMany = false;
        dstMany = false;
        optional = false;
    }
    
    public Path(Node node)
    {
        super(1);
        add(node);
        srcMany = false;
        dstMany = false;
        optional = false;
    }
    
    public Path(Path src)
    {
        super(src);
        srcMany = src.srcMany;
        dstMany = src.dstMany;
        optional = src.optional;
    }
    
    public Path(Path src, Node next, boolean srcM, boolean dstM, boolean opt)
    {
        super(src.size() + 1);
        addAll(src);
        add(next);
        srcMany = srcM || src.srcMany;
        dstMany = dstM || src.dstMany;
        optional = opt || src.optional;
    }
    
    public boolean isSrcMany()
    {
        return srcMany;
    }

    public boolean isDstMany()
    {
        return dstMany;
    }

    public boolean isOptional()
    {
        return optional;
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
    
    @Override
    public String toString()
    {
        String ret = super.toString();
        ret += "(";
        ret += srcMany ? "M" : "1";
        ret += ":";
        ret += dstMany ? "M" : "1";
        if (optional)
            ret += "*";
        ret += ")";
        return ret;
    }
    
}
