package eu.interiot.intermw.commons.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An {@link Annotation} for
 * {@link eu.interiot.intermw.commons.interfaces.MwFactory} instances
 *
 * This is used for creating instances of
 * {@link eu.interiot.intermw.commons.interfaces.MwFactory} by reflection
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MwFactory {

}
