HZ
=====

This project provides off-heap storage for HZ Maps. It is unofficial extension and is not supported by 
Hazelcast Inc.  Inside HZ it replaces `ConcurrentHashMap`  with off-heap `HTreeMap` from [MapDB](http://www.mapdb.org). 
It is independent from other off-heap solutions and is free under Apache License 2.0 with no hooks attached. 

Author works on embedded database engine and this is demonstration of its abilities.
This extension should be usable in production, there is [free community](https://github.com/jankotek/mapdb-hz-offheap/issues) 
and [commercial support](http://www.kotek.net/consulting/)     

Intro
---------
HC uses Java maps for its internal data structures. Those are visible to Garbage Collector and do not scale well 
 with large number of items. MapDB offers off-heap collections not affected by GC. This project combines HZ and MapDB together.
  
This project makes very simple change inside HZ. At class `com.hazelcast.map.impl.AbstractRecordStore` it replaces value of 
map `records` with MapDB collection:
 
```java

    private final ConcurrentMap<Data, Record> records = 

        // ORIGINAL: 
        //new ConcurrentHashMap<Data, Record>(1000, 0.75f, 1);
        
        // NEW VERSION:         
        DBMaker
            .memoryDirectDB()
            .transactionDisable()
            .make()
            .hashMapCreate("recods")
            .keySerializer(new MapDBDataSerializer())
            .valueSerializer(new MapDBDataRecordSerializer())
            .counterEnable()
            .make();
```

It also provides Java Agent to instrument HZ classes at runtime  while they are loading. 
So there is no need to swap HZ binaries. 

Versioning
-------------

Right now (August 2015) this patch targets Hazelcast 3.5.N and JVM 6,7,8. It probably does not work with other releases. However it is trivial 
to port it to other versions (see older releases) and we will keep updating it together with Hazelcast. 

This patch follows Hazelcast versioning: `3.5.A`. First two numbers corresponds to major and minor Hazelcast release.
Last letter is release number of this library (A is first, B is second...). 

Version such as `3.5.A`, `3.5.B` etc should work with all `3.5.N` HZ releases such as `3.5.1` or `3.5.2`. 

Lattest version number can be found in [Maven Central](http://mvnrepository.com/artifact/org.mapdb/mapdb-hz-offheap).

Using it
---------

There are two components. First add library into your Maven dependencies. It contains serializers and some helper methods.
If you are not using Maven, you will also need MapDB library. Do not forget to replace `VERSION` 
with current version of this library (such as `3.5.A`)

```xml
    <dependencies>
        <dependency>
            <groupId>org.mapdb</groupId>
            <artifactId>mapdb-hz-offheap</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>
```

Secondly download `mapdb-hz-offheap-VERSION-javaagent.jar` and use it as `-javaagent` for JVM. When JVM starts, 
it will intercept class loading and patch Hazelcast classes to use MapDB collections. 
Jar files can be downloaded from [maven central](http://search.maven.org/#browse%7C1316374908). 

Also do not forget to increase off-heap memory for Direct ByteBuffers with JVM switch: `-XX:MaxDirectMemorySize=25G`

```
    java   -XX:MaxDirectMemorySize=25G    -javaagent:mapdb-hz-offheap-VERSION-javaagent.jar     com.example.YourMainClass
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


[Simple benchmark](https://github.com/jankotek/mapdb-hz-offheap/blob/master/src/test/java/org/mapdb/hzoffheap/Benchmark.java)
to verify usability of this extension. Original Hazelcast has 30% faster insertion rate.
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

It tested Hazelcast 3.2 on 64bit Kubuntu 14.04 with J64bit Oracle JVM 1.7.0_51 with 32GB RAM.

Original version ran with 25GB heap: `-Xmx25G`. After 30 minutes all memory was used and Garbage Collector kicked in consuming 100% CPU. 
No GC tuning was applied, that could probably fix this issue for a few extra gigabytes. 

Off-heap version ran with 128MB heap and 25GB off-heap: `-Xmx128M -XX:MaxDirectMemorySize=25G`. It maintained constant 
insert speed for duration of experiment (1 hour). GC usage was always bellow 1%

Results:

Unpatched Hazelcast inserted  81.8 million items over 30 minutes. It consumed 19.4 GB of heap memory. After 30 minutes experiment was stopped due to excesive GC.

mapdb-hz-offheap inserted 90.7 millions items over one hour. It consumed 60MB of heap and 9.8 GB of total memory as shown in Process Explorer. 
It finished experiment without any performance degradation, it should scale to 100GB+ without problems.   

Support 
---------

This is unofficial extension and is not supported by Hazelcast Inc. and their community.
Please do not open bug reports at their site, unless you can reproduce bug on unpatched HZ without this extension. 

This extension should be usable in production, 
there is [free community](https://github.com/jankotek/mapdb-hz-offheap/issues) 
and [commercial support](http://www.kotek.net/consulting/).
     

Future
----------

This patch was created to demonstrate MapDB abilities and versatility. It is trivial and I will support it 
for free with future versions of HZ, unless there is a huge architectural change.

In near future I would like to patch HZ with new extension points, so this patch does not require Java Agent, 
but can be enabled directly in configuration files. Also MapDB has many configuration options
(async write, cache, `byte[]` versus `DirectByteBuffer`) which should be available to user. 

I would like to provide some sort of clustering version of [MapDB](http://www.mapdb.org). 
But I am not sure that Hazelcast is best choice for that. 
Perhaps building on top of Zookeeper, GridGain or other memory grid would be better. 


 
 