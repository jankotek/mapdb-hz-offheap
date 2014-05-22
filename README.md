This project offers off-heap store for Hazelcast Maps. Inside HZ it replaces `ConcurrentHashMap`  with off-heap `HTreeMap` from MapDB. 
It is independent from other off-heap solutions and is free under Apache License 2.0 with no hooks attached. 
  

Intro
---------
HC uses Java maps for its internal data structures. Those are visible to Garbage Collector and do not scale well 
 with large number of items. MapDB offers off-heap collections not affected by GC. This project combines Hazelcast and MapDB together.
  
This project makes very simple change inside Hazelcast. At class `com.hazelcast.map.DefaultRecordStore` it replaces value of 
map `records` with MapDB collection:
 
```java

    private final ConcurrentMap<Data, Record> records = 

        // ORIGINAL: 
        // new ConcurrentHashMap<Data, Record>(1000);
        
        // NEW VERSION:         
        DBMaker
            .newMemoryDirectDB()
            .transactionDisable()
            .cacheDisable()
            .make()
            .createHashMap("recods")
            .keySerializer(new MapDBDataSerializer())
            .valueSerializer(new MapDBDataRecordSerializer())
            .counterEnable()
            .make();
```

It also provides Java Agent to instrumens Hazelcast classes directly while they are loading. 
So there is no need to deploy specialized fork.
 


Using it
---------

This patch was tested with Hazelcast 3.1 and 3.2 and JVM 6,7,8. It does not work with Hazelcast 3.0 or 3.3.

There are two components. First add library into your Maven dependencies. It contains serializers and some helper methods.
If you are not using Maven, you will also need MapDB library.

```xml
    <dependencies>
        <dependency>
            <groupId>org.mapdb</groupId>
            <artifactId>mapdb-hz-offheap</artifactId>
            <version>0.8</version>
        </dependency>
    </dependencies>
```

Secondly download `mapdb-hz-offheap-0.8-javaagent.jar` and use it as `-javaagent` for JVM. It will patch Hazelcast
to use MapDB collections. Jar files can be downloaded from [maven central](http://repo1.maven.org/maven2/org/mapdb/mapdb-hz-offheap). 

Also do not forget to increase off-heap memory for Direct ByteBuffers with JVM switch: `-XX:MaxDirectMemorySize=25G`

```
    java   -XX:MaxDirectMemorySize=25G    -javaagent:mapdb-hz-offheap-0.8-javaagent.jar     com.example.YourMainClass
```


If Hazelcast was patched successfully you will see following code in your logger:
 
```
    May 21, 2014 1:42:15 PM org.mapdb.hzoffheap.HzOffheapAgent doDefaultRecordStore
    INFO: mapdb-hz-offheap agent: starting code injection
    
    May 21, 2014 1:42:15 PM org.mapdb.hzoffheap.HzOffheapAgent doDefaultRecordStore
    INFO: mapdb-hz-offheap agent: code injected.
    
    May 21, 2014 1:42:15 PM org.mapdb.hzoffheap.HzOffheap defaultRecordStoreRecords
    INFO: mapdb-hz-offheap: MapDB HashMap instantiated. It works!
```

Benchmark
------------


Simple benchmark to verify usability of this extension. Original Hazelcast has 30% faster insertion rate.
 But this extension fits twice more data into same memory and its performance does not degrade with large data-sets.

Benchmark inserts items into Hazelcast Map for one hour on single node:

```java  
    
    HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
    Map<Long, String> map = instance.getMap("map");
  
    for(long i=0;;i++){
        map.put(i, "string"+i);
        //break if one hour passed
    }
```

It tested Hazelcast 3.2 on 64bit Kubuntu 14.04 with JVM 64bit Oracle JVM 1.7.0_51 with 32GB RAM.

Unpatched Hazelcast ran with 25GB heap: `-Xmx25G`. After 30 minutes it ran out of free heap and freezed with GC consuming 100% CPU. 

Off-heap version ran with 128MB heap and 25GB off-heap: `-Xmx128M -XX:MaxDirectMemorySize=25G`. It maintained constant 
insert speed for duration of experiment (1 hour). GC usage was always bellow 1%

Results:

*Unpatched Hazelcast* inserted  81.8 million items over 30 minutes. It consumed 19.4 GB of heap memory. It could not finish experiment due to GC overhead. 

*mapdb-hz-offheap* inserted 90.7 millions items over one hour. It consumed 60MB of heap and 9.8 GB of total memory as shown in Process Explorer. 
It finished experiment without any performance degradation, it should scale to 100GB+ without problems.   


Future
----------

This is just quick demonstration created in one afternoon. It uses well tested library (MapDB) and should be usable in production.
It was created to show MapDB abilities. 

It only partially patches  HC Maps to be off-heap. 
Some housekeeping data structures (probably expiration) are still on-heap. 
Also other data types such as Queues and Locks are on-heap.
 
In future we may extend this patch to other HC data structures and make HC completely off-heap. 
It would be great to merge this patch into upstream Hazelcast. 
Patching classes at runtime with Java Agent is limited, so we may also create fork Hazelcast in future. 
