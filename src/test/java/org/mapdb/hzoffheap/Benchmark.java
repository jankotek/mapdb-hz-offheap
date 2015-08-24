package org.mapdb.hzoffheap;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Benchmark {

    public static void main(String[] args) {

        Config cfg = new Config();

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);


        Map<Long, String> mapCustomers = instance.getMap("map");


        final long timeLen = 60*60*1000;
        final long startTime =System.currentTimeMillis();

        long i=0;
        for(;startTime+timeLen>System.currentTimeMillis();i++){
            if(i % 10000==0)
                System.out.printf("ITEMS: %,d   HEAP: %,d       %d %% \r",i,Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory(), 100*(System.currentTimeMillis()-startTime)/timeLen);

            mapCustomers.put(i, "string"+i);
        }

        System.out.println("DONE");
        System.out.printf("ITEMS:%,d   HEAP: %,d  ",i,Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());



    }
}