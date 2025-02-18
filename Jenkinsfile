pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/mohay22/task4.git'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                sh './gradlew sonarqube'
            }
        }
    }
}