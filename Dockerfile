FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B package

FROM quay.io/wildfly/wildfly:40.0.1.Final-jdk17
COPY --from=build /workspace/target/salledereunion.war /opt/jboss/wildfly/standalone/deployments/salledereunion.war
EXPOSE 10000
CMD ["/bin/sh", "-c", "exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -Djboss.http.port=${PORT:-10000}"]
