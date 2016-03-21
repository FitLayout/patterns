/**
 * StyleAnalyzerClassify.java
 *
 * Created on 20. 3. 2016, 15:52:59 by burgetr
 */
package org.fit.layout.patterns;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 
 * @author burgetr
 */
public class StyleAnalyzerClassify implements StyleAnalyzer
{
    /** The tags to be assigned */
    private List<Tag> tags;
    
    /** Classification attributes */
    private ArrayList<Attribute> attributes;
    
    /** Classification training set */
    private Instances trainset;
    
    /** The used classifier */
    private Classifier classifier;
    
    
    public StyleAnalyzerClassify(List<Tag> tags)
    {
        this.tags = tags;
        initTrainingData();
    }
    
    @Override
    public Set<Tag> inferTags(Area a)
    {
        Instance inst = createStyleInstance(new AreaStyle(a), null);
        inst.setDataset(trainset);
        Tag best = null;
        double bestScore = 0;
        try
        {
            double[] res = getClassifier().distributionForInstance(inst);
            for (int i = 0; i < res.length; i++)
            {
                if (res[i] > bestScore)
                {
                    best = tags.get(i);
                    bestScore = res[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<Tag> ret = new HashSet<Tag>(1);
        if (best != null)
            ret.add(best);
        return ret;
    }
    
    public Map<Tag, Double> classifyNodeStyle(AreaStyle style)
    {
        Instance inst = createStyleInstance(style, null);
        inst.setDataset(trainset);
        Map<Tag, Double> ret = new HashMap<Tag, Double>(tags.size());
        try
        {
            double[] res = getClassifier().distributionForInstance(inst);
            int i = 0;
            for (Tag tag : tags)
                ret.put(tag, res[i++]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void addTrainingExample(Area a, Tag t)
    {
        AreaStyle style = new AreaStyle(a);
        Instance inst = createStyleInstance(style, t);
        trainset.add(inst);
    }

    //============================================================================
    
    private Instance createStyleInstance(AreaStyle style, Tag tag)
    {
        Instance inst = new DenseInstance(13);
        String cls = (tag == null) ? "none" : tag.getValue();
        inst.setValue(attributes.get(0), cls);
        inst.setValue(attributes.get(1), style.getFontSize());
        inst.setValue(attributes.get(2), style.getWeight());
        inst.setValue(attributes.get(3), style.getStyle());
        inst.setValue(attributes.get(4), style.getColor().getRed() / 255.0);
        inst.setValue(attributes.get(5), style.getColor().getGreen() / 255.0);
        inst.setValue(attributes.get(6), style.getColor().getBlue() / 255.0);
        inst.setValue(attributes.get(7), style.getBgColor().getRed() / 255.0);
        inst.setValue(attributes.get(8), style.getBgColor().getGreen() / 255.0);
        inst.setValue(attributes.get(9), style.getBgColor().getBlue() / 255.0);
        inst.setValue(attributes.get(10), style.isBackgroundSeparated() ? 1.0 : 0.0);
        inst.setValue(attributes.get(11), style.getWidth());
        inst.setValue(attributes.get(12), style.getHeight());
        return inst;
    }

    private void initTrainingData()
    {
        List<String> tagnames = new ArrayList<String>(tags.size());
        for (Tag tag : tags)
            tagnames.add(tag.getValue());
        tagnames.add("none");
        
        attributes = new ArrayList<Attribute>(13);
        attributes.add(new Attribute("class", tagnames));
        attributes.add(new Attribute("fsize"));
        attributes.add(new Attribute("weight"));
        attributes.add(new Attribute("style"));
        attributes.add(new Attribute("fgr"));
        attributes.add(new Attribute("fgg"));
        attributes.add(new Attribute("fgb"));
        attributes.add(new Attribute("bgr"));
        attributes.add(new Attribute("bgg"));
        attributes.add(new Attribute("bgb"));
        attributes.add(new Attribute("bgsep"));
        attributes.add(new Attribute("width"));
        attributes.add(new Attribute("height"));
        trainset = new Instances("tags", attributes, 100);
        trainset.setClassIndex(0);
    }
    
    /**
     * Obtains the configured classifier.
     */
    private Classifier getClassifier()
    {
        if (classifier == null)
        {
            classifier = (Classifier) new IBk();
            try
            {
                classifier.buildClassifier(trainset);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return classifier;
    }

    //============================================================================
    
    public void dumpToFile(String file)
    {
        try
        {
            PrintWriter w = new PrintWriter(file);
            w.println(trainset.toString());
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}
