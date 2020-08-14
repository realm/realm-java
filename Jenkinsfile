#!groovy

@Library('realm-ci') _

import groovy.json.JsonOutput

buildSuccess = false
rosContainer = null
dockerNetworkId = UUID.randomUUID().toString()
// Branches from which we release SNAPSHOT's. Only release branches need to run on actual hardware.
releaseBranches = ['master', 'next-major', 'v10']
// Branches that are "important", so if they do not compile they will generate a Slack notification
slackNotificationBranches = [ 'master', 'releases', 'next-major', 'v10' ]
currentBranch = env.CHANGE_BRANCH
// 'android' nodes have android devices attached and 'brix' are physical machines in Copenhagen.
nodeSelector = (releaseBranches.contains(currentBranch)) ? 'android' : 'docker-cph-03' // Switch to `brix` when all CPH nodes work: https://jira.mongodb.org/browse/RCI-14
try {
  node(nodeSelector) {
    timeout(time: 90, unit: 'MINUTES') {
      // Allocate a custom workspace to avoid having % in the path (it breaks ld)
      ws('/tmp/realm-java') {
        stage('SCM') {
          checkout([
                  $class           : 'GitSCM',
                  branches         : scm.branches,
                  gitTool          : 'native git',
                  extensions       : scm.extensions + [
                          [$class: 'CleanCheckout'],
                          [$class: 'SubmoduleOption', recursiveSubmodules: true]
                  ],
                  userRemoteConfigs: scm.userRemoteConfigs
          ])
        }

        // Toggles for PR vs. Master builds.
        // - For PR's, we favor speed > absolute correctness. So we just build for x86, use an
        //   emulator and run unit tests for the ObjectServer variant.
        // - For branches from which we make releases, we build all architectures and run tests
        //   on an actual device.
        def useEmulator = false
        def emulatorImage = ""
        def abiFilter = ""
        def instrumentationTestTarget = "connectedAndroidTest"
        def deviceSerial = ""
        if (!releaseBranches.contains(currentBranch)) {
          useEmulator = true
          emulatorImage = "system-images;android-29;default;x86"
          abiFilter = "-PbuildTargetABIs=x86"
          instrumentationTestTarget = "connectedObjectServerDebugAndroidTest"
          deviceSerial = "emulator-5554"
        }

        try {

          def buildEnv = null
          stage('Prepare Docker Images') {
            buildEnv = docker.build 'realm-java:snapshot'
            // Docker image for testing Realm Object Server
            def dependProperties = readProperties file: 'dependencies.list'
            def rosVersion = dependProperties["REALM_OBJECT_SERVER_VERSION"]
            withCredentials([string(credentialsId: 'realm-sync-feature-token-enterprise', variable: 'realmFeatureToken')]) {
              rosEnv = docker.build 'ros:snapshot', "--build-arg ROS_VERSION=${rosVersion} --build-arg REALM_FEATURE_TOKEN=${realmFeatureToken} tools/sync_test_server"
            }
            rosContainer = rosEnv.run()
          }

          // There is a chance that real devices are attached to the host, so if the emulator is
          // running we need to make sure that ADB and tests targets the correct device.
          String restrictDevice = ""
          if (deviceSerial != null) {
            restrictDevice = "-e ANDROID_SERIAL=${deviceSerial} "
          }

          buildEnv.inside("-e HOME=/tmp " +
                  "-e _JAVA_OPTIONS=-Duser.home=/tmp " +
                  "--privileged " +
                  "-v /dev/kvm:/dev/kvm " +
                  "-v /dev/bus/usb:/dev/bus/usb " +
                  "-v ${env.HOME}/gradle-cache:/tmp/.gradle " +
                  "-v ${env.HOME}/.android:/tmp/.android " +
                  "-v ${env.HOME}/ccache:/tmp/.ccache " +
                  restrictDevice +
                  "-e REALM_CORE_DOWNLOAD_DIR=/tmp/.gradle " +
                  "--network container:${rosContainer.id} ") {

            // Lock required around all usages of Gradle as it isn't
            // able to share its cache between builds.
            lock("${env.NODE_NAME}-android") {
              if (useEmulator) {
                // TODO: We should wait until the emulator is online. For now assume it starts fast enough
                //  before the tests will run, since the library needs to build first.
                sh """yes '\n' | avdmanager create avd -n CIEmulator -k '${emulatorImage}' --force"""
                sh "adb start-server" // https://stackoverflow.com/questions/56198290/problems-with-adb-exe
                // Need to go to ANDROID_HOME due to https://askubuntu.com/questions/1005944/emulator-avd-does-not-launch-the-virtual-device
                sh "cd \$ANDROID_HOME/tools && emulator -avd CIEmulator -no-boot-anim -no-window -wipe-data -noaudio -partition-size 4098 &"
                try {
                  runBuild(abiFilter, instrumentationTestTarget)
                } finally {
                  sh "adb emu kill"
                }
              } else {
                runBuild(abiFilter, instrumentationTestTarget)
              }
            }
          }
        } finally {
          archiveServerLogs(rosContainer.id)
          sh "docker logs ${rosContainer.id}"
          rosContainer.stop()
        }
      }
    }
    currentBuild.rawBuild.setResult(Result.SUCCESS)
    buildSuccess = true
  }
} catch(Exception e) {
  currentBuild.rawBuild.setResult(Result.FAILURE)
  buildSuccess = false
  throw e
} finally {
  if (slackNotificationBranches.contains(currentBranch) && !buildSuccess) {
    node {
      withCredentials([[$class: 'StringBinding', credentialsId: 'slack-java-url', variable: 'SLACK_URL']]) {
        def payload = JsonOutput.toJson([
                username: 'Mr. Jenkins',
                icon_emoji: ':jenkins:',
                attachments: [[
                  'title': "The ${currentBranch} branch is broken!",
                  'text': "<${env.BUILD_URL}|Click here> to check the build.",
                  'color': "danger"
                ]]
        ])
        sh "curl -X POST --data-urlencode \'payload=${payload}\' ${env.SLACK_URL}"
      }
    }
  }
}

// Runs all build steps
def runBuild(abiFilter, instrumentationTestTarget) {

  stage('Build') {
    sh "chmod +x gradlew && ./gradlew assemble javadoc ${abiFilter} --stacktrace"
  }

  stage('Tests') {
    parallel 'JVM' : {
      try {
        sh "chmod +x gradlew && ./gradlew check ${abiFilter} --stacktrace"
      } finally {
        storeJunitResults 'realm/realm-annotations-processor/build/test-results/test/TEST-*.xml'
        storeJunitResults 'examples/unitTestExample/build/test-results/**/TEST-*.xml'
        storeJunitResults 'realm/realm-library/build/test-results/**/TEST-*.xml'
        step([$class: 'LintPublisher'])
      }
    },
    'Realm Transformer' : {
      try {
        gradle('realm-transformer', 'check')
      } finally {
        storeJunitResults 'realm-transformer/build/test-results/test/TEST-*.xml'
      }
    },
    'Static code analysis' : {
      try {
        gradle('realm', "findbugs checkstyle") // FIXME: pmd disabled: https://github.com/realm/realm-java/issues/7024
      } finally {
        publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/findbugs', reportFiles: 'findbugs-output.html', reportName: 'Findbugs issues'])
        // publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/reports/pmd', reportFiles: 'pmd.html', reportName: 'PMD Issues'])
        step([$class: 'CheckStylePublisher',
              canComputeNew: false,
              defaultEncoding: '',
              healthy: '',
              pattern: 'realm/realm-library/build/reports/checkstyle/checkstyle.xml',
              unHealthy: ''
        ])
      }
    },
    'Instrumentation' : {
      String backgroundPid
      try {
        backgroundPid = startLogCatCollector()
        forwardAdbPorts()
        gradle('realm', "${instrumentationTestTarget} ${abiFilter}")
      } finally {
        stopLogCatCollector(backgroundPid)
        storeJunitResults 'realm/realm-library/build/outputs/androidTest-results/connected/**/TEST-*.xml'
        storeJunitResults 'realm/kotlin-extensions/build/outputs/androidTest-results/connected/**/TEST-*.xml'
      }
    },
    'Gradle Plugin' : {
      try {
        gradle('gradle-plugin', 'check --debug')
      } finally {
        storeJunitResults 'gradle-plugin/build/test-results/test/TEST-*.xml'
      }
    }
  }

  // TODO: add support for running monkey on the example apps

  if (['master'].contains(currentBranch)) {
    stage('Collect metrics') {
      collectAarMetrics()
    }
  }

  if (releaseBranches.contains(currentBranch)) {
    stage('Publish to OJO') {
      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'bintray', passwordVariable: 'BINTRAY_KEY', usernameVariable: 'BINTRAY_USER']]) {
        sh "chmod +x gradlew && ./gradlew -PbintrayUser=${env.BINTRAY_USER} -PbintrayKey=${env.BINTRAY_KEY} assemble ojoUpload --stacktrace"
      }
    }
  }
}

def forwardAdbPorts() {
  sh """ adb reverse tcp:9080 tcp:9080 && adb reverse tcp:9443 tcp:9443 &&
      adb reverse tcp:8888 tcp:8888 && adb reverse tcp:9090 tcp:9090
  """
}

String startLogCatCollector() {
  // Cancel build quickly if no device is available. The lock acquired already should
  // ensure we have access to a device. If not, it is most likely a more severe problem.
  timeout(time: 1, unit: 'MINUTES') {
    // Need ADB as root to clear all buffers: https://stackoverflow.com/a/47686978/1389357
    sh 'adb devices'
    sh """adb root
      adb logcat -b all -c 
      adb logcat -v time > 'logcat.txt' &
      echo \$! > pid
    """
    return readFile("pid").trim()
  }
}

def stopLogCatCollector(String backgroundPid) {
  // The pid might not be available if the build was terminated early or stopped due to
  // a build error.
  if (backgroundPid != null) {
    sh "kill ${backgroundPid}"
    zip([
      'zipFile': 'logcat.zip',
      'archive': true,
      'glob' : 'logcat.txt'
    ])
    sh 'rm logcat.txt'
  }
}

def archiveServerLogs(String rosContainerId) {
  sh "docker cp ${rosContainerId}:/tmp/integration-test-command-server.log ./ros.log"
  zip([
      'zipFile': 'roslog.zip',
      'archive': true,
      'glob' : 'ros.log'
  ])
  sh 'rm ros.log'
}

def sendMetrics(String metricName, String metricValue, Map<String, String> tags) {
  def tagsString = getTagsString(tags)
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '5b8ad2d9-61a4-43b5-b4df-b8ff6b1f16fa', passwordVariable: 'influx_pass', usernameVariable: 'influx_user']]) {
    sh "curl -i -XPOST 'https://influxdb.realmlab.net/write?db=realm' --data-binary '${metricName},${tagsString} value=${metricValue}i' --user '${env.influx_user}:${env.influx_pass}'"
  }
}

@NonCPS
def getTagsString(Map<String, String> tags) {
  return tags.collect { k,v -> "$k=$v" }.join(',')
}

def storeJunitResults(String path) {
  step([
    $class: 'JUnitResultArchiver',
    allowEmptyResults: true,
    testResults: path
  ])
}

def collectAarMetrics() {
  def flavors = ['base', 'objectServer']
  for (def i = 0; i < flavors.size(); i++) {
    def flavor = flavors[i]
    sh """set -xe
      cd realm/realm-library/build/outputs/aar
      unzip realm-android-library-${flavor}-release.aar -d unzipped${flavor}
      find \$ANDROID_HOME -name d8 | sort -r | head -n 1 > d8
      \$(cat d8) --release --output ./unzipped${flavor} unzipped${flavor}/classes.jar
      cat ./unzipped${flavor}/temp${flavor}.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 \"%d\"' > methods${flavor}
    """

    def methods = readFile("realm/realm-library/build/outputs/aar/methods${flavor}")
    sendMetrics('methods', methods, ['flavor':flavor])

    def aarFile = findFiles(glob: "realm/realm-library/build/outputs/aar/realm-android-library-${flavor}-release.aar")[0]
    sendMetrics('aar_size', aarFile.length as String, ['flavor':flavor])

    def soFiles = findFiles(glob: "realm/realm-library/build/outputs/aar/unzipped${flavor}/jni/*/librealm-jni.so")
    for (def j = 0; j < soFiles.size(); j++) {
      def soFile = soFiles[j]
      def abiName = soFile.path.tokenize('/')[-2]
      def libSize = soFile.length as String
      sendMetrics('abi_size', libSize, ['flavor':flavor, 'type':abiName])
    }
  }
}

def gradle(String commands) {
  sh "chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}

def gradle(String relativePath, String commands) {
  sh "cd ${relativePath} && chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}
