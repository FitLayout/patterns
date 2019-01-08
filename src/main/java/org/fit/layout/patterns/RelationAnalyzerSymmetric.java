/**
 * RelationAnalyzerSymmetric.java
 *
 * Created on 17. 3. 2016, 14:15:42 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.model.Area;

/**
 * A special case of the relation analyzer that includes both the normal and
 * the inverse relations where applicable.
 * @author burgetr
 */
public class RelationAnalyzerSymmetric extends RelationAnalyzer
{
    private static final List<Relation> ANALYZED_RELATIONS;
    static {
        ANALYZED_RELATIONS = new ArrayList<>(10);
        ANALYZED_RELATIONS.add(new SameLineRelations());
        ANALYZED_RELATIONS.add(new RelationUnder());
        ANALYZED_RELATIONS.add(new RelationUnderHeading());
        ANALYZED_RELATIONS.add(new RelationBelow(false));
        ANALYZED_RELATIONS.add(new RelationBelow(true));
        ANALYZED_RELATIONS.add(new RelationLineBelow());
    }

    public RelationAnalyzerSymmetric(List<Area> areas)
    {
        super(areas);
        setAnalyzedRelations(ANALYZED_RELATIONS);
    }

}
