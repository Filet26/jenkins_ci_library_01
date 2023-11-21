def call() {
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
                sh 'find . -type f -name "*.py" | xargs bandit -s B106'
            }
        }


    }

}
}