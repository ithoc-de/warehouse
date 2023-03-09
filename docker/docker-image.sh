mvn -f pom.xml clean package
docker build --no-cache --tag warehouse:latest Dockerfile
docker images | grep warehouse
