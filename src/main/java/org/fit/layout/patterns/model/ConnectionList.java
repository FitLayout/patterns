/**
 * AreaConnectionList.java
 *
 * Created on 28. 2. 2016, 18:47:48 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A generic database of connections with different indices.
 * @author burgetr
 */
public class ConnectionList<P, T extends Connection<?>> extends ArrayList<T>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Index by the first node of the connection.
     */
    private Map<P, ConnectionList<P, T>> firstNodeIndex;
    
    /**
     * Pair index. It is build on request (see {@link ConnectionList#filterForPair(Pair)}).
     */
    private Map<Pair<P>, ConnectionList<P, T>> pairIndex;
    
    
    public ConnectionList()
    {
        super();
        init();
    }
    
    public ConnectionList(Collection<T> src)
    {
        super(src);
        init();
    }
    
    public ConnectionList(int size)
    {
        super(size);
        init();
    }

    private void init()
    {
        firstNodeIndex = new HashMap<>();
        pairIndex = new HashMap<>();
    }
    
    public ConnectionList<P, T> filterForFirstNode(P node)
    {
        ConnectionList<P, T> group = firstNodeIndex.get(node);
        if (group == null)
        {
            group = new ConnectionList<>();
            for (T cand : this)
            {
                if (cand.getA1().equals(node))
                    group.add(cand);
            }
            firstNodeIndex.put(node, group);
        }
        return group;
    }
    
    public ConnectionList<P, T> filterForPair(Pair<P> pair)
    {
        ConnectionList<P, T> group = pairIndex.get(pair);
        if (group == null)
        {
            group = new ConnectionList<>();
            for (T cand : this)
            {
                if (cand.getA1().equals(pair.getO1()) && cand.getA2().equals(pair.getO2()))
                    group.add(cand);
            }
            pairIndex.put(pair, group);
        }
        return group;
    }
    

}
