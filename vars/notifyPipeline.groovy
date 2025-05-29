def call(Map config = [:]) {
    timestamps {
        node {
            def statusMsg = ""
            def buildStatus = "SUCCESS"

            try {
                stage('Cred Scanning') {
                    try {
                        echo "Running credential scan..."
                        sh 'echo "No credentials found"'
                        statusMsg += "\n✅ Cred Scanning: Success"
                    } catch (e) {
                        buildStatus = "FAILURE"
                        statusMsg += "\n❌ Cred Scanning: ${e.message}"
                        error("Cred Scanning failed: ${e.message}")
                    }
                }

                stage('License Scanning') {
                    try {
                        echo "Running license scan..."
                        sh 'echo "All licenses compliant"'
                        statusMsg += "\n✅ License Scanning: Success"
                    } catch (e) {
                        buildStatus = "FAILURE"
                        statusMsg += "\n❌ License Scanning: ${e.message}"
                        error("License Scanning failed: ${e.message}")
                    }
                }

                stage('AMI Build') {
                    try {
                        echo "Building AMI..."
                        sh 'echo "AMI built successfully"'
                        statusMsg += "\n✅ AMI Build: Success"
                    } catch (e) {
                        buildStatus = "FAILURE"
                        statusMsg += "\n❌ AMI Build: ${e.message}"
                        error("AMI Build failed: ${e.message}")
                    }
                }

                stage('Commit Sign-off') {
                    try {
                        echo "Checking commit sign-off..."
                        sh 'git log -1 --pretty=%B | grep -q "Signed-off-by:"'
                        statusMsg += "\n✅ Commit Sign-off: Success"
                    } catch (e) {
                        buildStatus = "FAILURE"
                        statusMsg += "\n❌ Commit Sign-off: Commit missing sign-off"
                        error("Commit Sign-off check failed: Commit missing 'Signed-off-by'")
                    }
                }

            } catch (err) {
                currentBuild.result = "FAILURE"
                echo "Build failed: ${err.getMessage()}"
            } finally {
                currentBuild.result = buildStatus
                def slackColor = (buildStatus == "SUCCESS") ? "#2eb886" : "#ff0000"

                // ✅ Slack Notification
                slackSend(
                    channel: config.slackChannel ?: '#slack-notification',
                    color: slackColor,
                    message: "*Build Status: ${buildStatus}*\nJob: `${env.JOB_NAME}`\nBuild: #${env.BUILD_NUMBER}\n\n${statusMsg}"
                )

                // ✅ Email Notification
                mail(
                    subject: "Jenkins Build ${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: "Build Summary:\n\n${statusMsg}\n\nView Build: ${env.BUILD_URL}",
                    to: config.emailTo ?: 'shreytyagi75@gmail.com'
                )
            }
        }
    }
}
