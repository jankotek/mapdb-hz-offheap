package org.mapdb.hzoffheap;

import javassist.ClassPool;
import javassist.CtClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Java agent which modifies classes to use MapDB off-heap maps 
 *
 * <a href="http://www.javassist.org/">javassist</a>.
 *  
 * <p>inspired by http://today.java.net/article/2008/04/22/add-logging-class-load-time-java-instrumentation</p>
 * <p>also inspired by https://github.com/kreyssel/maven-examples/tree/master/javaagent</p>
 */
public class HzOffheapAgent implements ClassFileTransformer {

    static final String DEFAULT_RECORD_STORE ="com/hazelcast/map/impl/AbstractRecordStore";


    static final Logger LOG = Logger.getLogger(HzOffheapAgent.class.getName());

    /**
     * add agent
     */
    public static void premain( final String agentArgument, final Instrumentation instrumentation ) {
        instrumentation.addTransformer( new HzOffheapAgent() );
    }

    /**
     * instrument class
     */
    public byte[] transform( final ClassLoader loader, final String className, final Class clazz,
        final java.security.ProtectionDomain domain, final byte[] bytes ) {

        if(DEFAULT_RECORD_STORE.equals(className)) {
            return doDefaultRecordStore(className, clazz, bytes);
        }

        return bytes;
    }

    private byte[] doDefaultRecordStore(final String name, final Class clazz, byte[] b) {

        LOG.log(Level.INFO, "mapdb-hz-offheap agent: starting code injection");

        ClassPool pool = ClassPool.getDefault();
        CtClass cl = null;

        try {
            cl = pool.makeClass( new java.io.ByteArrayInputStream( b ) );

            final String CONSTRUCTOR = "records="+HzOffheap.class.getName()+".defaultRecordStoreRecords();";

            cl.getDeclaredConstructors()[0].insertAfter(CONSTRUCTOR);

            b = cl.toBytecode();

            LOG.log(Level.INFO, "mapdb-hz-offheap agent: code injected.");

        } catch( Exception e ) {
            LOG.log(Level.SEVERE, "Could not instrument Hazelcast to use off-heap store " + name, e);
            throw new RuntimeException( "Could not instrument Hazelcast to use off-heap store ", e);
        } finally {

            if( cl != null ) {
                cl.detach();
            }
        }

        return b;
    }

}
