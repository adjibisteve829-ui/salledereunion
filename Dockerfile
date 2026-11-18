FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B package

FROM quay.io/wildfly/wildfly:40.0.1.Final-jdk17
COPY --from=build /workspace/target/salledereunion.war /opt/jboss/wildfly/standalone/deployments/salledereunion.war
# The Render Free instance has 512 MiB total; leave room for WildFly's native memory.
ENV JAVA_OPTS="-Djdk.serialFilter=maxbytes=10485760;maxdepth=128;maxarray=100000;maxrefs=300000 -Xms64m -Xmx256m -Xss512k -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=32m -XX:ActiveProcessorCount=1 -XX:+UseSerialGC -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true --add-exports=java.desktop/sun.awt=ALL-UNNAMED --add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.management/javax.management=ALL-UNNAMED --add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED --add-opens=java.naming/com.sun.jndi.url.ldap=ALL-UNNAMED --add-opens=java.naming/com.sun.jndi.url.ldaps=ALL-UNNAMED --add-opens=jdk.naming.dns/com.sun.jndi.dns=ALL-UNNAMED"
EXPOSE 10000
CMD ["/bin/sh", "-c", "exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -Djboss.http.port=${PORT:-10000}"]
