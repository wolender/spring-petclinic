pipeline {
    agent any

    tools {
        maven 'mvn'
    }

    stages {
        stage('Pull') {
            steps {
                git branch: 'main', url:'https://github.com/wolender/spring-petclinic.git'
            }
        }

        stage ('Copy Variables'){
            steps{
                copyArtifacts(
                    projectName: 'Provision Resources', // Name of the first pipeline
                    filter: 'env_variables.groovy', // Path to the artifact in the first pipeline
                    target: "vars" 
                )
            }
        }

        stage('Deploy via Ansible') {
            steps {
                dir('deploy'){
                    
                    load "../vars/env_variables.groovy"
                    load "../vars/app_version.groovy"
                    
                    sh "echo ec2-user@${env.APP_IP} > inventory "
                    sh "/var/lib/jenkins/.local/bin/ansible-playbook -v deploy_playbook.yaml -e \"REPO_URL=${env.REPO_URL}\" -e \"MYSQL_URL=${env.MYSQL_URL}\" -e \"MYSQL_USER=${env.MYSQL_USER}\" -e \"APP_NEW_VER=${env.APP_NEW_VER}\""
                    sh "echo http://${env.APP_LB_URL}"
                }

            }
            
        }

    }
}