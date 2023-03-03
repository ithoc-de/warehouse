kubectl delete -n default service webblog-service
kubectl delete -n default deployment webblog-service
docker rmi --force webblog-service:0.0.1-SNAPSHOT
mvn clean package
docker build --no-cache --tag webblog-service:0.0.1-SNAPSHOT .
docker images | grep webblog-service
kubectl apply -f kubernetes-deployment.yaml
kubectl create -f kubernetes-service.yaml
minikube service s4e-warehouse-app
