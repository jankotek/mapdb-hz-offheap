package com.hazelcast.map.impl.record;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public  class MapDBDataRecordSerializer extends Serializer<Record> implements Serializable {

    final static MapDBDataSerializer ds = new MapDBDataSerializer();


    @Override
    public void serialize(DataOutput out, Record value) throws IOException {
        DataRecordWithStats w = (DataRecordWithStats) value;
        ds.serialize(out,w.getKey());
        ds.serialize(out,w.getValue());
    }

    @Override
    public Record deserialize(DataInput in, int available) throws IOException {
        return new DataRecordWithStats(ds.deserialize(in,-1),ds.deserialize(in,-1));
    }


}