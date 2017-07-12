/**
 * OneToManyMatcher.java
 *
 * Created on 28. 2. 2016, 17:07:22 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.ConsistentAreaAnalyzer.ChainList;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.AreaStyle;
import org.fit.layout.patterns.model.Match;
import org.fit.layout.patterns.model.TagConnection;
import org.fit.layout.patterns.model.TagConnectionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author burgetr
 */
public class OneToManyMatcher extends BaseMatcher
{
    private static Logger log = LoggerFactory.getLogger(OneToManyMatcher.class);

    private Tag[] srcTag;
    private boolean fixedOrder;
    private float minSupport;
    
    private List<Area> areas;
    
    private List<StyleCounter<AreaStyle>> styleStats; //style statistics
    private RelationAnalyzer pa;
    private ChainList chains;
    
    
    public OneToManyMatcher(Tag oneTag, Tag manyTag, float minSupport, boolean fixedOrder)
    {
        super();
        srcTag = new Tag[2];
        srcTag[0] = oneTag;
        srcTag[1] = manyTag;
        this.minSupport = minSupport;
        this.fixedOrder = fixedOrder;
    }
    
    @Override
    public List<Match> match(List<Area> areas)
    {
        this.areas = areas;
        gatherStatistics();
        
        log.debug("Styles:");
        for (int i = 0; i < srcTag.length; i++)
        {
            log.debug("Styles {}: {}", srcTag[i], styleStats.get(i));
        }
        
        List<Configuration> best = scanDisambiguations();
        for (Configuration conf : best)
            log.debug("Best:{}", conf);
        if (!best.isEmpty())
        {
            log.debug("Using:{}", best.get(0));
            
            StyleAnalyzerClassify cls = createClassifyingAnalyzer(best.get(0));
            cls.dumpToFile("/tmp/matches-" + srcTag[0].getValue() + "-" + srcTag[1].getValue() + ".arff");
            
            return getMatches(best.get(0));
        }
        else
            return null;
     }
    
    //===========================================================================================

    private List<Match> getMatches(Configuration conf)
    {
        StyleAnalyzer sa = new StyleAnalyzerFixed(conf.getStyleMap());
        Disambiguator dis = new Disambiguator(sa, conf.useChains ? chains : null, 0.09f); //lower the min support for real matching
        List<Match> ret = new ArrayList<>();
        for (Area a1 : areas)
        {
            if (srcTag[0].equals(dis.getAreaTag(a1)))
            {
                List<Area> inrel = getAreasInBestRelation(a1, conf.relation, srcTag[0], srcTag[1], dis);
                for (Area a2 : inrel)
                {
                    Match match = new Match();
                    match.putSingle(srcTag[0], a1);
                    match.putSingle(srcTag[1], a2);
                    ret.add(match);
                }
            }
        }
        return ret;
    }

    private StyleAnalyzerClassify createClassifyingAnalyzer(Configuration conf)
    {
        //bootstrap with a fixed style analyzer
        StyleAnalyzer sa = new StyleAnalyzerFixed(conf.getStyleMap());
        Disambiguator dis = new Disambiguator(sa, conf.useChains ? chains : null, minSupport);
        //select the areas that seem to be tagged with the appropriate tags
        Set<Area> selected0 = new HashSet<Area>();
        Set<Area> selected1 = new HashSet<Area>();
        for (Area a1 : areas)
        {
            if (srcTag[0].equals(dis.getAreaTag(a1)))
            {
                List<Area> inrel = getAreasInBestRelation(a1, conf.relation, srcTag[0], srcTag[1], dis);
                for (Area a2 : inrel)
                {
                    selected0.add(a1);
                    selected1.add(a2);
                }
            }
        }
        //train the classifier
        List<Tag> tagList = new ArrayList<Tag>(conf.getStyleMap().keySet());
        StyleAnalyzerClassify ret = new StyleAnalyzerClassify(tagList);
        for (Area a : areas)
        {
            if (selected0.contains(a))
                ret.addTrainingExample(a, srcTag[0]);
            else if (selected1.contains(a))
                ret.addTrainingExample(a, srcTag[1]);
            else
                ret.addTrainingExample(a, null);
        }
        return ret;
    }
    
    private List<Configuration> scanDisambiguations()
    {
        List<Configuration> all = new ArrayList<>();
        int bestCoverage = 0;
        //lists of used styles for the individual tags
        AreaStyle styles[][] = new AreaStyle[srcTag.length][];
        for (int i = 0; i < srcTag.length; i++)
        {
            List<AreaStyle> variants = new ArrayList<AreaStyle>(styleStats.get(i).getFrequentSyles(0.33f));
            if (getUseStyleWildcards() > 0)
                variants.addAll(createStyleCombinations(variants, getUseStyleWildcards()));
            if (variants.isEmpty())
            {
                log.error("No styles found for {}", srcTag[i]);
                return all;
            }
            log.debug("Trying for {}: {}", srcTag[i], variants);
            styles[i] = variants.toArray(new AreaStyle[0]);
        }
        //generate style combinations
        int indices[] = new int[srcTag.length];
        Arrays.fill(indices, 0);
        while (indices[indices.length - 1] < styles[indices.length - 1].length)
        {
            //create the style map for this iteration
            Map<Tag, AreaStyle> curStyles = new HashMap<Tag, AreaStyle>(srcTag.length);
            for (int i = 0; i < srcTag.length; i++)
            {
                //System.out.println("Use " + srcTag[i] + " style " + styles[i][indices[i]]); 
                curStyles.put(srcTag[i], styles[i][indices[i]]);
            }
            //log.debug("Style map: {}", curStyles);
            //if (curStyles.toString().equals("{minute=[fs:12.0 w:0.0 s:0.0 c:#000000], hour=[fs:15.0 w:0.0 s:0.0 c:#000000]}"))
            //{
            
            for (boolean useChains : new boolean[]{false, true})
            {
                //build statistics for the selected disambiguations
                StyleAnalyzer sa = new StyleAnalyzerFixed(curStyles);
                Disambiguator dis = new Disambiguator(sa, useChains ? chains : null, minSupport);
                TagConnectionList cons = pa.getTagConnections(dis);
                PatternCounter<TagConnection> pc = new PatternCounter<>();
                for (TagConnection con : cons)
                {
                    //System.out.println(con);
                    pc.add(con, con.getWeight());
                }
                //log.debug("Relations: {}", pc);
                
                //scan coverings of our tags
                for (TagConnection rel : pc.getAll().keySet())
                {
                    if (rel.getA1().equals(srcTag[1]) && rel.getA2().equals(srcTag[0]) /*&& rel.getRelation().toString().equals("rel:after")*/)
                    {
                        int cover = checkCovering(rel.getA1(), rel.getRelation(), rel.getA2(), dis);
                        Configuration conf = new Configuration(curStyles, useChains, rel.getRelation(), cover);
                        all.add(conf);
                        //log.debug("Try:{}", conf);
                        if (cover > bestCoverage)
                            bestCoverage = cover;
                    }
                }
            }
            //}
            //increment the indices
            indices[0]++;
            for (int i = 0; i < indices.length - 1; i++)
            {
                if (indices[i] >= styles[i].length)
                {
                    indices[i] = 0;
                    indices[i+1]++;
                }
                else
                    break;
            }
        }
        
        //select the best configurations
        List<Configuration> best = new ArrayList<>();
        for (Configuration conf : all)
        {
            if (conf.getCoverage() == bestCoverage)
                best.add(conf);
        }
        
        return best;
    }
    
    private int checkCovering(Tag tag1, Relation relation, Tag tag2, Disambiguator dis)
    {
        Set<Area> areas1 = new HashSet<Area>();
        Set<Area> areas2 = new HashSet<Area>();
        
        //start with all areas
        for (Area a : areas)
        {
            if (a.hasTag(tag1, minSupport))
                areas1.add(a);
            if (a.hasTag(tag2, minSupport))
                areas2.add(a);
        }
        
        return checkCovering(tag1, areas1, relation, tag2, areas2, dis);
    }
    
    private int checkCovering(Tag tag1, Set<Area> areas1, Relation relation, Tag tag2, Set<Area> areas2, Disambiguator dis)
    {
        Set<Area> matchedAreas = new HashSet<Area>();
        //remove matching pairs
        for (Area a : areas2)
        {
            if (tag2.equals(dis.getAreaTag(a)))
            {
                //if (a.getId() == 392)
                //    System.out.println("jo!");
                List<Area> inrel = getAreasInBestRelation(a, relation, tag2, tag1, dis);
                boolean matched = false;
                for (Area b : inrel)
                {
                    if (areas1.remove(b))
                    {
                        //log.debug("Cover: " + a + " " + relation + " " + b);
                        //b.addTag(new VisualTag("minute"), 1.0f);
                        matchedAreas.add(b);
                        matched = true;
                    }
                }
                if (matched)
                {
                    matchedAreas.add(a);
                    //a.addTag(new VisualTag("hour"), 1.0f);
                }
            }
        }
        
        return matchedAreas.size();
    }
    
    /**
     * Obtains all the area that are in the given relation with the given area and there exists
     * no better source area for this with the same destination area and a higher weight.
     * E.g. all areas below {@code a}.
     * Only the areas with specified tags are taken into account, the tags are inferred using
     * a disambiguator.
     * @param a the area to compare
     * @param r the relation to use.
     * @param srcTag the tag required for the source areas (incl. {@code a})
     * @param destTag the tag required for the destination areas
     * @param dis the disambiguator used for assigning the tags to areas
     * @return the list of corresponding areas
     */
    private List<Area> getAreasInBestRelation(Area a, Relation r, Tag srcTag, Tag destTag, Disambiguator dis)
    {
        List<AreaConnection> dest = pa.getConnections(null, r, a, -1.0f);
        List<Area> ret = new ArrayList<Area>(dest.size());
        for (AreaConnection cand : dest)
        {
            if (destTag.equals(dis.getAreaTag(cand.getA1())))
            {
                //find the source nodes that are closer
                List<AreaConnection> better = pa.getConnections(cand.getA1(), r, null, cand.getWeight());
                boolean foundBetter = false;
                for (AreaConnection betterCand : better)
                {
                    if (srcTag.equals(dis.getAreaTag(betterCand.getA2())))
                    {
                        foundBetter = true;
                        break;
                    }
                }
                if (!foundBetter)
                    ret.add(cand.getA1()); //a1 has no "better" source area, use it
            }
        }
        return ret;
    }
    
    private Set<AreaStyle> createStyleCombinations(List<AreaStyle> styles, int maxWildcards)
    {
        Set<AreaStyle> ret = new HashSet<>();
        for (int i = 0; i < styles.size(); i++)
        {
            AreaStyle s1 = styles.get(i);
            for (int j = 0; j < styles.size(); j++)
            {
                if (i != j)
                {
                    AreaStyle s2 = styles.get(j);
                    if (s1.getEditingDistance(s2) <= maxWildcards)
                    {
                        AreaStyle gen = new AreaStyle(s1);
                        gen.generalizeToFit(s2);
                        ret.add(gen);
                    }
                }
            }
        }
        return ret;
    }
    
    //===========================================================================================
    
    private void gatherStatistics()
    {
        //count styles
        styleStats = new ArrayList<>(srcTag.length);
        for (int i = 0; i < srcTag.length; i++)
            styleStats.add(new StyleCounter<AreaStyle>());
        for (Area a : areas)
        {
            for (int i = 0; i < srcTag.length; i++)
            {
                if (a.hasTag(srcTag[i]))
                    styleStats.get(i).add(new AreaStyle(a));
            }
        }
        //create pattern analyzer
        if (fixedOrder)
            pa = new RelationAnalyzer(areas);
        else
            pa = new RelationAnalyzerSymmetric(areas);
        //discover tag chains used for disambiguation
        ConsistentAreaAnalyzer ca = new ConsistentAreaAnalyzer(pa, srcTag, minSupport);
        chains = ca.findConsistentChains(new RelationUnder());
        chains.addAll(ca.findConsistentChains(new RelationSide()));
    }

    /*private void dumpRelations(List<AreaConnection> connections, String outfile)
    {
        try
        {
            PrintWriter w = new PrintWriter(outfile);
            w.println("@RELATION areas");
            w.println("@ATTRIBUTE x NUMERIC");
            w.println("@ATTRIBUTE y NUMERIC");
            w.println("@data");
            for (AreaConnection con : connections)
            {
                if (con.getA1().hasTag(srcTag[1]) && con.getA2().hasTag(srcTag[0]))
                {
                    Rectangular bounds = new Rectangular(con.getA1().getBounds());
                    bounds.expandToEnclose(con.getA2().getBounds());
                    w.println("% " + con.toString());
                    w.println(bounds.getX1() + "," + bounds.getY1());
                    w.println(bounds.getX2() + "," + bounds.getY2());
                }
            }
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
    }*/
    
    //===========================================================================================
    
    private class Configuration
    {
        private Map<Tag, AreaStyle> styleMap;
        private boolean useChains;
        private Relation relation;
        private int coverage;
        
        public Configuration(Map<Tag, AreaStyle> styleMap, boolean useChains, Relation relation, int coverage)
        {
            this.styleMap = styleMap;
            this.useChains = useChains;
            this.relation = relation;
            this.coverage = coverage;
        }

        public Map<Tag, AreaStyle> getStyleMap()
        {
            return styleMap;
        }

        public boolean getUseChains()
        {
            return useChains;
        }

        public Relation getRelation()
        {
            return relation;
        }

        public int getCoverage()
        {
            return coverage;
        }

        @Override
        public String toString()
        {
            return getRelation() + " " + getStyleMap() + " useChains=" + getUseChains() + " (" + getCoverage() + " matches)";
        }
        
    }
    
}
