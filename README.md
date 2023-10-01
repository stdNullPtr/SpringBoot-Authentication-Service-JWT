# Spring Boot Authentication Service API (JWT)

* [Introduction](#introduction)
* [Requirements](#requirements)
* [Running the application locally](#running-the-application-locally)
* [Spring configuration YAML](#spring-application-yaml)
* [Using the API](#using-the-API)
* [Docker](#docker)
* [Copyright](#copyright)
* [Author](#author)
* [Links](#links)

## Introduction

This project is a simple example of how you would implement
an authentication provider using [JSON WebTokens](https://jwt.io/), Spring Boot and Spring Security.

The database used is MySQL.

I <b>highly encourage you</b> to use [Docker](#docker) to run the project

## Requirements

- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- Strongly recommended [IntelliJ IDE](https://www.jetbrains.com/idea/download/?section=windows), it's amazing.
- [Maven 3](https://maven.apache.org) (There is a mvn wrapper included in this project)
- [MySQL 8.1](https://dev.mysql.com/downloads/mysql/)
- (Optional) [Docker](https://docs.docker.com/engine/install/)
- (Optional) [HeidiSQL](https://www.heidisql.com/download.php) A simple tool for accessing your MySQL DB
- (Optional) [Postman](https://www.postman.com/downloads/)

## Running the application locally

### 1. MySQL setup

First make sure you set up a MySQL local instance.
Then, configure your chosen username and password in application.yml:

```yaml
spring:
  datasource:
    url: "jdbc:mysql://${MYSQL_HOST:localhost}:3306/testdb?useSSL=false&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true"
    username: "root"
    password: ${MYSQL_PASSWORD:qaz88x}
```

Upon startup, the application will execute the SQL queries inside ```/resources/import.sql```
which will populate the user permissions in the database:

```sql
INSERT INTO roles(name) VALUES ('ROLE_USER');
INSERT INTO roles(name) VALUES ('ROLE_MODERATOR');
INSERT INTO roles(name) VALUES ('ROLE_ADMIN');
```

### 2. Starting the Spring Boot app

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method
in the `com.anto.authservice.Application` class from your IDE.

Alternatively you can use
the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html)
like so:

```shell
mvn spring-boot:run
```

## Spring application yaml

```yaml
spring:
  datasource:
    # MySQL connection details
    url: "jdbc:mysql://${MYSQL_HOST:localhost}:3306/testdb?useSSL=false&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true"
    username: "root"
    password: ${MYSQL_PASSWORD:qaz88x}
  jpa:
    properties:
      hibernate:
        dialect: "org.hibernate.dialect.MySQLDialect"
    hibernate:
      # Print sql requests, useful during development
      show-sql: true
      # Clear DB after each service start for easier development
      ddl-auto: "create"
app:
  jwt:
    # JWT expiration
    expirationMs: "3600000"
    # Refresh token expiration
    refreshExpirationMs: "86400000"
    # Secret for signing the JWT
    secret: "ufGJqqC94OBE8qJFigbB55Pf2mLCXUDomQKP87qaGl/Nj9b/aWOlvtJ+bBtggH9XnBHR4M7SBtGOq++XfXw0iw=="

```

## Using the API

### Endpoints

#### ```POST localhost:8080/api/auth/signup```

Used for initial registration in the database. Example payload:

```json
{
  "username": "mod",
  "email": "mod@anto.com",
  "password": "123456",
  "role": [
    "mod",
    "user"
  ]
}
```

Example response:

```json
{
  "message": "User registered successfully!"
}
```

Database changes:

![HeidiSQL-after-signup.png](img/HeidiSQL-after-signup.png)

This will create a new user with the specified username and password in the database

#### ```POST localhost:8080/api/auth/signin```

Used to authenticate through the API, receiving a JWT and a refresh token in return.

Example payload (following previous example):

```json
{
  "username": "mod",
  "password": "123456"
}
```

Example response:

```json
{
  "type": "Bearer",
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtb2QiLCJpYXQiOjE2OTU4MjY0NTEsImV4cCI6MTY5NTkxMjg1MX0.eFe8VtXxEXp7lDlMM9evXG-dx9oSarzJZto5I9d3D-t53mTsJ7iU3q6_vvi6dJ_BUnWzGm7YLaC6Hm1iQ3ZKJA",
  "refreshToken": "d85c5c12-363b-4a9c-8ac8-98823716ec1e",
  "id": 1,
  "username": "mod",
  "email": "mod@anto.com",
  "roles": [
    "ROLE_USER",
    "ROLE_MODERATOR"
  ]
}
```

The token provided in the *token* field can now be used alongside future API calls to other endpoints.

#### ```POST localhost:8080/api/auth/refreshtoken```

Used to request a new access token.

Example payload (The refresh token received during [sign in](#post-localhost8080apiauthsignin))

```json
{
  "refreshToken": "d85c5c12-363b-4a9c-8ac8-98823716ec1e"
}
```

Example response:

```json
{
  "type": "Bearer",
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtb2QiLCJpYXQiOjE2OTYxODMyNzEsImV4cCI6MTY5NjE4Njg3MX0.6HXkZzkDNoVi7ivl7wR2ok6PUFDKWXNeyZyCAkksawAHSLlVytSAnYLOlSzWO-irbMapWNDu3X-NJWTqk3-ixg",
  "refreshToken": "d85c5c12-363b-4a9c-8ac8-98823716ec1e"
}
```

#### ```Multiple endpoints: localhost:8080/api/test/*```

Endpoints purely for testing purposes that the resource is protected and not accessible without a valid JWT

#### ```GET localhost:8080/api/test/mod```

This endpoint is only accessible if the JWT provided has rights to access *mod* role protected endpoints.

<b>Required header</b>: ```Authorization: Bearer <token>```

How this looks like in Postman: ![Request-mods-only.png](img/Request-mods-only.png)

## Docker

Install [Docker](https://docs.docker.com/engine/install/)

The docker configs are in [Dockerfile](service/Dockerfile) and [compose](compose.yaml)

Run ```docker compose up``` to start a MySQL instance and the service with a single command!

## Copyright

License: [BSD-4-Clause](LICENSE)

## Author

[Antonio - LinkedIn](https://www.linkedin.com/in/antonio-lyubchev/)

## Links

*Special thanks to [Bezkoder](https://www.bezkoder.com/)*
