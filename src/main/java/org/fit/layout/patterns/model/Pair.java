package org.fit.layout.patterns.model;


/**
 * A generic pair of objects.
 * 
 * @author burgetr
 * @param <P>
 */
public class Pair<P>
{
    public P o1;
    public P o2;
    
    public Pair(P o1, P o2)
    {
        this.o1 = o1;
        this.o2 = o2;
    }

    public P getO1()
    {
        return o1;
    }

    public void setO1(P o1)
    {
        this.o1 = o1;
    }

    public P getO2()
    {
        return o2;
    }

    public void setO2(P o2)
    {
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