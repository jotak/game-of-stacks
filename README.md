# game-of-stacks
This simulation game will show the power of different stacks (Quarkus, Vert.x, ..) depending on the business logic and show the results using Istio and Kiali. This will be presented as fun simulation UI showing the attack of Winterfell by a group of microservices :-) 

## Running locally

### Kafka

- With docker-compose:

```bash
cd kafka && docker-compose up
```

- From local installation (example)

```bash
cd ~/apps/kafka_2.12-2.3.0/
bin/zookeeper-server-start.sh config/zookeeper.properties
# Open new terminal
bin/kafka-server-start.sh config/server.properties 
```

### GoS services

```bash
make clean install start
```

Open http://localhost:8081

## Running on Minikube

Assuming minikube is up and running

```bash
# Deploy Strimzi/Kafka in namespace "kafka"
make deploy-kafka

# Build GoS
make clean build build-native

# Build docker images & deploy
MINIKUBE=true make docker push deploy

# Expose (port-forward)
make expose
# Then open browser on http://localhost:8081
```

At the moment, only UI and Villains are deployed. See comments in Makefile for more options.

## Build native image

Configure GraalVM for Quarkus:
https://quarkus.io/guides/building-native-image#configuring-graalvm

Then from a Quarkus maven project (hero, catapult-quarkus, ...)
```
$ mvn clean package -Pnative
$ docker build -f src/main/docker/Dockerfile.native -t hero-native .     
```


## TODO

- Docker builds: more layered build (no fat jar)
- Improve kafka deployment (would be better without shell scripts & curl)
- Arrow
- Bow need to be "taken" when a hero uses it?

Next steps:
- Have a blocking i/o catapult?
- New weapon: Bow/arrow; triggered as a process with known target (sysprops/env)
- Differentiate sprites with text (e.g. graal/hotspot)

## Istio

kubectl label namespace default istio-injection=enabled

