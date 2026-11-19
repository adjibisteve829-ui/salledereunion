FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B package

FROM quay.io/wildfly/wildfly:40.0.1.Final-jdk17
COPY --from=build /workspace/target/salledereunion.war /opt/jboss/wildfly/standalone/deployments/salledereunion.war
# The Render Free instance has 512 MiB total; allow Hibernate more Metaspace while
# keeping headroom for WildFly's native memory.
ENV JAVA_OPTS="-Xms64m -Xmx192m -Xss512k -XX:MaxMetaspaceSize=224m -XX:ReservedCodeCacheSize=32m -XX:ActiveProcessorCount=1 -XX:+UseSerialGC"
EXPOSE 10000
CMD ["/bin/sh", "-c", "exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -Djboss.http.port=${PORT:-10000}"]
