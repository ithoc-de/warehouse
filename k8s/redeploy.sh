# Postgres




kubectl delete -n default service warehouse
kubectl delete -n default deployment warehouse
docker rmi --force warehouse:latest
mvn clean package
docker build --no-cache --tag warehouse:latest .
docker images | grep warehouse

kubectl apply -f k8s/config.yaml
kubectl apply -f k8s/deployment.yaml

kubectl create -f k8s/service.yaml
minikube service warehouse-service

kubectl apply -f k8s/ingress.yaml
