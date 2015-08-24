package com.hazelcast.map.impl.record;

import com.hazelcast.nio.serialization.Data;
import com.hazelcast.nio.serialization.DefaultData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class MapDBDataSerializer extends org.mapdb.Serializer<Data> implements Serializable {

    @Override
    public void serialize(DataOutput dataOutput, Data data) throws IOException {
        byte[] b = data.toByteArray();
        dataOutput.writeInt(b.length);
        dataOutput.write(b);
    }

    @Override
    public Data deserialize(DataInput dataInput, int i) throws IOException {
        int size = dataInput.readInt();
        byte[] b = new byte[size];
        dataInput.readFully(b);
        return new DefaultData(b);
    }

    @Override
    public int fixedSize() {
        return -1;
    }
}