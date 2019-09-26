/**
 * MatchResultScore.java
 *
 * Created on 29. 11. 2018, 14:34:37 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.eval.ConnectionStats;

/**
 * Represents different statistics about a match result
 * @author burgetr
 */
public class MatchResultScore implements Comparable<MatchResultScore>
{
    /** A list of comparators that are used for comparing the match result in the given order. */
    private static List<Comparator<MatchResultScore>> cclist;
    static {
        cclist = new ArrayList<>(5);
        cclist.add(new Comparator<MatchResultScore>() //number of matched areas above a threshold
        {
            @Override
            public int compare(MatchResultScore o1, MatchResultScore o2)
            {
                if (o1.getStats() != null && o1.getStats() == o2.getStats())
                {
                    final int threshold = o1.getStats().getMaxAreas() / 2;
                    int t1 = (o1.getMatchedAreas() > threshold) ? 1 : 0;
                    int t2 = (o2.getMatchedAreas() > threshold) ? 1 : 0;
                    return t1 - t2;
                }
                else
                    return 0; //cannot compare, use next comparators
            }
        });
        /*cclist.add(new Comparator<MatchResultScore>() //minimal metric standard deviation value (lower is better)
        {
            @Override
            public int compare(MatchResultScore o1, MatchResultScore o2)
            {
                if (o1.getMinMetric() < o2.getMinMetric())
                    return 1;
                else if (o1.getMinMetric() > o2.getMinMetric())
                    return -1;
                else
                    return 0;
            }
        });*/
        /*cclist.add(new Comparator<MatchResultScore>() //connection weight standard deviation (lower is better)
        {
            @Override
            public int compare(MatchResultScore o1, MatchResultScore o2)
            {
                if (o1.getConnectionWeightSigma() < o2.getConnectionWeightSigma())
                    return 1;
                else if (o1.getConnectionWeightSigma() > o2.getConnectionWeightSigma())
                    return -1;
                else
                    return 0;
            }
        });*/
        cclist.add(new Comparator<MatchResultScore>() //overall score
        {
            @Override
            public int compare(MatchResultScore o1, MatchResultScore o2)
            {
                if (o1.getOverallScore() > o2.getOverallScore())
                    return 1;
                else if (o1.getOverallScore() < o2.getOverallScore())
                    return -1;
                else
                    return 0;
            }
        });
        /*cclist.add(new Comparator<MatchResultScore>() //hint score
        {
            @Override
            public int compare(MatchResultScore o1, MatchResultScore o2)
            {
                if (o1.getHintScore() > o2.getHintScore())
                    return 1;
                else if (o1.getHintScore() < o2.getHintScore())
                    return -1;
                else
                    return 0;
            }
        });*/
        /*cclist.add(new Comparator<MatchResultScore>() //number of matched areas
        {
            @Override
            public int compare(MatchResultScore o1, MatchResultScore o2)
            {
                return o1.getMatchedAreas().size() - o2.getMatchedAreas().size();
            }
        });
        cclist.add(new Comparator<MatchResultScore>() //total number of matches found
        {
            @Override
            public int compare(MatchResultScore o1, MatchResultScore o2)
            {
                return o1.getMatches().size() - o2.getMatches().size();
            }
        });
        cclist.add(new Comparator<MatchResultScore>() //average connection weight (greater is better)
        {
            @Override
            public int compare(MatchResultScore o1, MatchResultScore o2)
            {
                if (o1.getAverageConnectionWeight() > o2.getAverageConnectionWeight())
                    return 1;
                else if (o1.getAverageConnectionWeight() < o2.getAverageConnectionWeight())
                    return -1;
                else
                    return 0;
            }
        });*/
    }

    private MatchStatistics stats;
    private int matches;
    private int matchedAreas;
    private float minConnectionWeight;
    private float maxConnectionWeight;
    private float averageConnectionWeight;
    private float connectionWeightSigma;
    private float minMetric;
    private float styleConsistency;
    private float hintScore;
    
    private Map<TagConnection, ConnectionStats> conStats;
    private Map<Tag, StyleCounter<AreaStyle>> styleStats;
    
    
    public MatchResultScore()
    {
    }

    public MatchStatistics getStats()
    {
        return stats;
    }

    public void setStats(MatchStatistics stats)
    {
        this.stats = stats;
        updateStats();
    }

    public int getMatches()
    {
        return matches;
    }

    public void setMatches(int matches)
    {
        this.matches = matches;
    }

    public int getMatchedAreas()
    {
        return matchedAreas;
    }

    public void setMatchedAreas(int matchedAreas)
    {
        this.matchedAreas = matchedAreas;
    }

    public float getMinConnectionWeight()
    {
        return minConnectionWeight;
    }

    public void setMinConnectionWeight(float minConnectionWeight)
    {
        this.minConnectionWeight = minConnectionWeight;
    }

    public float getMaxConnectionWeight()
    {
        return maxConnectionWeight;
    }

    public void setMaxConnectionWeight(float maxConnectionWeight)
    {
        this.maxConnectionWeight = maxConnectionWeight;
    }

    public float getAverageConnectionWeight()
    {
        return averageConnectionWeight;
    }

    public void setAverageConnectionWeight(float averageConnectionWeight)
    {
        this.averageConnectionWeight = averageConnectionWeight;
    }

    public float getConnectionWeightSigma()
    {
        return connectionWeightSigma;
    }

    public void setConnectionWeightSigma(float connectionWeightSigma)
    {
        this.connectionWeightSigma = connectionWeightSigma;
    }

    public float getMinMetric()
    {
        return minMetric;
    }

    public void setMinMetric(float minMetric)
    {
        this.minMetric = minMetric;
    }

    public float getStyleConsistency()
    {
        return styleConsistency;
    }

    public void setStyleConsistency(float styleConsistency)
    {
        this.styleConsistency = styleConsistency;
    }

    public float getHintScore()
    {
        return hintScore;
    }

    public void setHintScore(float hintScore)
    {
        this.hintScore = hintScore;
    }

    public Map<TagConnection, ConnectionStats> getConStats()
    {
        return conStats;
    }

    public void setConStats(Map<TagConnection, ConnectionStats> conStats)
    {
        this.conStats = conStats;
    }

    public Map<Tag, StyleCounter<AreaStyle>> getStyleStats()
    {
        return styleStats;
    }

    public void setStyleStats(Map<Tag, StyleCounter<AreaStyle>> styleStats)
    {
        this.styleStats = styleStats;
    }

    public float getCoveredAreas()
    {
        if (getStats() != null)
            return getMatchedAreas() / (float) getStats().getMaxAreas();
        else
            return 0.0f;
    }
    
    public float getCoveredMatches()
    {
        if (getStats() != null)
            return getMatches() / (float) getStats().getMaxMatches();
        else
            return 0.0f;
    }
    
    public float getNormalizedMM()
    {
        if (getStats() != null && getStats().getMaxMM() != 0.0f)
            return 1.0f - (getMinMetric() / getStats().getMaxMM());
        else
            return 0.0f;
    }
    
    public float getOverallScore()
    {
        //original weights
        return (1 * getNormalizedMM()
                + 0.5f * getStyleConsistency()
                + 1 * getCoveredAreas()
                + 0.5f * getAverageConnectionWeight()
                + 0.25f * getHintScore()) 
                / 3.25f;
        
        //discovered from CEUR dataset
        /*return (0.0f  * getCoveredMatches() +
                0.36f * getCoveredAreas() +
                0.44f * getAverageConnectionWeight() +
                0.28f * getConnectionWeightSigma() +
                0.74f * getStyleConsistency() +
                0.14f * getHintScore())
                / 1.96f;*/
        
        //w1
        /*return 1.076f  * getCoveredMatches() +
                0.1094f * getCoveredAreas() +
                0.3978f * getAverageConnectionWeight() +
                1.1297f * getStyleConsistency() +
                0.0068f * (1.0f - getMinMetric()) +
                0.2013f * getHintScore() +
               -1.7828f;*/
        
    }
    
    public void updateStats()
    {
        stats.setMaxMatches(Math.max(stats.getMaxMatches(), getMatches()));
        stats.setMaxAreas(Math.max(stats.getMaxAreas(), getMatchedAreas()));
        stats.setMaxMM(Math.max(stats.getMaxMM(), getMinMetric()));
    }
    
    @Override
    public int compareTo(MatchResultScore o)
    {
        for (Comparator<MatchResultScore> cc : cclist)
        {
            int comp = cc.compare(this, o);
            if (comp != 0)
                return comp;
        }
        return 0;
    }
    
    @Override
    public String toString()
    {
        return getMatches() + " matches (" + getCoveredMatches() + ")"
            + ", " + getMatchedAreas() + " areas covered (" + getCoveredAreas() + ")"
            + ", S=" + getOverallScore()
            + ", w=" + getAverageConnectionWeight() 
            + ", s=" + getConnectionWeightSigma()
            + ", sc=" + getStyleConsistency()
            + ", mm=" + getNormalizedMM()
            + ", hs=" + getHintScore()
            //+ ", min=" + getMinConnectionWeight()
            //+ ", max=" + getMaxConnectionWeight()
            ;
    }
    
    public void dumpMinMetric()
    {
        for (TagConnection tcon : getConStats().keySet())
        {
            System.out.println(tcon  + " best:" + getConStats().get(tcon).getBestMetric() + "=" + getConStats().get(tcon).getBestMetricValue());
            for (Metric m : getConStats().get(tcon).getMetrics())
            {
                System.out.println(" " + m + ": " + getConStats().get(tcon).getMetricSigma(m));
            }
        }
    }
    
    public void dumpStyleStats()
    {
        Map<Tag, StyleCounter<AreaStyle>> styles = getStyleStats();
        for (Tag t : styles.keySet())
        {
            StyleCounter<AreaStyle> style = styles.get(t);
            System.out.println(t + " style: " + style.getPercentage(style.getMostFrequent()));
        }
    }

}
