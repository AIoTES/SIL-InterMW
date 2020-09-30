package eu.interiot.intermw.comm.prm.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * An {@link Annotation} for {@link eu.interiot.intermw.comm.prm.PlatformRequestManager}
 * instances
 *
 * This is used for creating instances of
 * {@link eu.interiot.intermw.comm.prm.PlatformRequestManager} by reflection
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio fuart</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PlatformRequestManager {
}
