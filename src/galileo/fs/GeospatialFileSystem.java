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

package galileo.fs;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import galileo.dataset.Block;
import galileo.dataset.Coordinates;
import galileo.dataset.Metadata;
import galileo.dataset.SpatialProperties;
import galileo.graph.FeaturePath;
import galileo.graph.MetadataGraph;
import galileo.query.Query;
import galileo.serialization.SerializationException;
import galileo.serialization.Serializer;
import galileo.util.GeoHash;
import galileo.util.StackTraceToString;

/**
 * Implements a {@link FileSystem} for Geospatial data.  This file system
 * manager assumes that the information being stored has both space and time
 * properties.
 * <p>
 * Relevant system properties include
 * galileo.fs.GeospatialFileSystem.timeFormat and
 * galileo.fs.GeospatialFileSystem.geohashPrecision
 * to modify how the hierarchy is created.
 */
public class GeospatialFileSystem extends FileSystem {

    private static final Logger logger = Logger.getLogger("galileo");

    private static final String DEFAULT_TIME_FORMAT = "yyyy/M/d";
    private static final int DEFAULT_GEOHASH_PRECISION = 5;

    private static final String metadataStore = "metadata.graph";

    private MetadataGraph metadataGraph;

    private SimpleDateFormat timeFormatter;
    private String timeFormat;
    private int geohashPrecision;

    public GeospatialFileSystem(String storageDirectory)
    throws FileSystemException, IOException, SerializationException {
        super(storageDirectory);

        this.timeFormat = System.getProperty(
                "galileo.fs.GeospatialFileSystem.timeFormat",
                DEFAULT_TIME_FORMAT);
        this.geohashPrecision = Integer.parseInt(System.getProperty(
                "galileo.fs.GeospatialFileSystem.geohashPrecision",
                DEFAULT_GEOHASH_PRECISION + ""));

        timeFormatter = new SimpleDateFormat();
        timeFormatter.applyPattern(timeFormat);

        File metaFile = new File(storageDirectory + "/" + metadataStore);
        if (metaFile.exists()) {
            metadataGraph = Serializer.restore(MetadataGraph.class, metaFile);
            //TODO verification
        } else {
            metadataGraph = new MetadataGraph();
        }
    }

    @Override
    public String storeBlock(Block block)
    throws FileSystemException, IOException {
        String name = block.getMetadata().getName();
        if (name.equals("")) {
            UUID blockUUID = UUID.nameUUIDFromBytes(block.getData());
            name = blockUUID.toString();
        }

        String blockDirPath = storageDirectory + "/"
            + getStorageDirectory(block);
        String blockPath = blockDirPath + "/" + name
            + FileSystem.BLOCK_EXTENSION;

        /* Ensure the storage directory is there. */
        File blockDirectory = new File(blockDirPath);
        if (!blockDirectory.exists()) {
            if (!blockDirectory.mkdirs()) {
                throw new IOException("Failed to create directory (" +
                    blockDirPath + ") for block.");
            }
        }

        FileOutputStream blockOutStream = new FileOutputStream(blockPath);
        byte[] blockData = Serializer.serialize(block);
        blockOutStream.write(blockData);
        blockOutStream.close();

        Metadata meta = block.getMetadata();
        FeaturePath<String> path = createPath(blockPath, meta);

        try {
            metadataGraph.addPath(path);
        } catch (Exception e) {
            throw new FileSystemException("Error storing block: "
                    + e.getClass().getCanonicalName() + ":"
                    + System.lineSeparator() +
                    StackTraceToString.convert(e));
        }

        return blockPath;
    }

    /**
     * Given a {@link Block}, determine its storage directory on disk.
     *
     * @param block The Block to inspect
     *
     * @return String representation of the directory on disk this Block should
     * be stored in.
     */
    private String getStorageDirectory(Block block) {
        String directory = "";

        Metadata meta = block.getMetadata();
        Date date = meta.getTemporalProperties().getLowerBound();

        directory = timeFormatter.format(date) + "/";

        Coordinates coords = null;
        SpatialProperties spatialProps = meta.getSpatialProperties();
        if (spatialProps.hasRange()) {
            coords = spatialProps.getSpatialRange().getCenterPoint();
        } else {
            coords = spatialProps.getCoordinates();
        }
        directory += GeoHash.encode(coords, geohashPrecision);

        return directory;
    }

    /**
     * Using the Feature attributes found in the provided Metadata, a
     * path is created for insertion into the Metadata Graph.
     */
    protected FeaturePath<String> createPath(
            String physicalPath, Metadata meta) {

        FeaturePath<String> path = new FeaturePath<String>(
                physicalPath, meta.getAttributes().toArray());

        return path;
    }

    @Override
    protected void storeMetadata(String blockPath, Metadata metadata)
    throws FileSystemException, IOException {
        FeaturePath<String> path = createPath(blockPath, metadata);

        try {
            metadataGraph.addPath(path);
        } catch (Exception e) {
            throw new FileSystemException("Error storing block: "
                    + e.getClass().getCanonicalName() + ":"
                    + System.lineSeparator() +
                    StackTraceToString.convert(e));
        }
    }

    public MetadataGraph query(Query query) {
        return metadataGraph.evaluateQuery(query);
    }

    public void shutdown() {
        try {
            Serializer.persist(metadataGraph,
                    storageDirectory + "/" + metadataStore);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing persistent index file", e);
        }
    }
}
