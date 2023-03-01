def DOCKER_IMAGE = "olihock/s4e-warehouse" + ":$BUILD_NUMBER"

pipeline {
    environment {
        DOCKERHUB_CREDENTIALS = credentials('e8dfdc5d-790b-4f34-9e5a-71a9af034bdb')
    }
    agent any
    stages {
        stage('Build Image') {
            steps {
                script {
                    docker.build ${DOCKER_IMAGE}
                }
                sh 'docker images'
            }
        }
        stage('Push Image') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push ${DOCKERHUB_IMAGE}'
            }
        }
    }
    post {
        always {
            sh 'docker logout'
        }
    }
}