package com.redhat.demo;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.security.Principal;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import org.eclipse.microprofile.jwt.JsonWebToken;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/hello")
public class GreetingResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/default")
    @RolesAllowed({"DEFAULT_USER","ADMIN"})
    public String helloDefaultUser() {
        OidcJwtCallerPrincipal oidcPrincipal = getOIDCPrincipal();
        String username = String.valueOf(oidcPrincipal.claim("preferred_username").orElseThrow());;
        return "Hello, Your user is the: " + username;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/admin")
    @RolesAllowed("ADMIN")
    public String helloAdminUser() {
        OidcJwtCallerPrincipal oidcPrincipal = getOIDCPrincipal();
        String username = String.valueOf(oidcPrincipal.claim("preferred_username").orElseThrow());
        return "Hello user: " + username + ", Only administrators can use that resource";
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/perfis")
    @Authenticated
    public String helloPerfisString() {
        OidcJwtCallerPrincipal oidcPrincipal = getOIDCPrincipal();
        JsonObject resource_access = (JsonObject) oidcPrincipal.claim("resource_access").get();
        String username = String.valueOf(oidcPrincipal.claim("preferred_username").orElseThrow());
        return "Hello User : " + username + ", you have the following profiles: " +  resource_access.getJsonObject("hello-app").getJsonArray("roles");
    }

    private OidcJwtCallerPrincipal getOIDCPrincipal() {
        Principal principal = securityIdentity.getPrincipal();
        OidcJwtCallerPrincipal oidcPrincipal = null;
        if(principal instanceof OidcJwtCallerPrincipal) {
            oidcPrincipal = (OidcJwtCallerPrincipal) principal;
        }
        return oidcPrincipal;
    }
}