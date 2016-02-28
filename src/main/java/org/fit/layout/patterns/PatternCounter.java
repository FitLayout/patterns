/**
 * StyleCounter.java
 *
 * Created on 5. 3. 2015, 16:30:24 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Statistical analyzer of style occurences. Any implementation of the style may be provided,
 * e.g. the default {@code NodeStyle}. The style implementation must correctly implement the
 * {@code equals()} and {@code hashCode()} methods. 
 * 
 * @author burgetr
 */
public class PatternCounter<T>
{
    private Map<T, Float> patterns;
    
    
    /**
     * Creates an empty style counter.
     */
    public PatternCounter()
    {
        patterns = new HashMap<T, Float>();
    }
    
    /**
     * Adds a new occurence to the counter.
     * @param style The style to be added.
     */
    public void add(T style, float weight)
    {
        Float cur = patterns.get(style);
        if (cur == null)
            patterns.put(style, weight);
        else
            patterns.put(style, cur+weight);
    }
    
    /**
     * Obtains total registered number of occurences of the given style. 
     * @param style the style whose number of occurences should be returned
     * @return
     */
    public float getWeight(T style)
    {
        Float cnt = patterns.get(style);
        return cnt == null ? 0 : cnt;
    }
    
    /**
     * Obtains the most frequent style. If there are multiple styles with the same frequency then
     * only one of them is returned.
     * @return The most frequent style or {@code null} when the counter is empty.
     */
    public T getMostFrequent()
    {
        T ret = null;
        float w = 0;
        for (Map.Entry<T, Float> entry : patterns.entrySet())
        {
            if (entry.getValue() > w)
            {
                ret = entry.getKey();
                w = entry.getValue();
            }
        }
        return ret;
    }
    
    /**
     * Obtains the most frequent style or styles when multiple of them have the maximal frequency.
     * @return The list of styles with the maximal frequency.
     */
    public List<T> getMostFrequentAll()
    {
        List<T> ret = new Vector<T>();
        Float maxw = 0.0f;
        for (Map.Entry<T, Float> entry : patterns.entrySet())
        {
            if (entry.getValue() > maxw)
                maxw = entry.getValue();
        }
        for (Map.Entry<T, Float> entry : patterns.entrySet())
        {
            if (entry.getValue() == maxw)
                ret.add(entry.getKey());
        }
        return ret;
    }

    /**
     * Obtains all the registered styles and their frequencies.
     * @return A map that assigns a frequency to each unique style.
     */
    public Map<T, Float> getAll()
    {
    	return patterns;
    }
    
    /**
     * Obtains all the registered styles and their frequencies, sort by frequenct.
     * @return A map that assigns a frequency to each unique style.
     */
    public Map<T, Float> getAllSorted()
    {
        Map<T, Float> map = patterns;
        TreeMap<T, Float> smap = new TreeMap<T, Float>(new StyleCountComparator(map));
        smap.putAll(map);
        return smap;
    }
    
    //==============================================================================================
    
	@Override
	public String toString() 
	{
		Map<T, Float> map = getAllSorted();
		StringBuilder ret = new StringBuilder();
		for (Map.Entry<T, Float> entry : map.entrySet())
		{
			ret.append(entry.getValue()).append("x(");
			ret.append(entry.getKey().toString());
			ret.append(") ");
		}
		return ret.toString();
	}
	
    //==============================================================================================
	
    /**
     * A comparator used for sorting style maps according to the style count.
     */
    class StyleCountComparator implements Comparator<T>
    {
        Map<T, Float> base;

        public StyleCountComparator(Map<T, Float> base)
        {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(T a, T b)
        {
            if (base.get(a) >= base.get(b))
            {
                return -1;
            }
            else
            {
                return 1;
            } // returning 0 would merge keys
        }
    }

    
}
