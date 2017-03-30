#!groovy​
pipeline {
    agent any

//    agent {
//        docker {
//            image 'hyperhq/jenkins-slave-gradle'
//        }
//    }

    stages {

        stage('Prepare') {
            steps {
                git credentialsId: 'gitlab-jenkins', branch: 'master', url: 'git@gitlab.exist.com:pemc/metering.git'
            }
        }

        stage('Build') {
            steps {
                echo 'Building..'
                sh "./gradlew build -x test"
            }
        }

        stage('Test') {
            steps {
                echo 'Testing..'
                sh "./gradlew test"
            }
        }

        stage('Stage Archive') {
            steps {
                echo 'Archiving uber jar'
                archiveArtifacts artifacts: '**/target/libs/*.jar', fingerprint: true

                echo 'Archiving test results'
                junit '**/target/test-results/test/TEST-*.xml'
            }
        }

        stage('Dockerize') {
            steps {
                echo 'Building docker image'
                sh "./gradlew buildDockerImage"
            }
        }

        stage('Publish Docker image') {
            steps {
                echo 'Publishing docker image'
                sh "./gradlew -PdockerRegistryUrl=pemc.medcurial.com pushDockerImage"
            }
        }

        stage('Publish Jar Artifacts') {
            steps {
                echo 'Publishing Jar Artifacts'
            }
        }

        stage('Deploy') {
            steps {
                echo "Updating database via liquibase"
                sh "./gradlew -Penv=ss-combined dbUpdate"

                sh "./gradlew -Penv=ss-combined -PmarathonUrl=http://192.168.233.2/marathon/v2 marathonDeploy"

                archiveArtifacts artifacts: '**/deployment/marathon/*.json', fingerprint: true
            }
        }
    }
}
