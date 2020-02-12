# game-of-stacks
Did you know that you can do native with Java? And Quarkus makes it so easy to flip! Our presentation will compare different way of running a Quarkus Java service (GraalVM, Hotspot) using the monitoring power of Kiali. This will be presented as fun simulation game showing the attack of Winterfell by a group of scary microservices. We’ll try to keep trolls out of the battle. :-)

When you ask someone about what they think about Java, you often get answers like:
“Java use too much memory”
“With Java the startup time is so slow”
“Why would we need a JVM now that we do containers”

Well with this demo, you are going to change your mind and see that now with Java you have the best of both world (native/non native) and you can just flip from one to the other in the blink of an eye (just close your eyes at native build time:). 

Who is going to take the Iron Thrones?
A little hint.. Long live to Java!

## Running locally

### Kafka

- With docker-compose:

```bash
make start-kafka
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
make clean install start-web
# then Open http://localhost:8081
$ make start
```


## Running on Minikube

Assuming minikube is up and running

```bash
# Deploy Strimzi/Kafka in namespace "kafka"
make deploy-kafka

# Build GoS
make clean build build-native

# Build docker images & deploy
## For docker:
make docker deploy

## For podman:
make podman deploy

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


## TODO

- Docker builds: more layered build (no fat jar)
- Improve kafka deployment (would be better without shell scripts & curl)

Next steps:
- Have a blocking i/o catapult?
- New weapon: Bow/arrow; triggered as a process with known target (sysprops/env)
- Differentiate sprites with text (e.g. graal/hotspot)

## Istio

kubectl label namespace default istio-injection=enabled

