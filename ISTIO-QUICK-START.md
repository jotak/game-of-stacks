## Running on minikube with low resources

Assuming minikube is up and running, and you have cloned this repo.

```bash
# Deploy Strimzi/Kafka in namespace 'kafka':
make deploy-kafka

# If you don't have already Istio deployed, to download and deploy Istio, run:
make deploy-istio

# Alternatively you can also deploy Istio without downloading (just set your Istio local installation path as in the example below):
ISTIO_PATH=~/istio-1.5.0 make deploy-istio

# Enable Istio on namespaces 'default' and 'kafka':
make enable-istio

# Start:
QUAY=true make simu-low-resources
```

For non-minikube / remote clusters, the same commands should work as well.

For managed Istio (e.g. Red Hat Service Mesh), ignore the Istio deploy/enable commands, and make sure auto-injection is enabled for namespaces 'default' and 'kafka'.
