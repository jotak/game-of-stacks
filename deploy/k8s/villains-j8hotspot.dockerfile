FROM fabric8/java-alpine-openjdk8-jre

EXPOSE 8081 9090

# Copy dependencies
# COPY villains/target/dependency/* /deployment/libs/

# Copy classes
# COPY villains/target/classes /deployment/classes

# Temp: fat jar
COPY services/villains/target/gos-villains-0.0.1-runner.jar /deployment/

ENV JAVA_APP_DIR=/deployment
ENV JAVA_LIB_DIR=/deployment
ENV JAVA_CLASSPATH=${JAVA_LIB_DIR}/gos-villains-0.0.1-runner.jar
ENV JAVA_MAIN_CLASS="demo.gos.villains.VillainsVerticle"
RUN chgrp -R 0 ${JAVA_APP_DIR} && chmod -R g+rwX ${JAVA_APP_DIR}
