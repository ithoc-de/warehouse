pipeline {
    environment {
        DOCKERHUB_CREDENTIALS = credentials('e8dfdc5d-790b-4f34-9e5a-71a9af034bdb')
    }
    agent any
    stages {
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