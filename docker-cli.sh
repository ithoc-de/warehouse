docker run -d --name mariadb -p 3306:3306 -e MARIADB_ROOT_PASSWORD=dev -v data:/var/lib/mysql mariadb:10.9.5
