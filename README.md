# **Segurança de APIs com Keycloak**



Exemplo de aplicação backend/api desenvolvida em quarkus protegida e integrada com serviçod e autorização OIDC Keycloak.

TODO DESENHO DESGIN

No nosso exemplo teremos basicamente 3 atores: o **servidor de autorização**, responsável pela emissão de tokens JWTs e representado pelo keycloak, o postman que será utilizado para simular uma aplicação front-end e nossa API REST desenvolvida em quarkus e protegida pelo servidore de autorização.



Basicamente teremos como exemplo os seguintes endpoints:

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



Download do Keycloak

https://drive.google.com/file/d/18KuC-ROYIebjIiyf-uY0tF3c7UP8fQyS/view?usp=sharing







