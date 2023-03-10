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
        stage('Build local image') {
            steps {
//                build "olihock/warehouse" + ":$BRANCH_NAME" + "-$BUILD_NUMBER"
                sh 'docker images | grep warehouse'
            }
        }
    }
}