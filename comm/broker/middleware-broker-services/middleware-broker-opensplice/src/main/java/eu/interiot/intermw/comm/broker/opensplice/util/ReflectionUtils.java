/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union<92>s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 *
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 *
 *
 * For more information, contact:
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 *
 *
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.comm.broker.opensplice.util;

import DDS.DataReader;
import DDS.DataWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The following methods are used to connect to a DDS by using Java reflection
 *
 * Sept 2016
 *
 * @author jldominguez
 *
 */
public class ReflectionUtils {

    /**
     * Used by {@link ReflectionPublisher}
     *
     * @param writer_helper_class_name
     * @param dw
     * @return
     */
    public static Object narrow(String writer_helper_class_name, DataWriter dw) {

        Class<?> whclass = null;
        Method narrow_method = null;

        try {
            whclass = Class.forName(writer_helper_class_name);
            narrow_method = whclass.getMethod("narrow", Object.class);
            return narrow_method.invoke(null, dw);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Used by {@link ReflectionSubscriber}
     *
     * @param reader_helper_class_name
     * @param dr
     * @return
     */
    public static Object narrow(String reader_helper_class_name, DataReader dr) {

        Class<?> whclass = null;
        Method narrow_method = null;

        try {
            whclass = Class.forName(reader_helper_class_name);
            narrow_method = whclass.getMethod("narrow", Object.class);
            return narrow_method.invoke(null, dr);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Used by {@link ReflectionPublisher}
     *
     * @param message_writer
     * @param message
     * @return
     */
    public static long register_instance(Object message_writer, Object message) {
        Class<?> message_class = null;
        Class<?> writer_class = null;
        Method meth = null;

        try {
            message_class = message.getClass();
            writer_class = message_writer.getClass();

            meth = writer_class.getMethod("register_instance", message_class);
            Object res = meth.invoke(message_writer, message);
            if (res instanceof Long) {
                return (Long) res;
            } else {
                return 0;
            }
        } catch (Exception exc) {
            throw new RuntimeException("Error while registering instance to write.", exc);
        }
    }

    /**
     * Used by {@link ReflectionPublisher}
     *
     * @param message_writer
     * @param message
     * @param handle
     * @return
     */
    public static int write(Object message_writer, Object message, long handle) {

        Class<?> message_class = null;
        Class<?> writer_class = null;
        Method meth = null;

        try {
            message_class = message.getClass();
            writer_class = message_writer.getClass();
            meth = writer_class.getMethod("write", message_class, long.class);
            Object res = meth.invoke(message_writer, message, handle);
            if (res instanceof Integer) {
                return (Integer) res;
            } else {
                return 0;
            }
        } catch (Exception exc) {
            throw new RuntimeException("Error while writing.", exc);
        }
    }

    public static Object mapToObject(Map<String, Object> map, Class<?> klass) throws Exception {

        Object resp = klass.newInstance();

        Field[] ff = klass.getDeclaredFields();
        String fname = null;
        Class<?> fclass = null;
        Object mapobj = null;
        Object convval = null;
        for (int i = 0; i < ff.length; i++) {
            fname = ff[i].getName();
            mapobj = map.get(fname);
            fclass = ff[i].getType();

            convval = toClass(mapobj, fclass);
            if (convval != null) {
                ff[i].set(resp, convval);
            }
        }
        return resp;

    }

    private static Object toClass(Object val, Class<?> klass) {
        Class<?> valclass = val.getClass();
        if (klass.isAssignableFrom(valclass)) {
            return val;
        }

        return null;
    }

}
