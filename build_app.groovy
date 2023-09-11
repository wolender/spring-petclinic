pipeline {
    agent {

        label 'ec2-fleet'

        }


    stages {
        stage('Pull') {
            steps {
                git branch: 'main', url:'https://github.com/wolender/spring-petclinic.git'
            }
        }

        stage('Static Code Analysis') {
            steps {
                withSonarQubeEnv(installationName: 'SQ_SERVER') {
                    sh "mvn clean verify sonar:sonar -Dsonar.projectKey=petclinic -Dsonar.projectName='petclinic'"
                }
            }
            
        }

        stage('Test') {
            steps {
                sh 'echo "running tests"'
                sh 'mvn clean test'

                junit 'target/surefire-reports/*.xml'
            }
            
        }
        stage ('Copy Variables'){
            steps{
                copyArtifacts(
                    projectName: 'Provision Resources', // Name of the first pipeline
                    filter: '*/terraform/env_variables.groovy', // Path to the artifact in the first pipeline
                    target: "env_variables.groovy" 
                )
            }
        }
        stage('Add version') {
            steps {
                script {
                    load "app_version.groovy"
                    def pythonOutput = sh(script: "python3 semver.py ${env.APP_NEW_VER}", returnStdout: true).trim()
                    env.APP_NEW_VER = pythonOutput

                    writeFile file: "app_version.groovy", text: "env.APP_NEW_VER=\"${env.APP_NEW_VER}\""
                    
                    load "app_version.groovy"
                    
                    echo "Captured Version: ${env.APP_NEW_VER}"
                    
                    sh "mvn -q -ntp -B versions:set -DnewVersion=${env.APP_NEW_VER}"
                }
            }
        }


        stage('Tag Repository') {
            steps {
                sshagent(credentials: ['GIT_KEY']) {
                    load "app_version.groovy"
                    sh "git tag -a ${env.APP_NEW_VER} -m \"Version ${env.APP_NEW_VER}\""
                    sh "ssh-keyscan github.com >> ~/.ssh/known_hosts"
                    sh "git remote set-url origin git@github.com:wolender/spring-petclinic.git"
                    sh """
                    git add . 
                    git commit -m \"Version: ${env.APP_NEW_VER}\"
                    git push git@github.com:wolender/spring-petclinic.git 
                    """

                }

            }
        }

        stage('Build') {
            steps {
                load "app_version.groovy"
                sh 'mvn clean install -DskipTests -Dspring.profiles.active=mysql'
                sh "docker build -t wolender-ecr:${env.APP_NEW_VER} ."
            }
            
        }

        stage('Push') {
            steps {

                load "env_variables.groovy"

                sh "aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin ${env.REPO_URL}"
                sh "docker tag wolender-ecr:${env.APP_NEW_VER} ${env.REPO_URL}:${env.APP_NEW_VER}"
                sh "docker push ${env.REPO_URL}:${env.APP_NEW_VER}"
            }
            
        }

    }
}