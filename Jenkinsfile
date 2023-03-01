pipeline {
  environment {
    registry = "olihock/s4e-warehouse"
    registryCredential = 'e8dfdc5d-790b-4f34-9e5a-71a9af034bdb'
  }
  agent any
  stages {
    stage('Building image') {
      steps{
        script {
          docker.build registry + ":$BUILD_NUMBER"
        }
      }
    }
  }
}