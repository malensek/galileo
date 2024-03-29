package edu.colostate.cs.galileo.adapters;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.colostate.cs.galileo.serialization.SerializationInputStream;
import edu.colostate.cs.galileo.util.Geohash;
import edu.colostate.cs.galileo.util.PerformanceTimer;

public class MergeSketch {

    public static void main(String[] args) throws Exception {
        SketchProcessor sp = new SketchProcessor();

        List<GeoHashIndexedRecord> records1 = createRecords(args[0]);
        List<GeoHashIndexedRecord> records2 = createRecords(args[1]);

        PerformanceTimer pt = new PerformanceTimer("time");

        System.out.println("Building sketch...");
        pt.start();
        for (GeoHashIndexedRecord record : records1) {
            sp.process(record);
        }
        pt.stopAndPrint();
        System.out.println(sp.getGraphMetrics());

        System.out.println("Splitting...");
        pt.start();
        byte[] region = sp.split("z");
        pt.stopAndPrint();
        System.out.println(sp.getGraphMetrics());

        System.out.println("Building new sketch");
        SketchProcessor miniSp = new SketchProcessor();
        miniSp.merge(null, region);
        System.out.println(miniSp.getGraphMetrics());
        for (GeoHashIndexedRecord record : records2) {
            miniSp.process(record);
        }
        byte[] entireRegion = miniSp.split("");

        System.out.println("Merging...");
        pt.start();
        sp.merge(null, entireRegion);
        pt.stopAndPrint();
        System.out.println(sp.getGraphMetrics());

        byte[] big = sp.split("9");
        sp.merge(null, big);
        System.out.println(sp.getGraphMetrics());

        sp.split("9");
        sp.split("d");
        sp.split("c");
        sp.split("f");
        sp.split("b");
        sp.split("8");

        System.out.println(sp.getGraphMetrics());
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        System.gc();
        System.out.println();
        System.out.println("max=" + runtime.maxMemory());
        System.out.println("total=" + runtime.totalMemory());
        System.out.println("free=" + runtime.freeMemory());
        System.out.println("used=" + (runtime.totalMemory() - runtime.freeMemory()));

        sp.split("");
        System.out.println(sp.getGraphMetrics());
    }

    public static List<GeoHashIndexedRecord> createRecords(String fileName)
    throws Exception {
        System.out.println("Reading metadata blob: " + fileName);
        FileInputStream fIn = new FileInputStream(fileName);
        BufferedInputStream bIn = new BufferedInputStream(fIn);
        SerializationInputStream in = new SerializationInputStream(bIn);

        int num = in.readInt();
        System.out.println("Records: " + num);

        List<GeoHashIndexedRecord> records = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            float lat = in.readFloat();
            float lon = in.readFloat();
            byte[] payload = in.readField();
            GeoHashIndexedRecord rec = new GeoHashIndexedRecord(
                    payload, Geohash.encode(lat, lon, 4));
            records.add(rec);

            if (i % 5000 == 0) {
                System.out.print('.');
            }
        }
        System.out.println();

        in.close();

        return records;
    }
}
