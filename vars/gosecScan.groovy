def call(Map config = [:]) {
    node {
        try {
            stage('Clean Workspace') {
                cleanWs()
            }

            stage('Clone Repository') {
                sh "git clone ${config.repoUrl ?: 'https://github.com/OT-MICROSERVICES/employee-api.git'}"
            }

            stage('Install Dependencies') {
                sh """
                    export PATH=/usr/local/go/bin:\$PATH
                    cd employee-api
                    go mod tidy
                """
            }

            stage('Install gosec') {
                sh """
                    export PATH=/usr/local/go/bin:\$PATH
                    go install github.com/securego/gosec/v2/cmd/gosec@latest
                    export PATH=\$HOME/go/bin:\$PATH
                """
            }

            stage('Run Dependency Scan') {
                sh """
                    export PATH=/usr/local/go/bin:\$PATH
                    export PATH=\$HOME/go/bin:\$PATH
                    cd employee-api
                    mkdir -p reports
                    gosec -fmt=json -out=reports/gosec-report.json ./... || true
                """
            }

            stage('Archive Report') {
                archiveArtifacts artifacts: 'employee-api/reports/gosec-report.json', fingerprint: true
            }
        } catch (err) {
            currentBuild.result = 'FAILURE'
            throw err
        }
    }
}
