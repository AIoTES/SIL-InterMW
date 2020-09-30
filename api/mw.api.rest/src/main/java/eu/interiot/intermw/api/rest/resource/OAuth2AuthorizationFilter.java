package eu.interiot.intermw.api.rest.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.intermw.commons.Context;
import eu.interiot.intermw.commons.DefaultConfiguration;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.services.registry.ParliamentRegistry;
import eu.interiot.intermw.services.registry.ParliamentRegistryExperimental;
import joptsimple.internal.Strings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@Provider
//@PreMatching
public class OAuth2AuthorizationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InterMwApiREST.class);
    private static final String CONFIG_FILE = "intermw.properties";
    private static final String INTROSPECT_PATH = "/oauth2/introspect";
    private static final String WSO2IS_BASE_URL_PROPERTY = "wso2is.baseUrl";
    private static final String WSO2IS_USERNAME_PROPERTY = "wso2is.username";
    private static final String WSO2IS_PASSWORD_PROPERTY = "wso2is.password";
    private static Pattern AUTH_HEADER_PATTERN = Pattern.compile("Bearer ([\\w-]+)");
    private ObjectMapper objectMapper = new ObjectMapper();
    private ParliamentRegistry parliamentRegistry;
    private String base64EncodedCredentials;
    private String introspectEndpointUrl;

    public OAuth2AuthorizationFilter() throws MiddlewareException {
        logger.debug("OAuth2AuthorizationFilter is initializing...");
        try {
            parliamentRegistry = new ParliamentRegistryExperimental(Context.getConfiguration());
            Configuration conf = new DefaultConfiguration(CONFIG_FILE);

            if (Strings.isNullOrEmpty(conf.getProperty(WSO2IS_BASE_URL_PROPERTY))) {
                throw new MiddlewareException("'%s' configuration property is not set.", WSO2IS_BASE_URL_PROPERTY);
            }
            if (Strings.isNullOrEmpty(conf.getProperty(WSO2IS_USERNAME_PROPERTY))) {
                throw new MiddlewareException("'%s' configuration property is not set.", WSO2IS_USERNAME_PROPERTY);
            }
            if (Strings.isNullOrEmpty(conf.getProperty(WSO2IS_PASSWORD_PROPERTY))) {
                throw new MiddlewareException("'%s' configuration property is not set.", WSO2IS_PASSWORD_PROPERTY);
            }

            String credentials = conf.getProperty(WSO2IS_USERNAME_PROPERTY) + ":" + conf.getProperty(WSO2IS_PASSWORD_PROPERTY);
            base64EncodedCredentials = new String(Base64.getEncoder().encode(credentials.getBytes()), StandardCharsets.UTF_8);
            introspectEndpointUrl = conf.getProperty(WSO2IS_BASE_URL_PROPERTY) + INTROSPECT_PATH;
            logger.debug("OAuth2AuthorizationFilter has been initialized successfully.");

        } catch (Exception e) {
            logger.error("OAuth2AuthorizationFilter failed to initialize: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void filter(ContainerRequestContext context) {
        try {
            filterImpl(context);

        } catch (Exception e) {
            logger.error("Failed to validate OAuth access token: " + e.getMessage(), e);
            context.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to validate OAuth access token. Please see server log for details.")
                    .build());
        }
    }

    private void filterImpl(ContainerRequestContext context) throws Exception {
        String requestPath = context.getUriInfo().getPath();
        if (requestPath.equals("swagger.json")) {
            return;
        }

        if (!context.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("The 'Authorization' header is missing.")
                    .build());
            return;
        }

        Matcher matcher = AUTH_HEADER_PATTERN.matcher(context.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (!matcher.matches()) {
            context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid 'Authorization' header.")
                    .build());
            return;
        }

        String token = matcher.group(1);
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(introspectEndpointUrl);
        HttpEntity httpEntity = new StringEntity("token=" + token,
                ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8));
        httpPost.setEntity(httpEntity);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + base64EncodedCredentials);

        HttpResponse response;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            throw new Exception("Failed to execute HTTP request to " + introspectEndpointUrl + ": " + e.getMessage());
        }
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Unexpected response code received from Identity Server: " + response.getStatusLine());
        }

        HttpEntity entity = response.getEntity();
        WSO2User wso2User;
        try {
            wso2User = objectMapper.readValue(entity.getContent(), WSO2User.class);
        } catch (IOException e) {
            throw new Exception("Failed to deserialize WSO2User: " + e.getMessage(), e);
        }

        if (!wso2User.active) {
            context.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("Invalid access token or inactive user.")
                    .build());
            return;
        }

        String clientId = getClientId(wso2User.username);
        if (requestPath.equals("mw2mw/clients") && context.getMethod().equals("POST")) {
            // skip the client registration check
        } else {
            try {
                if (!parliamentRegistry.isClientRegistered(clientId)) {
                    context.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .entity("Client is not registered within INTER-MW.")
                            .build());
                    return;
                }
            } catch (Exception e) {
                throw new Exception("Failed to query Parliament registry: " + e.getMessage(), e);
            }
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

    private String getClientId(String username) {
        return username.substring(username.indexOf('/') + 1, username.indexOf('@'));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WSO2User {
        public boolean active;
        public String username;
    }
}