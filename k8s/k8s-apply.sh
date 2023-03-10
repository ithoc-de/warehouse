kubectl apply -f k8s/postgres-config.yaml
kubectl apply -f k8s/postgres-persistence.yaml
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/postgres-service.yaml

kubectl apply -f k8s/keycloak-config.yaml
kubectl apply -f k8s/keycloak-deployment.yaml
#kubectl apply -f k8s/keycloak-service.yaml
kubectl apply -f k8s/keycloak-ingress.yaml

kubectl apply -f k8s/warehouse-config.yaml
kubectl apply -f k8s/warehouse-deployment.yaml
kubectl apply -f k8s/warehouse-service.yaml
