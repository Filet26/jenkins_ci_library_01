def call() {
    pipeline {
    agent any
    stages {
        // stage('Build') {
        //     steps {
        //         sh 'echo haha'
        //     }
        // }
        stage("Python Lint") {
            steps {
                sh 'find . -type f -name "*.py" | xargs pylint --fail-under=5.0'
            }
        }


    }

}
}