pipeline {
    agent any

    tools {
        maven 'maven3'  // Ensure this matches the name configured in Jenkins
        jdk 'OracleJDK21'  // Change to match your actual JDK installation
    }

    environment {
        NEXUS_USER = 'admin'
        NEXUS_PASSWORD = 'admin'
        SNAP_REPO = 'StudentCollaborationApp-snapshot'
        RELEASE_REPO = 'StudentCollaborationApp-repo'
        CENTRAL_REPO = 'StudentCollaborationApp-central-repo'
        NEXUS_GRP_REPO = 'StudentCollaborationApp-grp-repo'
        NEXUS_IP = '192.168.33.20'
        NEXUS_PORT = '8081'
        NEXUS_LOGIN = "nexuslogin"
        SONARSERVER = 'sonarserver'
        SONARSCANNER = 'sonarscanner'
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn -s settings.xml -DskipTests install'
            }
            post {
                success {
                    echo "Now Archiving."
                    archiveArtifacts artifacts: '**/*.jar'
                }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -s settings.xml test'
            }
        }

        stage('Checkstyle Analysis') {
            steps {
                sh 'mvn -s settings.xml checkstyle:checkstyle'
            }
        }

        stage("Upload Artifact") {
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: "${NEXUS_IP}:${NEXUS_PORT}",
                    groupId: 'MobileGroup',
                    version: "${env.BUILD_ID}-${env.BUILD_TIMESTAMP}",
                    repository: "${RELEASE_REPO}",
                    credentialsId: "${NEXUS_LOGIN}",
                    artifacts: [
                        [artifactId: 'STUDENTCOLLABORATIONAPP-MAIN',
                        classifier: '',
                        file: 'target/StudentCollaborationApp.jar',
                        type: 'jar']
                    ]
                )
            }
        }
    }
}
