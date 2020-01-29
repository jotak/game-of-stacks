####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode
#
# Before building the docker image run:
#
# mvn package -Pnative -Dnative-image.docker-build=true
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile.native -t quarkus/gos-gm .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/gos-gm
#
###
FROM registry.fedoraproject.org/fedora-minimal
EXPOSE 8080
WORKDIR /work/
COPY hero/target/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]