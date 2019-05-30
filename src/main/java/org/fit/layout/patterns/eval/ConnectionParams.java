/**
 * MatchParams.java
 *
 * Created on 30. 5. 2019, 13:14:21 by burgetr
 */
package org.fit.layout.patterns.eval;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.patterns.model.Match;
import org.fit.layout.patterns.model.Match.ConnectionMatch;

/**
 * Parametres of a set of matches.
 * 
 * @author burgetr
 */
public class ConnectionParams
{
    private List<MatchParams> params;
    
    public ConnectionParams(List<ConnectionMatch> matches)
    {
        params = new ArrayList<>(matches.size());
        for (ConnectionMatch match : matches)
        {
            params.add(getMatchParams(match));
        }
    }

    private MatchParams getMatchParams(ConnectionMatch match)
    {
        MatchParams ret = new MatchParams();
        
        return ret;
    }
    
    //========================================================================================
    
    private static class MatchParams extends ArrayList<Param>
    {
        
    }
    
    private static class Param
    {
        public float value;
        
        public Param(float value)
        {
            this.value = value;
        }
    }
    
}
