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
                    projectName: 'Infrastructure/Provision Resources', // Name of the first pipeline
                    filter: 'env_variables.groovy', // Path to the artifact in the first pipeline
                    target: "vars" 
                )
            }
        }

        stage('Deploy via Ansible') {
            steps {
                dir('deploy'){

                    withCredentials([usernamePassword(credentialsId: 'db_creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        load "../vars/env_variables.groovy"
                        load "../vars/app_version.groovy"
                        
                        sh "echo ec2-user@${env.APP_IP} > inventory "
                        sh '/var/lib/jenkins/.local/bin/ansible-playbook -v deploy_playbook.yaml -e "REPO_URL=$REPO_URL" -e "MYSQL_URL=$MYSQL_URL" -e "MYSQL_USER=$USERNAME" -e "MYSQL_PASS=$PASSWORD" -e "APP_NEW_VER=$APP_NEW_VER"'
                        sh "echo \"http://${env.APP_LB_URL}\""
                    }
                }

            }
            
        }
        stage('Output') {
            steps {
                dir('vars'){
                        sh "cd "
                        sh "ls -al"
                        sh "pwd"
                        stash(name: 'Vars')
                }
            }
        }

    }
    post {
        success {
            //unstash('Vars')
            script {
                sh "ls -al"
                load "vars/env_variables.groovy"
                load "vars/app_version.groovy"
                def appUrl = "http://${env.APP_LB_URL}"
                currentBuild.description = "Application version ${env.APP_NEW_VER} URL: ${appUrl}"
            }
        }
    }
}