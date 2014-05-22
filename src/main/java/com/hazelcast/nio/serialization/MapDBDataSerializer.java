package com.hazelcast.nio.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class MapDBDataSerializer implements org.mapdb.Serializer<Data>, Serializable {

    @Override
    public void serialize(DataOutput dataOutput, Data data) throws IOException {
        dataOutput.writeInt(data.getType());
        dataOutput.writeInt(data.getBuffer().length);
        dataOutput.write(data.getBuffer());
        dataOutput.writeInt(data.getPartitionHash());
    }

    @Override
    public Data deserialize(DataInput dataInput, int i) throws IOException {
        int type = dataInput.readInt();
        byte[] buf = new byte[dataInput.readInt()];
        dataInput.readFully(buf);
        Data d = new Data(type, buf);
        d.partitionHash = dataInput.readInt();
        return d;
    }

    @Override
    public int fixedSize() {
        return -1;
    }
}
