FROM openjdk:17-oracle

EXPOSE 8761

ADD target/eureka-server-0.0.1.jar eureka-server.jar

ENTRYPOINT ["java","-jar","eureka-server.jar"]