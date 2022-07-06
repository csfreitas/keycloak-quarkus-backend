# **Segurança de APIs quarkus com Keycloak**



## Objetivo

Exemplo de aplicação backend/api desenvolvida em quarkus protegida e integrada com serviço de autorização OIDC Keycloak.

![Exemplo API Keycloak](assets/Exemplo API Keycloak.png)

No nosso exemplo teremos basicamente 3 atores: o **servidor de autorização**, responsável pela emissão de tokens JWTs e representado pelo keycloak, o postman que será utilizado para simular uma aplicação front-end e nossa API REST desenvolvida em quarkus e protegida pelo servidore de autorização.



Como exemplo serão os seguintes endpoints:

- **/hello** - serviço de acesso público não autenticado 
- **/hello/default** - serviço autorizado para usuário padrão e administrador
- **/hello/admin** - serviço autorizado para usuário administrador
- **/hello/perfis** - serviço autorizado para qualquer usuário autenticado



Iremos utilizar uma configuração padrão então para os usuários de perfis de acesso baseado em *Roles*(RBAC  - Role Based Access Control), para autorizar os usuários a consumirem as APIs. Teremos então os seguintes usuários com seus repectivos perfis.

| USERNAME     | PASSWORD | CLIENT    | ROLES               |
| ------------ | -------- | --------- | ------------------- |
| demo         | 1234     | hello-app | NDA                 |
| demo-default | 1234     | hello-app | DEFAULT_USER        |
| demo-admin   | 1234     | hello-app | DEFAULT_USER, ADMIN |

## Keycloak Authorization Server

Para facilitar a execução e teste do exemplo estamos disponibilizando um pacote com banco de dados local(h2) já configurado com os usuários, *clients* e *roles* configuradas:

- https://drive.google.com/file/d/18KuC-ROYIebjIiyf-uY0tF3c7UP8fQyS/view?usp=sharing

O keycloak pode ser baixado também no site: https://www.keycloak.org/downloads, com demais opções de execução para desenvolvimento.

Deve ser realizado a configuração dos usuários, clients e roles conforme a tabela de usuários apresentada na seção anterior.

Para subir o keycloak local:

```shell
cd $KEYCLOAK_HOME/bin
sh standalone.sh 
```

Para executar em portas diferentes, no caso de um pacote ***wildfly***:

```shell
cd $KEYCLOAK_HOME/bin
sh standalone.sh -Djboss.socket.binding.port-offset=100
```

Para outros tipos de instalação olhar a documentação: https://www.keycloak.org/guides

## API hello-app

### Configuração da aplicação

Extensão quarkus utilizada para OIDC:

```xml
<!-- OIDC extensions -->
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-oidc</artifactId>
</dependency>
```

Extensão adicionada após criação do projeto quarkus com quarkus CLI:

```shell
quarkus create app com.redhat.demo:rest-api-keycloak:1.0
```

Para mais informações sobre a criação de projetos quarkus: https://quarkus.io/guides/getting-started

Depois ajustamos a versão de propriedades para uma versão ***suportada pela Red Hat***, até a redação desta documentação:

```xml
<properties>
  <!-- Omitted properties -->
	<quarkus.platform.group-id>com.redhat.quarkus.platform</quarkus.platform.group-id>
  <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
  <quarkus.platform.version>2.7.5.Final-redhat-00011</quarkus.platform.version>
  <!-- <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id> -->
  <!-- <quarkus.platform.version>2.10.1.Final</quarkus.platform.version> -->
</properties>
```

Para mais informações sobre a versão suportada: https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus/2.7

Configuração OIDC no *src/main/resource/* ***application.properties***

```properties
# OIDC Configuration
quarkus.oidc.auth-server-url=http://localhost:8180/auth/realms/demo
quarkus.oidc.client-id=hello-app
quarkus.oidc.credentials.secret=9f7Ah9G3VEMMakeXG8sDNolJdp2wKWoD
quarkus.oidc.tls.verification=none

```

Configuração dos ***resources***

```java
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
        return "Olá, seu usuário é: " + username;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/admin")
    @RolesAllowed("ADMIN")
    public String helloAdminUser() {
        OidcJwtCallerPrincipal oidcPrincipal = getOIDCPrincipal();
        String username = String.valueOf(oidcPrincipal.claim("preferred_username").orElseThrow());
        return "Olá usuário: " + username + ", Somente ADMINISTRADORES podem utilizar este recurso)";
    }


		@GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/perfis")
    @Authenticated
    public String helloPerfisString() {
        OidcJwtCallerPrincipal oidcPrincipal = getOIDCPrincipal();
        String username = String.valueOf(oidcPrincipal.claim("preferred_username").orElseThrow());
        JsonObject resource_access = (JsonObject) oidcPrincipal.claim("resource_access").get();
        Optional<JsonObject> ofNullableResourceAccess = Optional.ofNullable(resource_access); 
        String perfis = null;
        if(ofNullableResourceAccess.isPresent()) {
            Optional<JsonObject> ofNullableHelloApp = Optional.ofNullable(ofNullableResourceAccess.get().getJsonObject("hello-app"));
            if(ofNullableHelloApp.isPresent()) {
                perfis += ofNullableHelloApp.get().getJsonArray("roles");
            }
        }
        String response = null;
        if(perfis != null){
            response = "Olá usuário: " + username + ", você possui os seguintes perfis:' " +  perfis;
        } else {
            response = "Olá usuário: " + username + ", você não possui nenhum perfil configurado para aplicação: hello-app";
        }
        return response;
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
```

Em relação as configurações de autenticação e autorização:

- Para permitir qualquer acesso a um *resource*, utililizar a *annotation* **@PermitAll**
- Para permitir qualquer acesso autenticado de forma explícita, utilizar a *annotation* **@Authenticated**
- Para fazer uso de RBAC nos acessos aos resources, utilizar a *annotation* **@RolesAllowed({})**, com a lista de perfis.

### Token Claims E SecurityIdentity Roles

SecurityIdentity roles podem ser mapeados pela verificação do access token JWT da seguinte forma:

- Se a propriedade`quarkus.oidc.roles.role-claim-path` é definida e as claims de array ou string correspondentes são encontradas, então as funções são extraídas dessas claims.
- Se a claim `groups `estiver disponível então seu valor é usado.
- Se a claim`realm_access/roles` ou `resource_access/${client_id}/roles` (onde` ${client_id}` é o valor da propriedade `quarkus.oidc.client-id`) estiver disponível então seu valor é utilizado. Essas claims são é justamente as que o Keycloak emite em seus tokens.

Além disso, um **SecurityIdentityAugmentor** personalizado também pode ser usado para adicionar as funções conforme documentado [aqui](https://quarkus.io/version/2.7/guides/security#security-identity-customization).

### Execução da aplicação

Para executar com maven embarcado:

```shell
./mvnw quarkus:dev
```

### Testes

Para testar o funcionamento das APIs estamos disponibilizando essa Collection do Postman com as chamadas as resources configuradas para execução *localhost*.

 [Quarkus Demo.postman_collection.json](./Quarkus Demo.postman_collection.json) 

#### Obtendo token

Depois que importar a collection para o postman, basta selecionar o resource que deseja testar e selecionar a aba ***authorization***:

![Screen Shot 2022-07-06 at 15.14.18](assets/Screen Shot 2022-07-06 at 15.14.18.png)

Selecionar o tipo de authorization para ***OAuth 2.0***, e preencher as informações, se necessário:

**Grant Type**: Authorization Code

**Auth URL:** http://localhost:8180/auth/realms/demo/protocol/openid-connect/auth

**Access Token URL:** http://localhost:8180/auth/realms/demo/protocol/openid-connect/token

**Client-id:** postman

**Secret:** 9eH0lCx00emeZoaOcuxRMPgEED55Ers9

**Scope:** openid

> Alguns dos dados a cima podem mudar conforme a configuração da Realm a qual você estiver utilizando.



Selecionar o botão `Get New Access Token` e iniciar o fluxo padrão de atenticação.

<img src="assets/image-20220706150227885.png" alt="login" width="800"/>

Informar o usuário e senha:

<img src="assets/image-20220706150502531.png" alt="login" width="800"/>

Selecionar o botão `Use Token `após autenticação:

![image-20220706150620983](assets/image-20220706150620983.png)



Disparar uma chamada ao recurso desejado no botão `Send`:

![image-20220706150730010](assets/image-20220706150730010.png)

Observer o retorno do serviço:

![image-20220706150821989](assets/image-20220706150821989.png)



No nosso exemplo, o usuário ***demo*** não está autorizado a acessar o resource ***/hello/default***.
