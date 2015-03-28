/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2000,2007 Oracle.  All rights reserved.
 *
 * $Id: TyRuBaSerialBinding.java,v 1.1 2007/08/25 02:07:12 kdvolder Exp $
 */

package tyRuBa.engine.factbase.berkeley_db;

import java.io.IOException;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialInput;
import com.sleepycat.bind.serial.SerialOutput;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.util.FastInputStream;
import com.sleepycat.util.FastOutputStream;
import com.sleepycat.util.RuntimeExceptionWrapper;

/**
 * A concrete <code>EntryBinding</code> that treats a key or data entry as
 * a serialized object.
 *
 * <p>This binding stores objects in serialized object format.  The
 * deserialized objects are returned by the binding, and their
 * <code>Class</code> must implement the <code>Serializable</code>
 * interface.</p>
 *
 * <p>For key bindings, a tuple binding is usually a better choice than a
 * serial binding.  A tuple binding gives a reasonable sort order, and works
 * with comparators in all cases -- see below.</p>
 *
 * <p><em>WARNING:</em> SerialBinding should not be used with Berkeley DB Java
 * Edition for key bindings, when a custom comparator is used.  In JE,
 * comparators are instantiated and called internally at times when databases
 * are not accessible.  Because serial bindings depend on the class catalog
 * database, a serial binding cannot be used during these times.  An attempt
 * to use a serial binding with a custom comparator will result in a
 * NullPointerException during environment open or close.</p>
 *
 * @author Mark Hayes
 */
public class TyRuBaSerialBinding extends TupleBinding implements EntryBinding {

    private ClassCatalog classCatalog;
    private Class baseClass;

    /**
     * Creates a serial binding.
     *
     * @param classCatalog is the catalog to hold shared class information and
     * for a database should be a {@link StoredClassCatalog}.
     *
     * @param baseClass is the base class for serialized objects stored using
     * this binding -- all objects using this binding must be an instance of
     * this class.
     */
    public TyRuBaSerialBinding(ClassCatalog classCatalog, Class baseClass) {

        if (classCatalog == null) {
            throw new NullPointerException("classCatalog must be non-null");
        }
        this.classCatalog = classCatalog;
        this.baseClass = baseClass;
    }

    /**
     * Returns the base class for this binding.
     *
     * @return the base class for this binding.
     */
    public final Class getBaseClass() {

        return baseClass;
    }

    /**
     * Returns the class loader to be used during deserialization, or null if
     * a default class loader should be used.  The default implementation of
     * this method returns
     * <code>Thread.currentThread().getContextClassLoader()</code> to use the
     * context class loader for the current thread.
     *
     * <p>This method may be overriden to return a dynamically determined class
     * loader.  For example, <code>getBaseClass().getClassLoader()</code> could
     * be called to use the class loader for the base class, assuming that a
     * base class has been specified.</p>
     *
     * <p>If this method returns null, a default class loader will be used as
     * determined by the <code>java.io.ObjectInputStream.resolveClass</code>
     * method.</p>
     */
    public ClassLoader getClassLoader() {

        return Thread.currentThread().getContextClassLoader();
    }

	@Override
	public Object entryToObject(TupleInput input) {
        int length = input.getBufferLength()-input.getBufferOffset();
        byte[] hdr = SerialOutput.getStreamHeader();
        byte[] bufWithHeader = new byte[length + hdr.length];

        System.arraycopy(hdr, 0, bufWithHeader, 0, hdr.length);
        System.arraycopy(input.getBufferBytes(), input.getBufferOffset(),
                         bufWithHeader, hdr.length, length);

        try {
        	FastInputStream fin = new FastInputStream(bufWithHeader, 0, bufWithHeader.length);
            SerialInput jin = new SerialInput(
                fin,
                classCatalog,
                getClassLoader());
            Object result = jin.readObject();
            input.skipFast(fin.getBufferOffset()-hdr.length);
            return result;
        } catch (IOException e) {
            throw new RuntimeExceptionWrapper(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeExceptionWrapper(e);
        }
	}

	@Override
	public void objectToEntry(Object object, TupleOutput tuple_out) {

        if (baseClass != null && !baseClass.isInstance(object)) {
            throw new IllegalArgumentException(
                        "Data object class (" + object.getClass() +
                        ") not an instance of binding's base class (" +
                        baseClass + ')');
        }
        FastOutputStream fo = new FastOutputStream();
        try {
            SerialOutput jos = new SerialOutput(fo, classCatalog);
            jos.writeObject(object);
        } catch (IOException e) {
            throw new RuntimeExceptionWrapper(e);
        }

        byte[] hdr = SerialOutput.getStreamHeader();
        try {
			tuple_out.write(fo.getBufferBytes(), hdr.length,
			             fo.getBufferLength() - hdr.length);
		} catch (IOException e) {
			throw new Error(e);
		}
	}
}
