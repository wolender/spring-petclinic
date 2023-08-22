pipeline {
    agent any


    stages {
        stage('Pull') {
            steps {
                git branch: 'main', url:'https://github.com/wolender/spring-petclinic.git'
            }
        }

        stage('Login') {
            steps {
                dir('deploy'){
                    load "$JENKINS_HOME/env_variables.groovy"
                    
                    sh 'echo "ec2-user@${env.APP_IP}"'
                    sh 'ansible-playbook deploy_playbook.yaml -e "REPO_URL=${env.REPO_URL}" '
                }

            }
            
        }

    }
}