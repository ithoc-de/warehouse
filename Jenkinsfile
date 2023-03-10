pipeline {
    agent {
        dockerfile true
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
                build "olihock/warehouse" + ":$BRANCH_NAME" + "-$BUILD_NUMBER"
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