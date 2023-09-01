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
                // sh 'mvn clean test'

                // junit 'target/surefire-reports/*.xml'
            }
            
        }
        stage('Add version') {
            steps {
                sh "echo \"env.APP_NEW_VER=\"$(python3 semver.py)\"\" > /var/lib/jenkins/app_version.groovy"
                load "$JENKINS_HOME/app_version.groovy"
                sh "mvn -q -ntp -B versions:set -DnewVersion=${env.APP_NEW_VER}"
            }
        }

        stage('Tag Repository') {
            steps {

                load "$JENKINS_HOME/env_variables.groovy"
                sh "git tag -a ${env.APP_NEW_VER} -m \"Version ${env.APP_NEW_VER}\""
                sh "git push origin ${env.APP_NEW_VER}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests -Dspring.profiles.active=mysql'
                sh 'docker build -t wolender-ecr .'
            }
            
        }

        stage('Push') {
            steps {

                load "$JENKINS_HOME/env_variables.groovy"

                sh "aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin ${env.REPO_URL}"
                sh "docker tag wolender-ecr:${env.APP_NEW_VER} ${env.REPO_URL}"
                sh "docker push ${env.REPO_URL}"
            }
            
        }

    }
}