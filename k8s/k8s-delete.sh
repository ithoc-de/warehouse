kubectl delete -n default service warehouse
kubectl delete -n default deployment warehouse
kubectl delete configmap warehouse

#kubectl delete ingress keycloak
#kubectl delete -n default service keycloak
#kubectl delete -n default deployment keycloak
#kubectl delete configmap keycloak

#kubectl delete -n default service db-warehouse
#kubectl delete -n default deployment db-warehouse
#kubectl delete pvc db-warehouse
#kubectl delete pv db-warehouse
#kubectl delete configmap db-warehouse

#kubectl delete -n default service db-keycloak
#kubectl delete -n default deployment db-keycloak
#kubectl delete pvc db-keycloak
#kubectl delete pv db-keycloak
#kubectl delete configmap db-keycloak
