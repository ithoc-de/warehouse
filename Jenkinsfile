pipeline {
    agent {
        docker {
            image 'maven:3.9.0-eclipse-temurin-11'
            args '-v /root/.m2:/root/.m2'
        }
    }
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
    }
    stages {
        stage('Package application artifact') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Build local image') {
            steps {
                build "olihock/warehouse" + ":$BRANCH_NAME" + "-$BUILD_NUMBER"
                sh 'docker images | grep warehouse'
            }
        }
    }
}