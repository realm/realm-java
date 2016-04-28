#!groovy

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

@NonCPS
def getDeviceNames(String commandOutput) {
    return commandOutput
        .split('\n')
        .findAll { it.contains('\t') }
        .collect { it.split('\t')[0].trim() }
}

def transformIntoStep(device) {
    // We need to wrap what we return in a Groovy closure, or else it's invoked
    // when this method is called, not when we pass it to parallel.
    // To do this, you need to wrap the code below in { }, and either return
    // that explicitly, or use { -> } syntax.
    return {
        sh "adb -s ${device} shell getprop ro.product.model | tee model-name.txt"
        def modelName = readFile('model-name.txt').trim().replaceAll(' ', '_')

        sh "adb -s ${device} uninstall io.realm.test"
        sh "adb -s ${device} install realm-android-library-debug-androidTest-unaligned.apk"
        sh "adb -s ${device} shell am instrument -w -r io.realm.test/android.support.test.runner.AndroidJUnitRunner > test_result_${modelName}_${device}.txt"
        sh "java -jar /opt/log-converter.jar test_result_${modelName}_${device}.txt"
    }
}

{ ->
    try {
        node('FastLinux') {
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

           stage 'JVM tests'
           try {
               gradle 'assemble check javadoc'
           } finally {
               storeJunitResults 'realm/realm-annotations-processor/build/test-results/TEST-*.xml'
           }
           if (env.BRANCH_NAME == 'master') {
               collectAarMetrics()
           }
           // TODO: add support for running monkey on the example apps
           //stash includes: 'examples/*/build/outputs/apk/*debug.apk', name: 'examples'

           dir('examples') {
              try {
                  gradle 'check'
              } finally {
                  storeJunitResults 'unitTestExample/build/test-results/**/TEST-*.xml'
              }
           }

           stage 'static code analysis'
           try {
                dir('realm') {
                   gradle 'findbugs pmd checkstyle'
                }
           } finally {
                publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/findbugs', reportFiles: 'findbugs-output.html', reportName: 'Findbugs issues'])
                publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/reports/pmd', reportFiles: 'pmd.html', reportName: 'PMD Issues'])
           }

           stage 'build instrumented tests'
           dir('realm') {
               gradle 'assembleDebugAndroidTest'
               dir('realm-library/build/outputs/apk') {
                  stash name: 'test-apk', includes: 'realm-android-library-debug-androidTest-unaligned.apk'
               }
           }
        }

        node('android-hub') {
            stage 'run instrumented tests'
            sh 'rm -rf *'
            unstash 'test-apk'

            sh 'adb devices | tee devices.txt'
            def adbDevices = readFile('devices.txt')
            def devices = getDeviceNames(adbDevices)

            if (!devices) {
                throw new IllegalStateException('No devices were found')
            }

            def parallelSteps = [:]
            devices.each { device ->
                parallelSteps[device] = transformIntoStep(device)
            }

            parallel parallelSteps
            storeJunitResults 'test_result_*.xml'

            // TODO: add support for running monkey on the example apps
            // stage 'monkey examples'
            // sh 'rm -rf *'
            // unstash 'examples'
        }

        if (env.BRANCH_NAME == 'master') {
            node('FastLinux') {
                stage 'publish to OJO'
                unstash 'java'
                sh 'chmod +x gradlew && ./gradlew assemble ojoUpload'
            }
        }
        currentBuild.rawBuild.setResult(Result.SUCCESS)
    } catch (Exception e) {
        echo e.getMessage()
        currentBuild.rawBuild.setResult(Result.FAILURE)
    } finally {
        if (isPullRequest()) {
            node {
                reportResultToGithub()
            }
        }
    }
}
