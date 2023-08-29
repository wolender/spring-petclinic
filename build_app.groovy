pipeline {
    agent any


    stages {
        stage('Pull') {
            steps {
                git branch: 'main', url:'https://github.com/wolender/spring-petclinic.git'
            }
        }

        stage('Static Code Analysis') {
            steps {
                sh 'echo analize'
            }
            
        }

        stage('Test') {
            steps {
                sh 'echo "running tests"'
                sh 'mvn clean test'

                junit 'target/surefire-reports/*.xml'
            }
            
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
                sh 'docker build -t wolender-ecr .'
            }
            
        }

        stage('Push') {
            steps {

                load "$JENKINS_HOME/env_variables.groovy"

                sh "aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin ${env.REPO_URL}"
                sh "docker tag wolender-ecr:latest ${env.REPO_URL}"
                sh "docker push ${env.REPO_URL}"
            }
            
        }

    }
}