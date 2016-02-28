/**
 * Connection.java
 *
 * Created on 28. 2. 2016, 19:45:42 by burgetr
 */
package org.fit.layout.patterns;

/**
 * 
 * @author burgetr
 */
public class Connection<T>
{
    private T a1;
    private T a2;
    private Relation relation;
    private float weight;
    
    public Connection(T a1, T a2, Relation relation, float weight)
    {
        this.a1 = a1;
        this.a2 = a2;
        this.relation = relation;
        this.weight = weight;
    }

    public T getA1()
    {
        return a1;
    }

    public T getA2()
    {
        return a2;
    }

    public Relation getRelation()
    {
        return relation;
    }

    public float getWeight()
    {
        return weight;
    }

    @Override
    public String toString()
    {
        return a1.toString() + " " + relation.getName() + "(" + weight + ") " + a2.toString(); 
    }
    
    

}
