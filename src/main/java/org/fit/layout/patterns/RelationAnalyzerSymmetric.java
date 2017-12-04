/**
 * RelationAnalyzerSymmetric.java
 *
 * Created on 17. 3. 2016, 14:15:42 by burgetr
 */
package org.fit.layout.patterns;

import java.util.List;

import org.fit.layout.model.Area;

/**
 * A special case of the relation analyzer that includes both the normal and
 * the inverse relations where applicable.
 * @author burgetr
 */
public class RelationAnalyzerSymmetric extends RelationAnalyzer
{

    public RelationAnalyzerSymmetric(List<Area> areas)
    {
        super(areas);
    }

    @Override
    protected void initRelations()
    {
        addRelation(new RelationSide(false));
        addRelation(new RelationSide(true));
        addRelation(new RelationAfter(false));
        addRelation(new RelationAfter(true));
        addRelation(new RelationSameLine());
        addRelation(new RelationUnder());
        addRelation(new RelationUnderHeading());
        addRelation(new RelationBelow(false));
        addRelation(new RelationBelow(true));
    }

}
