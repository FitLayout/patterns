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
 * 
 * @author burgetr
 */
public class ConnectionList<P, T extends Connection<?>> extends ArrayList<T>
{
    private static final long serialVersionUID = 1L;
    
    private Map<Pair<P>, ConnectionList<P, T>> groups;
    
    
    public ConnectionList()
    {
        super();
        groups = new HashMap<>();
    }
    
    public ConnectionList(Collection<T> src)
    {
        super(src);
        groups = new HashMap<>();
    }
    
    public ConnectionList<P, T> filterForPair(P v1, P v2)
    {
        Pair<P> pair = new Pair<>(v1, v2);
        ConnectionList<P, T> group = groups.get(pair);
        if (group == null)
        {
            group = new ConnectionList<>();
            for (T cand : this)
            {
                if (cand.getA1().equals(v1) && cand.getA2().equals(v2))
                    group.add(cand);
            }
            groups.put(pair, group);
        }
        return group;
    }
    
    //======================================================================================
    
    private static class Pair<P>
    {
        public P o1;
        public P o2;
        
        public Pair(P o1, P o2)
        {
            this.o1 = o1;
            this.o2 = o2;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((o1 == null) ? 0 : o1.hashCode());
            result = prime * result + ((o2 == null) ? 0 : o2.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            @SuppressWarnings("unchecked")
            Pair<P> other = (Pair<P>) obj;
            if (o1 == null)
            {
                if (other.o1 != null) return false;
            }
            else if (!o1.equals(other.o1)) return false;
            if (o2 == null)
            {
                if (other.o2 != null) return false;
            }
            else if (!o2.equals(other.o2)) return false;
            return true;
        }

        @Override
        public String toString()
        {
            return "<" + o1.toString() + ", " + o2.toString() + ">";
        }
        
    }

}
