def call() {
    pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'echo haha'
            }
        }

    }

}
}