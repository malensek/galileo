package galileo.test.dht.partitioning;

import static org.junit.Assert.*;

import galileo.dataset.BlockMetadata;
import galileo.dataset.Device;
import galileo.dataset.DeviceSet;
import galileo.dataset.SpatialProperties;
import galileo.dataset.TemporalProperties;
import galileo.dataset.feature.Feature;
import galileo.dataset.feature.FeatureSet;
import galileo.dht.GroupInfo;
import galileo.dht.NetworkInfo;
import galileo.dht.NodeInfo;
import galileo.dht.PartitionException;
import galileo.dht.SpatialHierarchyPartitioner;
import galileo.dht.hash.HashException;
import galileo.dht.hash.HashTopologyException;

import org.junit.Test;

/**
 * Tests the {@link SpatialHierarchyPartitioner}.
 */
public class SpatialHierarchy {

    private NetworkInfo ni;
    private SpatialHierarchyPartitioner partitioner;

    private String[] geohashes = { "c2", "c8", "cb", "f0", "f2",
        "9r", "9x", "9z", "dp", "dr",
        "9q", "9w", "9y", "dn", "dq",
        "9m", "9t", "9v", "dj" };


    public SpatialHierarchy()
    throws HashException, HashTopologyException, PartitionException {
        ni = new NetworkInfo();
        ni.addGroup(new GroupInfo("test group"));
        ni.addGroup(new GroupInfo("group2"));

        GroupInfo group = ni.getGroups().get(0);
        group.addNode(new NodeInfo("lattice-1", 5555));
        group.addNode(new NodeInfo("lattice-2", 5555));
        group.addNode(new NodeInfo("lattice-3", 5555));
        group.addNode(new NodeInfo("lattice-4", 5555));
        group.addNode(new NodeInfo("lattice-5", 5555));
        group.addNode(new NodeInfo("lattice-6", 5555));
        group.addNode(new NodeInfo("lattice-7", 5555));

        GroupInfo group2 = ni.getGroups().get(1);
        group2.addNode(new NodeInfo("lattice-8", 5555));
        group2.addNode(new NodeInfo("lattice-9", 5555));
        group2.addNode(new NodeInfo("lattice-10", 5555));
        group2.addNode(new NodeInfo("lattice-11", 5555));
        group2.addNode(new NodeInfo("lattice-12", 5555));

        partitioner = new SpatialHierarchyPartitioner(null, ni, geohashes);
    }

    @Test
    public void testGroup1() throws Exception {
        TemporalProperties tp = new TemporalProperties(System.nanoTime());
        SpatialProperties sp = new SpatialProperties(39.16f, -106.2f);

        FeatureSet fs = new FeatureSet();
        fs.put(new Feature("test1", 36.2));

        DeviceSet ds = new DeviceSet();


        BlockMetadata meta = new BlockMetadata(tp, sp, fs, ds);

        assertEquals("block1", "lattice-1:5555",
                partitioner.locateData(meta).toString());

        ds.put(new Device("testdev"));
        assertEquals("block2", "lattice-3:5555",
                partitioner.locateData(meta).toString());

        fs.put(new Feature("temperature", 32.3));
        fs.put(new Feature("humidity", 33.3));
        assertEquals("block2", "lattice-4:5555",
                partitioner.locateData(meta).toString());

        ds.put(new Device("my_gps"));
        ds.put(new Device("radar"));
        fs.put(new Feature("wind_velocity", 55.2));
        fs.put(new Feature("featureificness", 100));
        assertEquals("block2", "lattice-2:5555",
                partitioner.locateData(meta).toString());

        sp = new SpatialProperties(29.55f, -111.19f);
        meta = new BlockMetadata(tp, sp, fs, ds);
        assertEquals("block2", "lattice-2:5555",
                partitioner.locateData(meta).toString());
    }

    @Test
    public void testGroup2() throws Exception {
        TemporalProperties tp = new TemporalProperties(System.nanoTime());
        SpatialProperties sp = new SpatialProperties(42.36f, -85.37f);

        FeatureSet fs = new FeatureSet();
        fs.put(new Feature("test1", 36.2));

        DeviceSet ds = new DeviceSet();

        BlockMetadata meta = new BlockMetadata(tp, sp, fs, ds);

        /* Note how the relative positions should be the same in this group! */
        assertEquals("block1", "lattice-8:5555",
                partitioner.locateData(meta).toString());

        ds.put(new Device("testdev"));
        assertEquals("block2", "lattice-10:5555",
                partitioner.locateData(meta).toString());

        fs.put(new Feature("temperature", 32.3));
        fs.put(new Feature("humidity", 33.3));
        assertEquals("block2", "lattice-11:5555",
                partitioner.locateData(meta).toString());

        ds.put(new Device("my_gps"));
        ds.put(new Device("radar"));
        fs.put(new Feature("wind_velocity", 55.2));
        fs.put(new Feature("featureificness", 100));
        assertEquals("block2", "lattice-9:5555",
                partitioner.locateData(meta).toString());
    }

    @Test(expected = galileo.dht.hash.HashException.class)
    public void testOutofBounds() throws Exception {
        TemporalProperties tp = new TemporalProperties(System.nanoTime());
        /* Around the north pole... */
        SpatialProperties sp = new SpatialProperties(87.90f, -154.06f);

        FeatureSet fs = new FeatureSet();
        fs.put(new Feature("test1", 36.2));

        DeviceSet ds = new DeviceSet();

        BlockMetadata meta = new BlockMetadata(tp, sp, fs, ds);

        /* Note how the relative positions should be the same in this group! */

        assertEquals("block1", "lattice-8:5555",
                partitioner.locateData(meta).toString());
    }
}