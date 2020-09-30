package eu.interiot.intermw.comm.commons.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An {@link Annotation} for
 * {@link eu.interiot.intermw.commons.Message} instances
 *
 * This is used for creating instances of
 * {@link eu.interiot.intermw.commons.Message} by reflection
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {

}
