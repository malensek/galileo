package edu.colostate.cs.galileo.adapters;

import edu.colostate.cs.galileo.dataset.feature.FeatureType;
import edu.colostate.cs.galileo.graph.FeatureHierarchy;
import edu.colostate.cs.galileo.graph2.Sketch;

public class BigSketch {

    /* Adds more and more data, while printing out graph stats */
    public static void main(String[] args)
    throws Exception {
        for (String featureName : TestConfiguration.FEATURE_NAMES) {
            ReadMetaBlob.activeFeatures.add(featureName);
        }

        FeatureHierarchy fh = new FeatureHierarchy();
        for (String featureName : TestConfiguration.FEATURE_NAMES) {
            System.out.println(
                    TestConfiguration.quantizers.get(featureName).numTicks()
                    + "   " + featureName);
            fh.addFeature(featureName, FeatureType.FLOAT);
        }
        //fh.addFeature("location", FeatureType.STRING);
        Sketch s = new Sketch(fh);

        long last = 0;
        for (String fileName : args) {
            ReadMetaBlob.loadData(fileName, s);
            System.out.println(s.getMetrics().getLeafCount() - last);
            last = s.getMetrics().getLeafCount();
        }

    }
}
