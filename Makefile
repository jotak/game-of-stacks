VERSION := 0.0.1
# List of all services (for image building / deploying)
SERVICES ?= ui villains
# Kube's CLI (kubectl or oc)
K8S_BIN ?= $(shell which kubectl 2>/dev/null || which oc 2>/dev/null)
# OCI CLI (docker or podman)
OCI_BIN ?= $(shell which podman 2>/dev/null || which docker 2>/dev/null)
# Tag for docker images
OCI_TAG ?= dev
# Set MINIKUBE=true if you want to deploy to minikube (using registry addons)
MINIKUBE ?= false
ifeq ($(MINIKUBE),true)
OCI_DOMAIN ?= "$(shell minikube ip):5000"
# TODO: these push opts are actually for podman, no matter it's minikube or not
PUSH_OPTS ?= "--tls-verify=false"
else
OCI_DOMAIN ?= docker.io
endif

clean:
	mvn clean

build: install

install:
	mvn install -DskipTests

test:
	mvn test

docker:
	for svc in ${SERVICES} ; do \
		${OCI_BIN} build -t ${OCI_DOMAIN}/${USER}/gos-oj9-$$svc:${OCI_TAG} -f ./k8s/$$svc-openj9.dockerfile ./ ; \
		${OCI_BIN} build -t ${OCI_DOMAIN}/${USER}/gos-hotspot-$$svc:${OCI_TAG} -f ./k8s/$$svc-hotspot.dockerfile ./ ; \
	done

deploy-kafka:
	${K8S_BIN} create namespace kafka
	curl -L https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.15.0/strimzi-cluster-operator-0.15.0.yaml   | sed 's/namespace: .*/namespace: kafka/'   | ${K8S_BIN} apply -f - -n kafka
	${K8S_BIN} apply -f https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.15.0/examples/kafka/kafka-persistent-single.yaml -n kafka

deploy:
ifeq ($(MINIKUBE),true)
	for svc in ${SERVICES} ; do \
		${OCI_BIN} tag ${OCI_DOMAIN}/${USER}/gos-oj9-$$svc:${OCI_TAG} localhost:5000/${USER}/gos-oj9-$$svc:${OCI_TAG} ; \
		${OCI_BIN} tag ${OCI_DOMAIN}/${USER}/gos-hotspot-$$svc:${OCI_TAG} localhost:5000/${USER}/gos-hotspot-$$svc:${OCI_TAG} ; \
		${OCI_BIN} push ${PUSH_OPTS} ${OCI_DOMAIN}/${USER}/gos-oj9-$$svc:${OCI_TAG} ; \
		${OCI_BIN} push ${PUSH_OPTS} ${OCI_DOMAIN}/${USER}/gos-hotspot-$$svc:${OCI_TAG} ; \
	done
endif
	for svc in ${SERVICES} ; do \
		${K8S_BIN} apply -f k8s/$$svc.yml ; \
	done


expose:
	@echo "URL: http://localhost:8081/"
	${K8S_BIN} port-forward svc/ui 8081:8081

undeploy:
	${K8S_BIN} delete all -l project=gos

start-ui:
	java -jar ./ui/target/gos-ui-${VERSION}-runner.jar

start-villains:
	java -jar ./villains/target/gos-villains-${VERSION}-runner.jar

start-catapult-vertx:
	java -jar ./catapult-vertx/target/gos-catapult-vertx-${VERSION}-runner.jar

start-ned:
	export shortId="ned" && java -jar ./hero/target/gos-hero-${VERSION}-runner.jar

start-aria:
	export shortId="aria" && java -jar ./hero/target/gos-hero-${VERSION}-runner.jar

start-kafka:
	cd kafka; docker-compose up

start:
	make -j5 start-ui start-villains start-catapult-vertx start-ned start-aria
