pipeline {
    environment {
        DOCKERHUB_CREDENTIALS = credentials('e8dfdc5d-790b-4f34-9e5a-71a9af034bdb')
    }
    agent any
    stages {
        stage('Build Image') {
            steps {
                script {
                    docker.build "olihock/s4e-warehouse" + ":$BUILD_NUMBER"
                }
                sh 'docker images'
            }
        }
        stage('Push Image') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push "olihock/s4e-warehouse:$BUILD_NUMBER"'
            }
        }
    }
    post {
        always {
            sh 'docker logout'
        }
    }
}