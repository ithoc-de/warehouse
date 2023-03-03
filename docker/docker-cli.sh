# MariaDB Database
#docker run -d --name mariadb -p 3306:3306 -e MARIADB_ROOT_PASSWORD=dev -v data/mariadb:/var/lib/mysql mariadb:10.9.5

# KeyCloak OpenID Connect Authorization Server
docker run -d --name keycloak -p 7002:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin  quay.io/keycloak/keycloak:19.0.3 start-dev
# --volume /mnt/c/Users/olive/ws/Solution4Europe/s4e-warehouse/keycloak/realm.json:/opt/keycloak/data/import/realm.json
# --import-realm