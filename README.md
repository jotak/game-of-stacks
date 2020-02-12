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
## For docker:
MINIKUBE=true make docker deploy

## For podman:
MINIKUBE=true make podman deploy

# Expose (port-forward)
make expose
# Then open browser on http://localhost:8081
```

It starts with all deployments scaled to 0, except the web interface. To start the demo:

```bash
# Deploys 5 heroes (native), arrows (native), villains (oj9)
make arrow-scaling-hero-native-vs-hotspot--native

# Redeploys with 5 heroes (hotspot), arrows (native), villains (oj9)
make arrow-scaling-hero-native-vs-hotspot--hotspot

# Redeploys with 4 heroes (mix hotspot/native), arrows (native), villains (oj9)
make arrow-scaling-hero-native-vs-hotspot--mixed
```

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

Next steps:
- Have a blocking i/o catapult?
- New weapon: Bow/arrow; triggered as a process with known target (sysprops/env)
- Differentiate sprites with text (e.g. graal/hotspot)

## Istio

kubectl label namespace default istio-injection=enabled

