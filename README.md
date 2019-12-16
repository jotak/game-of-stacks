# game-of-stacks
This simulation game will show the power of different stacks (Quarkus, Vert.x, ..) depending on the business logic and show the results using Istio and Kiali. This will be presented as fun simulation UI showing the attack of Winterfell by a group of microservices :-) 


## Running on Minikube

Assuming minikube is up and running

```bash
# Deploy Strimzi/Kafka in namespace "kafka"
make deploy-kafka

# Build GoS
make clean build

# Build docker images & deploy
MINIKUBE=true make docker deploy

# Expose (port-forward)
make expose
# Then open browser on http://localhost:8081
```

At the moment, only UI and Villains are deployed. See comments in Makefile for more options.

## TODO

- Docker builds: more layered build (no fat jar)
- Maybe/probably we don't want to build hotspot + openj9 images for all as it's done now
- sed image tags or such within deployment yamls
- Improve kafka deployment (would be better without shell scripts & curl)
- Fix hardcoded broker url