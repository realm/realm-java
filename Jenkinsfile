#!groovy

{ ->
    try {
        node('docker') {
            stage 'SCM'
            if (isPullRequest()) {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "origin/pull/${GITHUB_PR_NUMBER}/head"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'CleanCheckout']],
                    gitTool: 'native git',
                    submoduleCfg: [],
                    userRemoteConfigs: [[
                    credentialsId: '1642fb1a-1a82-4b10-a25e-f9e95f43c93f',
                    name: 'origin',
                    refspec: "+refs/heads/master:refs/remotes/origin/master +refs/pull/${GITHUB_PR_NUMBER}/head:refs/remotes/origin/pull/${GITHUB_PR_NUMBER}/head",
                    url: 'https://github.com/realm/realm-java.git'
                    ]]
                ])
            } else {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'CleanCheckout']],
                    gitTool: 'native git',
                    submoduleCfg: [],
                    userRemoteConfigs: [[url: 'https://github.com/realm/realm-java.git']]
                ])
            }

            stage 'Docker build'
            def buildEnv = docker.build 'realm-java:snapshot'
            buildEnv.inside("--privileged -v /dev/bus/usb:/dev/bus/usb -v ${env.HOME}/gradle-cache:/root/.gradle") {
                stage 'JVM tests'
                try {
                    gradle 'assemble check javadoc'
                } finally {
                    storeJunitResults 'realm/realm-annotations-processor/build/test-results/TEST-*.xml'
                }

                try {
                    sh 'cd examples && ./gradlew unitTestExample:check --stacktrace'
                } finally {
                    storeJunitResults 'examples/unitTestExample/build/test-results/**/TEST-*.xml'
                }

                stage 'Static code analysis'
                try {
                    sh 'cd realm && ./gradlew findbugs pmd checkstyle --stacktrace'
                } finally {
                    publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/findbugs', reportFiles: 'findbugs-output.html', reportName: 'Findbugs issues'])
                    publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/reports/pmd', reportFiles: 'pmd.html', reportName: 'PMD Issues'])
                }

                stage 'Run instrumented tests'
                boolean archiveLog = true
                String backgroundPid
                try {
                    sh 'cd realm'
                    backgroundPid = startLogCatCollector()
                    sh './gradlew connectedUnitTests --stacktrace'
                    archiveLog = false;
                } finally {
                    stopLogCatCollector(backgroundPid, archiveLog)
                    storeJunitResults 'realm/realm-library/build/outputs/androidTest-results/connected/TEST-*.xml'
                }

               // TODO: add support for running monkey on the example apps

                if (env.BRANCH_NAME == 'master') {
                    stage 'Collect metrics'
                    collectAarMetrics()

                    stage 'Publish to OJO'
                    sh 'chmod +x gradlew && ./gradlew assemble ojoUpload'
                }
            }
        }

        currentBuild.rawBuild.setResult(Result.SUCCESS)
    } catch (Exception e) {
        echo "The job failed with the following exception: ${e.getMessage()}"
        currentBuild.rawBuild.setResult(Result.FAILURE)
    } finally {
        if (isPullRequest()) {
            node {
                reportResultToGithub()
            }
        }
    }
}

def String startLogCatCollector() {
    sh '''adb logcat -c
    `adb logcat > "logcat.txt" &` & echo $1 > pid
    '''
    return readFile("pid").trim()
}

def stopLogCatCollector(String backgroundPid, boolean archiveLog) {
    sh 'kill $backgroundPid'
    if (archiveLog) {
        zip([
            'zipFile': 'logcat.zip',
            'archive': true,
            'glob' : 'logcat.txt'
        ])
    }
    sh 'rm logcat.txt '
}

def isPullRequest() {
    return binding.variables.containsKey('GITHUB_PR_NUMBER')
}

def reportResultToGithub() {
    step([
        $class: 'GitHubPRBuildStatusPublisher',
        statusMsg: [content: "${GITHUB_PR_COND_REF} run ended"],
        unstableAs: 'FAILURE'
    ])
}

def sendMetrics(String metric, String value) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '5b8ad2d9-61a4-43b5-b4df-b8ff6b1f16fa', passwordVariable: 'influx_pass', usernameVariable: 'influx_user']]) {
        sh "curl -i -XPOST 'https://greatscott-pinheads-70.c.influxdb.com:8086/write?db=realm' --data-binary '${metric} value=${value}i' --user '${env.influx_user}:${env.influx_pass}'"
    }
}

def sendTaggedMetric(String metric, String value, String tagName, String tagValue) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '5b8ad2d9-61a4-43b5-b4df-b8ff6b1f16fa', passwordVariable: 'influx_pass', usernameVariable: 'influx_user']]) {
        sh "curl -i -XPOST 'https://greatscott-pinheads-70.c.influxdb.com:8086/write?db=realm' --data-binary '${metric},${tagName}=${tagValue} value=${value}i' --user '${env.influx_user}:${env.influx_pass}'"
    }
}

def storeJunitResults(String path) {
    step([
        $class: 'JUnitResultArchiver',
        testResults: path
    ])
}

def collectAarMetrics() {
    dir('realm/realm-library/build/outputs/aar') {
        sh '''set -xe
        unzip realm-android-library-release.aar -d unzipped
        find $ANDROID_HOME -name dx | sort -r | head -n 1 > dx
        $(cat dx) --dex --output=temp.dex unzipped/classes.jar
        cat temp.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 "%d"' > methods
        '''
        sendMetrics('methods', readFile('methods'))

        sendMetrics('aar_size', new File('realm-android-library-release.aar').length())

        def rootFolder = new File('unzipped/jni')
        rootFolder.traverse (type: DIRECTORIES) { folder ->
            def abiName = folder.name()
            def libSize = new File(folder, 'librealm-jni.so').size() as String
            sendTaggedMetric('abi_size', libSize, 'type', abiName)
        }
    }
}

def gradle(String commands) {
    sh "chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}
