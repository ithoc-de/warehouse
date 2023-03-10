docker rmi -f warehouse:latest
mvn -f pom.xml clean package -DskipTests
mvn test
docker build --no-cache --tag warehouse:latest .
docker images | grep warehouse
