kubectl delete -n default service warehouse
kubectl delete -n default deployment warehouse
kubectl delete configmap warehouse

kubectl delete ingress keycloak
kubectl delete -n default service keycloak
kubectl delete -n default deployment keycloak
kubectl delete configmap keycloak

kubectl delete -n default service postgres
kubectl delete -n default deployment postgres
kubectl delete pvc postgres
kubectl delete pv postgres
kubectl delete configmap postgres



