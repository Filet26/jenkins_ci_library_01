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
                    withCredentials([sshUserPrivateKey(credentialsId: 'filetkafkaKEY', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')]) {
                        sshagent(['filetkafkaKEY']) {
                            sshCommand remote: [
                                name: '20.150.206.132',
                                host: '20.150.206.132',
                                user: SSH_USER,
                                identityFile: SSH_KEY_FILE,
                                allowAnyHosts: 'true'
                            ], command: """
                                cd /home/azureuser/Microservices-4850/deployment &&
                                docker compose down &&
                                docker image pull filetfilet/${dockerRepoName}:${imageName} &&
                                docker compose up -d
                            """
                        }
                    }
                }
            }
}


        }

    }
}