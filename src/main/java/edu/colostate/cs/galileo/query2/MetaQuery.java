/*
Copyright (c) 2016, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package edu.colostate.cs.galileo.query2;

import java.util.List;
import java.util.Set;

import edu.colostate.cs.galileo.dataset.feature.FeatureType;
import edu.colostate.cs.galileo.graph2.DataContainer;
import edu.colostate.cs.galileo.graph2.Vertex;

/**
 * Retrieves metadata records stored in {@link DataContainer} instances and
 * merges them into a final result. For example, this could retrieve all the
 * features matching a particular set of values and then merge their summary
 * statistics into a single record.
 *
 * @author malensek
 */
public class MetaQuery extends Query {

    private DataContainer aggregateData = new DataContainer();

    public MetaQuery() {

    }

    public DataContainer result() {
        return aggregateData;
    }

    @Override
    public void execute(Vertex root)
    throws QueryException {
        this.query(root);
    }

    private void query(Vertex vertex)
    throws QueryException {
        DataContainer container = vertex.getData();
        if (container != null) {
            this.aggregateData.merge(container);
        }

        if (vertex.numNeighbors() == 0) {
            /* This is a leaf node */
            return;
        }

        String childFeature = vertex.getFirstNeighbor().getLabel().getName();
        List<Expression> expList = this.expressions.get(childFeature);
        if (expList != null) {
            Set<Vertex> matches = evaluate(vertex, expList);
            for (Vertex match : matches) {
                if (match == null) {
                    continue;
                }

                if (match.getLabel().getType() == FeatureType.NULL) {
                    continue;
                }

                query(match);
            }
        } else {
            /* No expression operates on this vertex. Consider all children. */
            for (Vertex neighbor : vertex.getAllNeighbors()) {
                query(neighbor);
            }
        }
    }
}

