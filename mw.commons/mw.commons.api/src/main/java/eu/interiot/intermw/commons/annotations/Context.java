package eu.interiot.intermw.commons.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An {@link Annotation} for
 * {@link eu.interiot.intermw.commons.Configuration} instances
 *
 * This is used for loading module Component classes by reflection.
 * This should facilitate future deployment implementations through OSGi, Web conaners etc.
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Context {

}
