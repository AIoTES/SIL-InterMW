package eu.interiot.intermw.api.rest.resource;

import eu.interiot.intermw.commons.Context;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.services.registry.ParliamentRegistryExperimental;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

@Provider
@PreMatching
public class AuthorizationFilterMock implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilterMock.class);
    private static final String CLIENT_ID_HEADER = "Client-ID";
    private ParliamentRegistryExperimental parliamentRegistry;

    public AuthorizationFilterMock() throws MiddlewareException {
        logger.debug("AuthorizationFilterMock is initializing...");
        parliamentRegistry = new ParliamentRegistryExperimental(Context.getConfiguration());
        logger.debug("AuthorizationFilterMock has been initialized successfully.");
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        String path = ((ContainerRequest) context).getRequestUri().getPath();
        String method = context.getMethod();
        if (path.equals("/api/swagger.json") ||
                path.equals("/api/mw2mw/clients") && method.equals("POST")) {
            return;
        }

        if (!context.getHeaders().containsKey(CLIENT_ID_HEADER)) {

            context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("The 'Client-ID' header is missing.")
                    .build());
            return;
        }

        String clientId = context.getHeaderString(CLIENT_ID_HEADER);
        boolean clientRegistered;
        try {
            clientRegistered = parliamentRegistry.isClientRegistered(clientId);
        } catch (MiddlewareException e) {
            logger.error("Failed to validate client: " + e.getMessage(), e);
            throw new IOException("Failed to validate client: " + e.getMessage());
        }
        if (!clientRegistered) {
            context.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("Client '" + clientId + "' is not registered.")
                    .build());
            return;
        }

        final SecurityContext oldSecurityContext = context.getSecurityContext();
        context.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> clientId;
            }

            @Override
            public boolean isUserInRole(String s) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return oldSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return oldSecurityContext.getAuthenticationScheme();
            }
        });

    }
}
