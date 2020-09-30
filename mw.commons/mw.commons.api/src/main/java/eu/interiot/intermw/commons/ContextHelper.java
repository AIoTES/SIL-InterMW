package eu.interiot.intermw.commons;

import eu.interiot.intermw.commons.exceptions.ContextException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Main entry point to get the {@link Configuration} instances
 * <p>
 * It uses reflection to automatically search for classes annotated with
 * {@link eu.interiot.intermw.commons.annotations.Configuration} annotations
 * <p>
 * The {@link ContextHelper} class provides helper funcions and
 * {@link Configuration}
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
public class ContextHelper {

    private final static Logger log = LoggerFactory.getLogger(ContextHelper.class);

    // FIXME improve the DEFAULT_PACKAGE definition in order to find alternative
    // configurations for Configuration and Middleware implementations
    private final static String DEFAULT_PACKAGE = "eu";
    private final static String CURRENTDIR = ".";

    private Configuration configuration;
    private final Reflections defaultReflections;
    private final String propertiesFileName;

    public ContextHelper(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
        this.defaultReflections = createReflections();
    }

    /**
     * Looks for a *.properties file in the classpath or in the current jar
     * directory and creates a new {@link Configuration} instance
     *
     * @return A {@link Configuration} instance
     * @throws MiddlewareException Thrown when no Class annotated with
     *                             {@link eu.interiot.intermw.commons.annotations.Configuration}
     *                             is found
     */
    public Configuration getConfiguration() throws MiddlewareException {
        if (configuration == null) {
            configuration = getConfiguration(propertiesFileName);
        }
        return configuration;
    }

    /**
     * Creates a new {@link Configuration} instance given a <propertiesFileName>
     *
     * @param propertiesFileName The properties file name
     * @return A new {@link Configuration} instance
     * @throws MiddlewareException Thrown when no Class annotated with
     *                             {@link eu.interiot.intermw.commons.annotations.Configuration}
     *                             is found
     * @see #getConfiguration()
     */
    public Configuration getConfiguration(String propertiesFileName) throws MiddlewareException {
        Class<?> aClass = getFirstClassAnnotatedWith(eu.interiot.intermw.commons.annotations.Configuration.class,
                defaultReflections);

        log.debug("Creating a new instance of {} passing the properties file name {}...", aClass, propertiesFileName);
        Configuration configuration = null;
        try {
            configuration = (Configuration) aClass.getConstructor(String.class).newInstance(propertiesFileName);

        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new ContextException(String.format("Cannot create an instance of the class %s: %s",
                    aClass.getCanonicalName(), targetException.getMessage()), targetException);

        } catch (Exception e) {
            throw new ContextException(String.format("Cannot create an instance of the class %s: %s",
                    aClass.getCanonicalName(), e.getMessage()), e);
        }

        return configuration;
    }

    /**
     * Looks for the first class with the {@link Annotation} class passed as a
     * parameter
     *
     * @param componentAnnotation The {@link Annotation} instance to look for
     * @return A Class annotated with the componentAnnotation parameter
     * @throws MiddlewareException When something fails
     */
    @SuppressWarnings("rawtypes")
    public Class<?> getFirstClassAnnotatedWith(Class componentAnnotation) throws MiddlewareException {

        log.debug("Getting class annotated with " + componentAnnotation);
        List<String> packagesNames = getConfiguration().getScanPackages();
        log.debug("Packages names discovered: " + packagesNames);
        Class<?> componentClass = null;

        for (String packageName : packagesNames) {

            Reflections reflections = new Reflections(packageName);

            componentClass = getFirstClassAnnotatedWith(componentAnnotation, reflections);

            if (componentClass != null)
                break;
        }

        return componentClass;
    }

    /**
     * @param annotationClass The annotationClass
     * @param reflections     The {@link Reflections} instance
     * @return A Class annotated with the annotationClass parameter
     * @throws MiddlewareException When something fails
     * @see ContextHelper#getFirstClassAnnotatedWith(Class) but you can pass a
     * {@link Reflections} instance as a parameter
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class<?> getFirstClassAnnotatedWith(Class annotationClass, Reflections reflections)
            throws MiddlewareException {
        log.debug("Searching for classes annotated with {}...", annotationClass.toString());
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotationClass);
        if (classes == null || classes.isEmpty()) {
            throw new MiddlewareException("Cannot find any classes annotated with " + annotationClass.toString());
        }

        Iterator<Class<?>> iterator = classes.iterator();
        Class<?> firstClass = null;
        while (iterator.hasNext()) {
            // we took just the first one
            if (firstClass == null) {
                firstClass = iterator.next();
                break;
            }

        }
        log.debug("Annotated class found: {}", firstClass);

        return firstClass;
    }

    public List<Class<?>> getTypesAnnotatedWith(List<String> packagesNames, Class<? extends Annotation> klazz) {
        List<Class<?>> result = new ArrayList<Class<?>>();

        for (String packageName : packagesNames) {
            Reflections reflections = new Reflections(packageName);

            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(klazz);

            Iterator<Class<?>> it = classes.iterator();

            Class<?> _class;
            while (it.hasNext()) {
                _class = it.next();
                result.add(_class);
            }
        }

        return result;
    }

    public Class<?> getDefaultAnnotatedClass(List<String> packagesNames, Class<? extends Annotation> klazz)
            throws MiddlewareException {
        for (String packageName : packagesNames) {
            Reflections reflections = new Reflections(packageName);

            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(klazz);

            Iterator<Class<?>> it = classes.iterator();

            Class<?> _class;
            Class<?> firstClass = null;
            Class<?> defaultClass = null;
            while (it.hasNext()) {
                _class = it.next();

                if (firstClass == null) {
                    firstClass = _class;
                }

                try {
                    if (defaultClass == null
                            && _class.getAnnotation(klazz).getClass().getMethod("isDefault", new Class[]{})
                            .invoke(_class.getAnnotation(klazz), new Object[]{}).equals(true)) {
                        defaultClass = _class;
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
                    log.warn("ContextHelper", e);
                }
            }

            if (defaultClass != null) {
                _class = defaultClass;
            } else {
                _class = firstClass;
            }

            if (_class != null) {
                return _class;
            }
        }

        return null;
    }

    /**
     * Getter method for a {@link Reflections} instance
     *
     * @return The {@link Reflections} instance
     */
    public Reflections getReflections() {
        return this.defaultReflections;
    }

    private Reflections createReflections(String packageName) {
        return new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(new ResourcesScanner(), new SubTypesScanner(), new TypeAnnotationsScanner()));
    }

    private Reflections createReflections() {
        return createReflections(DEFAULT_PACKAGE);
    }
}
