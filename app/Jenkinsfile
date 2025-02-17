pipeline {

    agent any

    tools {
        maven "maven3"
        jdk "OracleJDK8"
    }

    environment {
        NEXUS_USER = 'admin'
        NEXUS_PASSWORD ='admin'
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

    stage {
        stage('Build'){
            steps{
                sh 'mvn -s settings.xml -DskipTests install'
            }
            post {
                success {
                    echo "Now Archiving."
                    archiveArtifactsv artifacts: '**/*.jar'
                }
            }
        }

            stage ('Test'){
                steps {
                    sh 'mvn -s settings.xml test'
                }
            }

            stage ('checkstyle Analysis'){
                steps {
                    sh 'mvn - settings.xml checkstyle:checkstyle'
                }
            }
    }
}
































            stage("Upload Artifact"){
                steps{
                        nexusArtifactUploader(
                            nexusVersion: 'nexus3',
                            protocol: 'http',
                            nexusUrl: "${NEXUS_ID}:${NEXUS_PORT}",
                            groupId: 'MobileGroup',
                            version: "${env.BUILD_ID}-${env.BUILD_TIMESTAMP}",
                            repository: "${tam205}",
                            credentialsId: "${NEXUS LOGIN}",
                            artifacts: [
                                [artifactId: 'STUDENTCOLLABORATIONAPP-MAIN',
                                classifier: '',
                                file: 'target/StudentCollaborationApp.jar',
                                type: 'jar']
                            ]
                        )
                }
            }