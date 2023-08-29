pipeline {
    agent any


    stages {
        stage('Pull') {
            steps {
                git branch: 'main', url:'https://github.com/wolender/spring-petclinic.git'
            }
        }

        stage('Deploy via Ansible') {
            steps {
                dir('deploy'){
                    load "$JENKINS_HOME/env_variables.groovy"
                    
                    sh "echo ec2-user@${env.APP_IP} > inventory "
                    sh "/var/lib/jenkins/.local/bin/ansible-playbook -v deploy_playbook.yaml -e \"REPO_URL=${env.REPO_URL}\" "
                    sh "echo http://${env.APP_IP}:8080"
                }

            }
            
        }

    }
}