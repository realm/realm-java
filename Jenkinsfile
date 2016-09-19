#!groovy

import groovy.json.JsonOutput

def buildSuccess = false
try {
  node('android') {
    // Allocate a custom workspace to avoid having % in the path (it breaks ld)
    ws('/tmp/realm-java') {
      stage 'SCM'
      checkout scm
      // Make sure not to delete the folder that Jenkins allocates to store scripts
      sh 'git clean -ffdx -e .????????'
      // Update submodule for object-store
      sh 'git submodule sync'
      sh 'git submodule update --init --force'

      stage 'Docker build'
      def buildEnv = docker.build 'realm-java:snapshot'
      buildEnv.inside("-e HOME=/tmp -e _JAVA_OPTIONS=-Duser.home=/tmp --privileged -v /dev/bus/usb:/dev/bus/usb -v ${env.HOME}/gradle-cache:/tmp/.gradle -v ${env.HOME}/.android:/tmp/.android") {
        stage 'JVM tests'
        try {
          gradle 'assemble check javadoc'
        } finally {
          storeJunitResults 'realm/realm-annotations-processor/build/test-results/TEST-*.xml'
          storeJunitResults 'examples/unitTestExample/build/test-results/**/TEST-*.xml'
          step([$class: 'LintPublisher'])
        }

        stage 'Static code analysis'
        try {
          gradle('realm', 'findbugs pmd checkstyle')
        } finally {
          publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/findbugs', reportFiles: 'findbugs-output.html', reportName: 'Findbugs issues'])
          publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/reports/pmd', reportFiles: 'pmd.html', reportName: 'PMD Issues'])
          step([$class: 'CheckStylePublisher',
          canComputeNew: false,
          defaultEncoding: '',
          healthy: '',
          pattern: 'realm/realm-library/build/reports/checkstyle/checkstyle.xml',
          unHealthy: ''
          ])
        }

        stage 'Run instrumented tests'
        boolean archiveLog = true
        String backgroundPid
        try {
          backgroundPid = startLogCatCollector()
          gradle('realm', 'connectedUnitTests')
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
          withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'bintray', passwordVariable: 'BINTRAY_KEY', usernameVariable: 'BINTRAY_USER']]) {
            sh "chmod +x gradlew && ./gradlew -PbintrayUser=${env.BINTRAY_USER} -PbintrayKey=${env.BINTRAY_KEY} assemble ojoUpload --stacktrace"
          }
        }
      }
    }
  }
  currentBuild.rawBuild.setResult(Result.SUCCESS)
  buildSuccess = true
} catch(Exception e) {
  currentBuild.rawBuild.setResult(Result.FAILURE)
  buildSuccess = false
  throw e
} finally {
  if (['master', 'releases'].contains(env.BRANCH_NAME) && !buildSuccess) {
    node {
      withCredentials([[$class: 'StringBinding', credentialsId: 'slack-java-url', variable: 'SLACK_URL']]) {
        def payload = JsonOutput.toJson([
          username: 'Mr. Jenkins',
          icon_emoji: ':jenkins:',
          attachments: [[
            'title': "The ${env.BRANCH_NAME} branch is broken!",
            'text': "<${env.BUILD_URL}|Click here> to check the build.",
            'color': "danger"
          ]]
        ])
        sh "curl -X POST --data-urlencode \'payload=${payload}\' ${env.SLACK_URL}"
      }
    }
  }
}


def String startLogCatCollector() {
  sh '''adb logcat -c
  adb logcat -v time > "logcat.txt" &
  echo $! > pid
  '''
  return readFile("pid").trim()
}

def stopLogCatCollector(String backgroundPid, boolean archiveLog) {
  sh "kill ${backgroundPid}"
  if (archiveLog) {
    zip([
      'zipFile': 'logcat.zip',
      'archive': true,
      'glob' : 'logcat.txt'
    ])
  }
  sh 'rm logcat.txt '
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
  sh '''set -xe
  cd realm/realm-library/build/outputs/aar
  unzip realm-android-library-release.aar -d unzipped
  find $ANDROID_HOME -name dx | sort -r | head -n 1 > dx
  $(cat dx) --dex --output=temp.dex unzipped/classes.jar
  cat temp.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 "%d"' > methods
  '''

  sendMetrics('methods', readFile('realm/realm-library/build/outputs/aar/methods'))

  def aarFile = findFiles(glob: 'realm/realm-library/build/outputs/aar/realm-android-library-release.aar')[0]
  sendMetrics('aar_size', aarFile.length as String)

  def soFiles = findFiles(glob: 'realm/realm-library/build/outputs/aar/unzipped/jni/*/librealm-jni.so')
  for (int i = 0; i < soFiles.length; i++) {
      def abiName = soFiles[i].path.tokenize('/')[-2]
      def libSize = soFiles[i].length as String
      sendTaggedMetric('abi_size', libSize, 'type', abiName)
  }
}

def gradle(String commands) {
  sh "chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}

def gradle(String relativePath, String commands) {
  sh "cd ${relativePath} && chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}
