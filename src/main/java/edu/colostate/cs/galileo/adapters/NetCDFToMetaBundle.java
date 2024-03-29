/*
Copyright (c) 2014, Colorado State University
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

package edu.colostate.cs.galileo.adapters;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPoint;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import edu.colostate.cs.galileo.dataset.MetaArray;
import edu.colostate.cs.galileo.dataset.Metadata;
import edu.colostate.cs.galileo.dataset.Pair;
import edu.colostate.cs.galileo.dataset.SpatialProperties;
import edu.colostate.cs.galileo.dataset.TemporalProperties;
import edu.colostate.cs.galileo.dataset.feature.Feature;
import edu.colostate.cs.galileo.serialization.Serializer;
import edu.colostate.cs.galileo.util.FileNames;
import edu.colostate.cs.galileo.util.Geohash;

public class NetCDFToMetaBundle {
    public static void main(String[] args)
    throws Exception {
        /* This should be enabled in situations where the user account does not
         * have write permissions to the parent directory of the file arg. */
        //DiskCache.setCachePolicy(true);

        File f = new File(args[0]);
        Pair<String, String> nameParts = FileNames.splitExtension(f);
        String ext = nameParts.b;

        if (ext.equals("grb") || ext.equals("bz2") || ext.equals("gz")) {
            System.out.println("Reading netcdf...");
            Map<String, Metadata> metaMap
                = NetCDFToMetaBundle.readFile(f.getAbsolutePath());

            /* Don't cache more than 4 GB: */
            /* (This should be disabled when multiple processes are using the
             * cache concurrently) */
            //DiskCache.cleanCache(4 * 1000 * 1000 * 1000, null);

            System.out.println("Creating bundle...");
            MetaArray metaBundle = new MetaArray();
            for (String s : metaMap.keySet()) {
                Metadata m = metaMap.get(s);
                metaBundle.add(m);
            }

            System.out.println("Writing bundle...");
            Serializer.persist(metaBundle, nameParts.a + ".mbundle");
        }
    }

    public static Map<String, Metadata> readFile(String file)
        throws Exception {
        NetcdfFile n = NetcdfFile.open(file);
        System.out.println("Opened: " + file);

        /* Determine the size of our grid */
        int xLen = n.findDimension("x").getLength();
        int yLen = n.findDimension("y").getLength();
        System.out.println("Grid size: " + xLen + "x" + yLen);

        /* What time is this set of readings for? */
        Variable timeVar = n.findVariable("time");
        String timeStr = timeVar.getUnitsString().toUpperCase();
        timeStr = timeStr.replace("HOURS SINCE ", "");
        timeStr = timeStr.replace("HOUR SINCE ", "");

        /* Find the base date (the day) the reading was taken */
        Date baseDate
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse(timeStr);

        /* Get the number of hours since the base date this reading was taken */
        int offset = timeVar.read().getInt(0);

        /* Generate the actual date for this reading */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + offset);
        System.out.println("Time of collection: " + calendar.getTime());

        /* We'll keep a mapping of geolocations -> Galileo Metadata */
        Map<String, Metadata> metaMap = new HashMap<>();

        /* Determine the lat, lon coordinates for the grid points, and get each
         * reading at each grid point. */
            NetcdfDataset dataset = new NetcdfDataset(n);
            GridDataset gridData = new GridDataset(dataset);
            for (GridDatatype g : gridData.getGrids()) {
                /* Let's look at 3D variables: these have WxH dimensions, plus a
                 * single plane.  A 4D variable would contain elevation
                 * and multiple planes as a result */
                if (g.getShape().length == 3) {
                    convert3DVariable(g, calendar.getTime(), metaMap);
                }
            }
            gridData.close();
            dataset.close();
            n.close();
            return metaMap;
    }

    private static void convert3DVariable(GridDatatype g, Date date,
            Map<String, Metadata> metaMap) throws IOException {

        Variable v = g.getVariable();
        System.out.println("Reading: " + v.getFullName());
        Array values = v.read();

        int h = v.getShape(1);
        int w = v.getShape(2);

        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w; ++j) {
                LatLonPoint pt = g.getCoordinateSystem().getLatLon(j, i);
                String hash = Geohash.encode(
                        (float) pt.getLatitude(),
                        (float) pt.getLongitude(), 10).toLowerCase();

                Metadata meta = metaMap.get(hash);
                if (meta == null) {
                    /* We need to create Metadata for this location */
                    meta = new Metadata();

                    UUID metaUUID = UUID.nameUUIDFromBytes(hash.getBytes());
                    meta.setName(metaUUID.toString());

                    SpatialProperties location = new SpatialProperties(
                            (float) pt.getLatitude(),
                            (float) pt.getLongitude());
                    meta.setSpatialProperties(location);

                    TemporalProperties time
                        = new TemporalProperties(date.getTime());
                    meta.setTemporalProperties(time);

                    metaMap.put(hash, meta);
                }

                String featureName = v.getFullName().toLowerCase();
                float featureValue = values.getFloat(i * w + j);
                if (Float.isNaN(featureValue) == false) {
                    Feature feature = new Feature(featureName, featureValue);
                    meta.putAttribute(feature);
                }
            }
        }
    }
}
