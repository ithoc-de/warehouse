
# Deployment

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
kubectl describe pod s4e-warehouse
kubectl describe service s4e-warehouse

kubectl apply -f webblog-config.yaml
kubectl delete -n default configmap webblog-config
kubectl create configmap webblog-config --from-env-file=webblog-config.properties
kubectl apply -f webblog-secrets.yaml

kubectl create -f kubernetes-service.yaml

```

## Minikube
**Use the Docker daemon in Minikube**
```
eval $(minikube docker-env)
minikube docker-env

minikube dashboard &

minikube service --all

minikube start
minikube stop

```
