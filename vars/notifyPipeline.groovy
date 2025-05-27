def call(Map config = [:]) {
    def email = config.get('email', 'shreytyagi75@gmail.com')
    def slackChannel = config.get('slackChannel', '#slack-notification')
    def simulateFailure = config.get('simulateFailure', false)

    node {
        timestamps {
            try {
                stage('Test Success Notification') {
                    echo '✅ Running SUCCESS stage...'
                    sh 'exit 0'
                }

                if (simulateFailure) {
                    stage('Test Failure Notification') {
                        echo '❌ Running FAILURE stage (intentionally failing)...'
                        sh 'exit 1'
                    }
                }

                echo 'Build succeeded!'
                slackSend (
                    channel: slackChannel,
                    color: '#00FF00',
                    message: "✅ SUCCESS: *${env.JOB_NAME}* #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                )
                mail to: email,
                     subject: "✅ Jenkins Job Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: """\
The Jenkins job *${env.JOB_NAME}* completed successfully.

Details:
${env.BUILD_URL}
"""

            } catch (e) {
                echo 'Build failed!'
                slackSend (
                    channel: slackChannel,
                    color: '#FF0000',
                    message: "❌ FAILURE: *${env.JOB_NAME}* #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                )
                mail to: email,
                     subject: "❌ Jenkins Job Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: """\
The Jenkins job *${env.JOB_NAME}* has failed.

Logs:
${env.BUILD_URL}console
"""
                throw e
            } finally {
                echo "📣 Final Status: ${currentBuild.currentResult}"
            }
        }
    }
}
