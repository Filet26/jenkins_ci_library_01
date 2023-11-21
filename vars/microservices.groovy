def call(dockerRepoName, imageName) {
    pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'pip install -r requirements.txt --break-system-packages'
                sh 'pip install bandit --break-system-packages'
            }
        }
        stage("Python Lint") {
            steps {
                sh 'find . -type f -name "*.py" | xargs pylint --fail-under=5.0'
            }
        }

        stage("Python Security Scanning") {
            steps {
                sh 'find . -type f -name "*.py" | xargs bandit'
            }
        }

        stage('Package') {
            when {
                expression { env.GIT_BRANCH == 'origin/main' }
            }
            steps {
                withCredentials([string(credentialsId: 'DockerHubDHO', variable: 'TOKEN')]) {
                    script {
                        sh "docker login -u 'filetfilet' -p '$TOKEN' docker.io"
                        sh "docker build -t ${dockerRepoName}:latest --tag filetfilet/${dockerRepoName}:${imageName} ."
                        sh "docker push filetfilet/${dockerRepoName}:${imageName}"
                    }
                }
            }
        }

        stage('Deploy to Kafka Server - Docker Compose') {
            steps {
                script {
                    def remote = [:]
                    remote.user = 'azureuser'
                    remote.host = '20.150.206.132'
                    remote.name = 'azureuser'
                    
                    withCredentials([sshUserPrivateKey(credentialsId: 'filetkafkaKEY', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')]) {
                        remote.identityFile = "${SSH_KEY_FILE}"
                    }
                    
                    // sh """ssh -o StrictHostKeyChecking=no -i $SSH_KEY_FILE ${SSH_USER}@20.150.206.132 <<EOF
                    // cd /home/azureuser/Microservices-4850/deployment
                    // docker-compose up -d
                    // EOF"""


                }
            }
}

        }

    }
}