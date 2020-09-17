#!groovy

@Library('realm-ci') _

import groovy.json.JsonOutput

// CONSTANTS

// Branches from which we release SNAPSHOT's. Only release branches need to run on actual hardware.
releaseBranches = ['master', 'next-major', 'v10']
// Branches that are "important", so if they do not compile they will generate a Slack notification
slackNotificationBranches = [ 'master', 'releases', 'next-major', 'v10' ]
// WARNING: Only set to `false` as an absolute last resort. Doing this will disable all integration
// tests.
enableIntegrationTests = true

// RUNTIME PROPERTIES

// Will store whether or not this build was successful.
buildSuccess = false
// Will be set to `true` if this build is a full release that should be available on Bintray.
// This is determined by comparing the current git tag to the version number of the build.
publishBuild = false
mongoDbRealmContainer = null
mongoDbRealmCommandServerContainer = null
emulatorContainer = null
dockerNetworkId = UUID.randomUUID().toString()
currentBranch = env.CHANGE_BRANCH
// FIXME: Always used the emulator until we can enable more reliable devices
// 'android' nodes have android devices attached and 'brix' are physical machines in Copenhagen.
// nodeSelector = (releaseBranches.contains(currentBranch)) ? 'android' : 'docker-cph-03' // Switch to `brix` when all CPH nodes work: https://jira.mongodb.org/browse/RCI-14
nodeSelector = 'docker-cph-03'
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

        // Check type of Build. We are treating this as a release build if we are building
        // the exact Git SHA that was tagged.
        gitTag = readGitTag()
        echo "Git tag: ${gitTag ?: 'none'}"
        if (!gitTag) {
          gitSha = sh(returnStdout: true, script: 'git rev-parse HEAD').trim().take(8)
          echo "Building non-release: ${gitSha}"
          setBuildName(gitSha)
          publishBuild = false
        } else {
          def version = readFile('version.txt').trim()
          if (gitTag != "v${version}") {
            error "Git tag '${gitTag}' does not match v${version}"
          } else {
            echo "Building release: '${gitTag}'"
            setBuildName("Tag ${gitTag}")
            publishBuild = true
          }
        }

      // Attempt to figure out what is going on


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
           // Build development branch
           useEmulator = true
           emulatorImage = "system-images;android-29;default;x86"
           abiFilter = "-PbuildTargetABIs=x86"
           instrumentationTestTarget = "connectedObjectServerDebugAndroidTest"
           deviceSerial = "emulator-5554"
         } else {
           // Build main/release branch
           // FIXME: Use emulator until we can get reliable devices on CI.
           //  But still build all ABI's and run all types of tests.
           useEmulator = true
           emulatorImage = "system-images;android-29;default;x86"
           instrumentationTestTarget = "connectedAndroidTest"
           deviceSerial = "emulator-5554"
         }

         try {

           def buildEnv = null
           stage('Prepare Docker Images') {
             // TODO Should be renamed to 'master' when merged there.
             // TODO Caching is currently disabled (with -do-not-cache suffix) due to the upload speed
             //  in Copenhagen being too slow. So the upload times out.
             buildEnv = buildDockerEnv("ci/realm-java:v10", push: currentBranch == 'v10-do-not-cache')
             def props = readProperties file: 'dependencies.list'
             echo "Version in dependencies.list: ${props.MONGODB_REALM_SERVER}"
             def mdbRealmImage = docker.image("docker.pkg.github.com/realm/ci/mongodb-realm-test-server:${props.MONGODB_REALM_SERVER}")
             docker.withRegistry('https://docker.pkg.github.com', 'github-packages-token') {
               mdbRealmImage.pull()
             }
             def commandServerEnv = docker.build 'mongodb-realm-command-server', "tools/sync_test_server"

             // Prepare Docker containers used by Instrumentation tests
             // TODO: How much of this logic can be moved to start_server.sh for shared logic with local testing.
             sh "docker network create ${dockerNetworkId}"
             mongoDbRealmContainer = mdbRealmImage.run("--network ${dockerNetworkId}")
             mongoDbRealmCommandServerContainer = commandServerEnv.run("--network container:${mongoDbRealmContainer.id}")
             sh "docker cp tools/sync_test_server/app_config ${mongoDbRealmContainer.id}:/tmp/app_config-testapp1"
             sh "docker cp tools/sync_test_server/app_config ${mongoDbRealmContainer.id}:/tmp/app_config-testapp2"
             sh "docker cp tools/sync_test_server/setup_mongodb_realm.sh ${mongoDbRealmContainer.id}:/tmp/"
             sh "docker exec -i ${mongoDbRealmContainer.id} sh /tmp/setup_mongodb_realm.sh"
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
                   "--network container:${mongoDbRealmContainer.id} ") {

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
//                   runBuild(abiFilter, instrumentationTestTarget)
                 } finally {
                   sh "adb emu kill"
                 }
               } else {
//                 runBuild(abiFilter, instrumentationTestTarget)
               }
             }

             runPublish()
           }

           // Release the library if needed
//           if (publishBuild) {
//           }


         } finally {
           // We assume that creating these containers and the docker network can be considered an atomic operation.
           if (mongoDbRealmContainer != null && mongoDbRealmCommandServerContainer != null) {
             archiveServerLogs(mongoDbRealmContainer.id, mongoDbRealmCommandServerContainer.id)
             mongoDbRealmContainer.stop()
             mongoDbRealmCommandServerContainer.stop()
             sh "docker network rm ${dockerNetworkId}"
           }
           if (emulatorContainer != null) {
             emulatorContainer.stop()
           }
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
  if (slackNotificationBranches.contains(currentBranch)) {
    node {
      withCredentials([[$class: 'StringBinding', credentialsId: 'slack-webhook-java-ci-channel', variable: 'SLACK_URL']]) {
        def payload = null
        if (!buildSuccess) {
          payload = JsonOutput.toJson([
                  username: "Realm CI",
                  icon_emoji: ":realm_new:",
                  text: "*The ${currentBranch} branch is broken!*\n<${env.BUILD_URL}|Click here> to check the build."
          ])
        } else if (currentBuild.getPreviousBuild() && currentBuild.getPreviousBuild().getResult().toString() != "SUCCESS" && buildSuccess) {
          payload = JsonOutput.toJson([
                  username: "Realm CI",
                  icon_emoji: ":realm_new:",
                  text: "*${currentBranch} is back to normal!*\n<${env.BUILD_URL}|Click here> to check the build."
          ])
        }

        if (payload != null) {
          sh "curl -X POST --data-urlencode \'payload=${payload}\' ${env.SLACK_URL}"
        }
      }
    }
  }
}

// Runs all build steps
def runBuild(abiFilter, instrumentationTestTarget) {

  stage('Build') {
    sh "chmod +x gradlew"
    sh "./gradlew assemble ${abiFilter} --stacktrace"
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
        gradle('realm', "spotbugsMain pmd checkstyle ${abiFilter}")
      } finally {
        publishHTML(target: [
          allowMissing: false, 
          alwaysLinkToLastBuild: false, 
          keepAll: true, 
          reportDir: 'realm/realm-library/build/reports/spotbugs', 
          reportFiles: 'main.html', 
          reportName: 'Spotbugs report'
        ])

        publishHTML(target: [
          allowMissing: false, 
          alwaysLinkToLastBuild: false, 
          keepAll: true, 
          reportDir: 'realm/realm-library/build/reports/pmd', 
          reportFiles: 'pmd.html', 
          reportName: 'PMD report'
        ])
        
        publishHTML(target: [
          allowMissing: false, 
          alwaysLinkToLastBuild: false, 
          keepAll: true, 
          reportDir: 'realm/realm-library/build/reports/checkstyle', 
          reportFiles: 'checkstyle.html', 
          reportName: 'Checkstyle report'
        ])
      }
    },
    'Instrumentation' : {
      if (enableIntegrationTests) {
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
      } else {
        echo "Instrumentation tests were disabled."
      }
    },
    'Gradle Plugin' : {
      try {
        gradle('gradle-plugin', 'check --debug')
      } finally {
        storeJunitResults 'gradle-plugin/build/test-results/test/TEST-*.xml'
      }
    },
    'JavaDoc': {
      sh "./gradlew javadoc ${abiFilter} --stacktrace"
    }
  }

  // TODO: add support for running monkey on the example apps

  if (['master'].contains(currentBranch)) {
    stage('Collect metrics') {
      collectAarMetrics()
    }
  }

  if (releaseBranches.contains(currentBranch) && !publishBuild) {
    stage('Publish to OJO') {
      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'bintray', passwordVariable: 'BINTRAY_KEY', usernameVariable: 'BINTRAY_USER']]) {
        sh "chmod +x gradlew && ./gradlew -PbintrayUser=${env.BINTRAY_USER} -PbintrayKey=${env.BINTRAY_KEY} ojoUpload --stacktrace"
      }
    }
  }
}

def runPublish() {
  stage('Publish Release') {
    withCredentials([
            [$class: 'StringBinding', credentialsId: 'slack-webhook-java-ci-channel', variable: 'SLACK_URL_CI'],
            [$class: 'StringBinding', credentialsId: 'slack-webhook-releases-channel', variable: 'SLACK_URL_RELEASE'],
            [$class: 'UsernamePasswordMultiBinding', credentialsId: 'bintray', passwordVariable: 'BINTRAY_KEY', usernameVariable: 'BINTRAY_USER'],
            [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'DOCS_S3_ACCESS_KEY', credentialsId: 'mongodb-realm-docs-s3', secretKeyVariable: 'DOCS_S3_SECRET_KEY'],
            [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'REALM_S3_ACCESS_KEY', credentialsId: 'realm-s3', secretKeyVariable: 'REALM_S3_SECRET_KEY']
    ]) {
      sh """
        set +x  
        sh tools/publish_release.sh '$BINTRAY_USER' '$BINTRAY_KEY' \
        '$REALM_S3_ACCESS_KEY' '$REALM_S3_SECRET_KEY' \
        '$DOCS_S3_ACCESS_KEY' '$DOCS_S3_SECRET_KEY' \
        '$SLACK_URL_RELEASE' \
        '$SLACK_URL_CI'
      """
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

def archiveServerLogs(String mongoDbRealmContainerId, String commandServerContainerId) {
  sh "docker logs ${commandServerContainerId} > ./command-server.log"
  zip([
    'zipFile': 'command-server-log.zip',
    'archive': true,
    'glob' : 'command-server.log'
  ])
  sh 'rm command-server.log'

  sh "docker cp ${mongoDbRealmContainerId}:/var/log/stitch.log ./stitch.log"
  zip([
    'zipFile': 'stitchlog.zip',
    'archive': true,
    'glob' : 'stitch.log'
  ])
  sh 'rm stitch.log'

  sh "docker cp ${mongoDbRealmContainerId}:/var/log/mongodb.log ./mongodb.log"
  zip([
    'zipFile': 'mongodb.zip',
    'archive': true,
    'glob' : 'mongodb.log'
  ])
  sh 'rm mongodb.log'
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

def readGitTag() {
  def command = 'git describe --exact-match --tags HEAD'
  def returnStatus = sh(returnStatus: true, script: command)
  if (returnStatus != 0) {
    return null
  }
  return sh(returnStdout: true, script: command).trim()
}
