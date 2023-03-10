pipeline {
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
    }
    agent {
        docker {
            image 'maven:3.9.0-eclipse-temurin-11'
            args '-v /root/.m2:/root/.m2'
        }
    }
    stages {
        stage('Package application artifact') {
            steps {
                sh 'mvn -B clean package'
            }
        }
        stage('Initialize docker environment'){
            def dockerHome = tool 'docker'
            env.PATH = "${dockerHome}/bin:${env.PATH}"
        }
        stage('Build local image') {
            steps {
                script {
                    docker.build "olihock/warehouse" + ":$BRANCH_NAME" + "-$BUILD_NUMBER"
                }
                sh 'docker images | grep warehouse'
            }
        }
        stage('Login to artifactory') {
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