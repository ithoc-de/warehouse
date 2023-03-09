
# Deployment

## Minikube
**Use the Docker daemon in Minikube**
```
eval $(minikube docker-env)
minikube docker-env

minikube start
minikube dashboard &

#minikube service --all

#minikube stop
```

## Scripting
```
docker/docker-pull.sh
docker/docker-image.sh
k8s/k8s-apply.sh
k8s/k8s-delete.sh
```

## PostgreSQL Client
```
sudo apt install postgresql-client-12
export POSTGRES_PASSWORD
kubectl port-forward --namespace default svc/postgres 5432:5432
```
```
psql --host 127.0.0.1 -U postgres -d postgres -p 5432
\list
\create database keycloak
\connect warehouse
\dt
```


## Docker
```
docker build --tag webblog-service:0.0.1-SNAPSHOT .
docker build --no-cache --tag webblog-service:0.0.1-SNAPSHOT .

docker build --build-arg DOCKER_PROXY='' --tag webblog-service:0.0.1-SNAPSHOT .
docker build --build-arg DOCKER_PROXY='docker-proxy.mecom.de/' --tag webblog-service:0.0.1-SNAPSHOT .


docker images | grep webblog-service

docker run -p 8080:8080 --name webblog-service --env SPRING_PROFILES_ACTIVE='default' --env DB_USERNAME='username' --env DB_PASSWORD='password' -d webblog-service:0.0.1-SNAPSHOT

docker logs webblog-service

docker stop webblog-service
docker rm webblog-service
```

## Kubernetes
```
kubectl create deployment s4e-warehouse --image=olihock/s4e-warehouse:latest
kubectl delete -n default deployment s4e-warehouse

kubectl apply -f kubernetes-deployment.yaml
kubectl rollout restart deployment s4e-warehouse

kubectl create -f kubernetes.yaml
kubectl get pods
kubectl describe pod warehouse
kubectl describe service warehouse

kubectl apply -f webblog-config.yaml
kubectl delete -n default configmap webblog-config
kubectl create configmap webblog-config --from-env-file=webblog-config.properties
kubectl apply -f webblog-secrets.yaml

kubectl create -f kubernetes-service.yaml

```

