package com.hazeclast.map.record;

import com.hazelcast.map.record.DataRecord;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.nio.serialization.MapDBDataSerializer;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public  class MapDBDataRecordSerializer implements Serializer<DataRecord>, Serializable {

        public static MapDBDataSerializer ds = new MapDBDataSerializer();

        @Override
        public void serialize(DataOutput dataOutput, DataRecord dataRecord) throws IOException {
            ds.serialize(dataOutput, dataRecord.getKey());
            ds.serialize(dataOutput, dataRecord.getValue());
            dataOutput.writeBoolean(dataRecord.getStatistics()!=null);

        }

        @Override
        public DataRecord deserialize(DataInput dataInput, int i) throws IOException {
            Data key = ds.deserialize(dataInput,-1);
            Data value = ds.deserialize(dataInput,-1);
            boolean stats = dataInput.readBoolean();
            return new DataRecord(key,value, stats);
        }

        @Override
        public int fixedSize() {
            return -1;
        }
    }