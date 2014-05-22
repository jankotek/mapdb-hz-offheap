package org.mapdb.hzoffheap;

import com.hazeclast.map.record.MapDBDataRecordSerializer;
import com.hazelcast.nio.serialization.MapDBDataSerializer;
import org.mapdb.DBMaker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Factory for off-heap maps
 */
public class HzOffheap {

    static final Logger LOG = Logger.getLogger(HzOffheap.class.getName());

    static AtomicBoolean logged = new AtomicBoolean(false);

    /**
     * Creates new off-heap map for HZ, called from instrumented code
     */
    public static ConcurrentMap  defaultRecordStoreRecords(){

        if(!logged.getAndSet(true)) {
            LOG.info("mapdb-hz-offheap: MapDB HashMap instantiated. It works!");
        }


        return DBMaker
                .newMemoryDirectDB()
                .transactionDisable()
                .cacheDisable()
                .make()
                .createHashMap("recods")
                .keySerializer(new MapDBDataSerializer())
                .valueSerializer(new MapDBDataRecordSerializer())
                .counterEnable()
                .make();
    }

}

