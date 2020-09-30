/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.commons.exceptions;

/**
 * {@link MiddlewareException} subclass for Bridge API exceptions
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * @see ErrorCode
 */
public class MiddlewareException extends Exception {

    private static final long serialVersionUID = 2134144519277702319L;

    private String cause;
    private Integer code;
    private Throwable exception;

    /**
     * The constructor
     *
     * @param exception The origin {@link Exception}
     */
    public MiddlewareException(Throwable exception) {
        super(exception);
        this.exception = exception;
    }

    /**
     * The constructor
     *
     * @param cause A string describing the cause of the {@link Exception}
     */
    public MiddlewareException(String cause) {
        super(cause);
        this.cause = cause;
    }

    /**
     * The constructor
     *
     * @param format Format of the cause
     * @param args   Arguments for format
     */
    public MiddlewareException(String format, Object... args) {
        this(String.format(format, args));
    }

    /**
     * The constructor
     *
     * @param code A unique ID to identify the {@link Exception}
     * @param e    The origin {@link Exception}
     */
    public MiddlewareException(Integer code, Throwable e) {
        super(e);
        this.code = code;
    }

    /**
     * The constructor
     *
     * @param cause A string describing the cause of the {@link Exception}
     * @param e     The origin {@link Exception}
     */
    public MiddlewareException(String cause, Throwable e) {
        super(cause, e);
        this.cause = cause;
        exception = e;
    }

    /**
     * The constructor
     *
     * @param e      The origin {@link Exception}
     * @param format Format of the cause
     * @param args   Arguments for format
     */
    public MiddlewareException(Throwable e, String format, Object... args) {
        super(String.format(format, args), e);
        this.cause = String.format(format, args);
        exception = e;
    }

    /**
     * The constructor
     *
     * @param code  A unique ID to identify the {@link Exception}
     * @param cause The origin {@link Exception}
     */
    public MiddlewareException(Integer code, String cause) {
        super(cause);
        this.cause = cause;
        this.code = code;
    }

    /**
     * The constructor
     *
     * @param code  A unique ID to identify the {@link Exception}
     * @param cause A string describing the cause of the {@link Exception}
     * @param e     The origin {@link Exception}
     */
    public MiddlewareException(Integer code, String cause, Throwable e) {
        super(cause, e);
        this.cause = cause;
        this.code = code;
        exception = e;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(cause);
        sb.append("\n");
        sb.append("Error code: ").append(code);
        if (exception != null) {
            sb.append("Exception: ").append(exception);
        }
        return sb.toString();
    }
}