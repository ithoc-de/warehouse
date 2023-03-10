kubectl apply -f k8s/db-keycloak-config.yaml
kubectl apply -f k8s/db-keycloak-persistence.yaml
kubectl apply -f k8s/db-keycloak-deployment.yaml
kubectl apply -f k8s/db-keycloak-service.yaml

kubectl apply -f k8s/db-warehouse-config.yaml
kubectl apply -f k8s/db-warehouse-persistence.yaml
kubectl apply -f k8s/db-warehouse-deployment.yaml
kubectl apply -f k8s/db-warehouse-service.yaml

kubectl apply -f k8s/keycloak-config.yaml
kubectl apply -f k8s/keycloak-deployment.yaml
kubectl apply -f k8s/keycloak-service.yaml
kubectl apply -f k8s/keycloak-ingress.yaml

kubectl apply -f k8s/warehouse-config.yaml
kubectl apply -f k8s/warehouse-deployment.yaml
kubectl apply -f k8s/warehouse-service.yaml
