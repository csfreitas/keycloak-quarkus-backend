# **Segurança de APIs quarkus com Keycloak**



## Objetivo

Exemplo de aplicação backend/api desenvolvida em quarkus protegida e integrada com serviçod e autorização OIDC Keycloak.

TODO DESENHO DESGIN

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



