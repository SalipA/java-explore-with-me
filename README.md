# Explore With Me

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)

### About Project
Explore with me is an educational project for developers studying at [Yandex Practicum Java Developer](https://practicum.yandex.ru/java-developer/) course.
This repository contains a backend for the social network for organizing events and searching for participants.

### About Stack
Technologies were used:
+ Java
+ Spring Boot
+ Maven
+ PostgreSQL
+ Hibernate

### About Architecture
+ MSA
+ Two services: main-service and stats-service

### About Functionality

+ **Stats-service**

    + service collects statistics of event views
+ **Main-service**

    + service process the core business logic of the project and CRUD-operations with entities

### About API
![Swagger](https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white)

Learn more information about endpoints and provided JSON - objects in project documentation:
+ [main-service](ewm-main-service-spec.json)
+ [stats-service](ewm-stats-service-spec.json)


### About Entity-Relationship model
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)

![erd](explore-erd.png)

### About Run
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

Use Maven for packaging and just ```docker-compose up``` it! 🐳