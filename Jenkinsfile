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
        stage('Test application units') {
            steps {
                sh 'mvn -B test'
            }
        }
        stage('Build local image') {
            steps {
                docker {
                    build "olihock/warehouse" + ":$BRANCH_NAME" + "-$BUILD_NUMBER"
                }
                sh 'docker images | grep warehouse'
            }
        }
        stage('Login to docker hub') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
            }
        }
        stage('Push image to artifactory') {
            steps {
                sh 'docker push "olihock/warehouse:$BRANCH_NAME-$BUILD_NUMBER"'
            }
        }
    }
    post {
        always {
            sh 'docker logout'
        }
    }
}