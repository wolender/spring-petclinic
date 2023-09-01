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
        script {
            load "$JENKINS_HOME/app_version.groovy"
            // Execute the Python script and capture its output
            def pythonOutput = sh(script: "python3 semver.py ${env.APP_NEW_VER}", returnStdout: true).trim()
            
            // Set the environmental variable within this script block
            env.APP_NEW_VER = pythonOutput
            
            // Save the environmental variable to a Groovy script file
            writeFile file: "$JENKINS_HOME/app_version.groovy", text: "env.APP_NEW_VER=\"${env.APP_NEW_VER}\""
            
            // Load the Groovy script to make the variable available
            load "$JENKINS_HOME/app_version.groovy"
            
            echo "Captured Version: ${env.APP_NEW_VER}"
            
            sh "mvn -q -ntp -B versions:set -DnewVersion=${env.APP_NEW_VER}"
        }
    }
}


        stage('Tag Repository') {
            steps {

                load "$JENKINS_HOME/app_version.groovy"
                sh "git tag -a ${env.APP_NEW_VER} -m \"Version ${env.APP_NEW_VER}\""
                sh "git remote set-url origin https://github.com/wolender/spring-petclinic.git"
                // sh "git push --tags"
            }
        }

        stage('Build') {
            steps {
                load "$JENKINS_HOME/app_version.groovy"
                sh 'mvn clean install -DskipTests -Dspring.profiles.active=mysql'
                sh "docker build -t wolender-ecr:${env.APP_NEW_VER} ."
            }
            
        }

        stage('Push') {
            steps {

                load "$JENKINS_HOME/env_variables.groovy"

                sh "aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin ${env.REPO_URL}"
                sh "docker tag wolender-ecr:${env.APP_NEW_VER} ${env.REPO_URL}:${env.APP_NEW_VER}"
                sh "docker push ${env.REPO_URL}:${env.APP_NEW_VER}"
            }
            
        }

    }
}