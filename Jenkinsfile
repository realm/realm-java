#!groovy

import groovy.json.JsonOutput

def buildSuccess = false
def mongoDbRealmContainer = null
def mongoDbRealmCommandServerContainer = null
def dockerNetworkId = UUID.randomUUID().toString()
try {
  node('android') {
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
        // For PR's, we just build for arm-v7a and run unit tests for the ObjectServer variant
        // A full build is done on `master`.
        // TODO Once Android emulators are available on all nodes, we can switch to x86 builds
        // on PR's for even more throughput.
        def abiFilter = ""
        def instrumentationTestTarget = "connectedAndroidTest"
        if (!['master', 'next-major', 'v10'].contains(env.BRANCH_NAME)) {
          abiFilter = "-PbuildTargetABIs=armeabi-v7a"
          instrumentationTestTarget = "connectedObjectServerDebugAndroidTest"
          // Run in debug more for better error reporting
        }

        // Prepare Docker images
        // FIXME: Had issues moving these into a seperate Stage step. Is this needed?
        buildEnv = docker.build 'realm-java:snapshot'
        def props = readProperties file: 'dependencies.list'
        echo "Version in dependencies.list: ${props.MONGODB_REALM_SERVER_VERSION}"
        def mdbRealmImage = docker.image("docker.pkg.github.com/realm/ci/mongodb-realm-test-server:${props.MONGODB_REALM_SERVER_VERSION}")
        docker.withRegistry('https://docker.pkg.github.com', 'github-packages-token') {
          mdbRealmImage.pull()
        }
        def commandServerEnv = docker.build 'mongodb-realm-command-server', "tools/sync_test_server"

        try {
          // Prepare Docker containers used by Instrumentation tests
          // TODO: How much of this logic can be moved to start_server.sh for shared logic with local testing.
          sh "docker network create ${dockerNetworkId}"
          mongoDbRealmContainer = mdbRealmImage.run("--network ${dockerNetworkId}")
          mongoDbRealmCommandServerContainer = commandServerEnv.run("--network container:${mongoDbRealmContainer.id}")
          sh "docker cp tools/sync_test_server/app_config ${mongoDbRealmContainer.id}:/tmp/app_config"
          sh "docker cp tools/sync_test_server/setup_mongodb_realm.sh ${mongoDbRealmContainer.id}:/tmp/"
          sh "docker exec -i ${mongoDbRealmContainer.id} sh /tmp/setup_mongodb_realm.sh"

          buildEnv.inside("-e HOME=/tmp " +
                  "-e _JAVA_OPTIONS=-Duser.home=/tmp " +
                  "--privileged " +
                  "-v /dev/bus/usb:/dev/bus/usb " +
                  "-v ${env.HOME}/gradle-cache:/tmp/.gradle " +
                  "-v ${env.HOME}/.android:/tmp/.android " +
                  "-v ${env.HOME}/ccache:/tmp/.ccache " +
                  "-e REALM_CORE_DOWNLOAD_DIR=/tmp/.gradle " +
                  "--network container:${mongoDbRealmContainer.id} ") {

            // Lock required around all usages of Gradle as it isn't
            // able to share its cache between builds.
            lock("${env.NODE_NAME}-android") {

              stage('JVM tests') {
                try {
                  withCredentials([[$class: 'FileBinding', credentialsId: 'c0cc8f9e-c3f1-4e22-b22f-6568392e26ae', variable: 'S3CFG']]) {
                    sh "chmod +x gradlew && ./gradlew assemble check javadoc -Ps3cfg=${env.S3CFG} ${abiFilter} --stacktrace"
                  }
                } finally {
                  storeJunitResults 'realm/realm-annotations-processor/build/test-results/test/TEST-*.xml'
                  storeJunitResults 'examples/unitTestExample/build/test-results/**/TEST-*.xml'
                  step([$class: 'LintPublisher'])
                }
              }

              stage('Realm Transformer tests') {
                try {
                  gradle('realm-transformer', 'check')
                } finally {
                  storeJunitResults 'realm-transformer/build/test-results/test/TEST-*.xml'
                }
              }


              stage('Static code analysis') {
                try {
                  gradle('realm', "findbugs ${abiFilter}") // FIXME Renable pmd and checkstyle
                } finally {
                  publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/findbugs', reportFiles: 'findbugs-output.html', reportName: 'Findbugs issues'])
//                  publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'realm/realm-library/build/reports/pmd', reportFiles: 'pmd.html', reportName: 'PMD Issues'])
//                  step([$class: 'CheckStylePublisher',
//                        canComputeNew: false,
//                        defaultEncoding: '',
//                        healthy: '',
//                        pattern: 'realm/realm-library/build/reports/checkstyle/checkstyle.xml',
//                        unHealthy: ''
//                  ])
                }
              }

              stage('Run instrumented tests') {
                String backgroundPid
                try {
                  backgroundPid = startLogCatCollector()
                  forwardAdbPorts()
                  gradle('realm', "${instrumentationTestTarget}")
                } finally {
                  stopLogCatCollector(backgroundPid)
                  storeJunitResults 'realm/realm-library/build/outputs/androidTest-results/connected/**/TEST-*.xml'
                  storeJunitResults 'realm/kotlin-extensions/build/outputs/androidTest-results/connected/**/TEST-*.xml'
                }
              }

              // Gradle plugin tests require that artifacts are available, so this
              // step needs to be after the instrumentation tests
              stage('Gradle plugin tests') {
                try {
                  gradle('gradle-plugin', 'check --debug')
                } finally {
                  storeJunitResults 'gradle-plugin/build/test-results/test/TEST-*.xml'
                }
              }

              // TODO: add support for running monkey on the example apps

              if (['master'].contains(env.BRANCH_NAME)) {
                stage('Collect metrics') {
                  collectAarMetrics()
                }
              }

              if (['master', 'next-major', 'v10'].contains(env.BRANCH_NAME)) {
                stage('Publish to OJO') {
                  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'bintray', passwordVariable: 'BINTRAY_KEY', usernameVariable: 'BINTRAY_USER']]) {
                    sh "chmod +x gradlew && ./gradlew -PbintrayUser=${env.BINTRAY_USER} -PbintrayKey=${env.BINTRAY_KEY} assemble ojoUpload --stacktrace"
                  }
                }
              }
            }
          }
        } finally {
          archiveServerLogs(mongoDbRealmContainer.id, mongoDbRealmCommandServerContainer.id)
          mongoDbRealmContainer.stop()
          mongoDbRealmCommandServerContainer.stop()
          sh "docker network rm ${dockerNetworkId}"
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
  if (['master', 'releases', 'next-major'].contains(env.BRANCH_NAME) && !buildSuccess) {
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

def forwardAdbPorts() {
  sh ''' adb reverse tcp:9080 tcp:9080 && adb reverse tcp:9443 tcp:9443 &&
      adb reverse tcp:8888 tcp:8888 && adb reverse tcp:9090 tcp:9090
  '''
}

def String startLogCatCollector() {
  sh '''adb logcat -c
  adb logcat -v time > "logcat.txt" &
  echo $! > pid
  '''
  return readFile("pid").trim()
}

def stopLogCatCollector(String backgroundPid) {
  sh "kill ${backgroundPid}"
  zip([
          'zipFile': 'logcat.zip',
          'archive': true,
          'glob' : 'logcat.txt'
  ])
  sh 'rm logcat.txt'
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
