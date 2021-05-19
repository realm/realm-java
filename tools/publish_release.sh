#!/bin/sh

# Script that make sure release build is correctly published in the appropriate channels.
#
# The following steps are executed:
#
# 1. Check that version in version.txt matches git tag which indicate a release.
# 2. Check that the changelog has a correct set date.
# 3. Build Javadoc
# 4. Upload all artifacts to Bintray without releasing them.
# 5. Verify that all artifacts have been uploaded, then release all of them at once.
# 6. Upload native debug symobols and update latest version number on S3.
# 7. Upload Javadoc to MongoDB Realm S3 bucket.
# 8. Notify #realm-releases and #realm-java-team-ci about the new release.
set -e

######################################
# Input Validation
######################################

usage() {
cat <<EOF
Usage: $0 <maven_central_user> <maven_central_key> <realm_s3_access_key> <realm_s3_secret_key> <docs_s3_access_key> <docs_s3_secret_key> <slack-webhook-releases-url> <slack-webhook-java-ci-url> <gradle-build-params>
Usage: $0 verify
EOF
}

if [ "$#" -ne 9 ] && [ "$1" != "verify" ]; then
  usage
  exit 1
fi

######################################
# Define Release steps
######################################

HERE=$(dirname `realpath "$0"`)
REALM_JAVA_PATH="$HERE/.."
RELEASE_VERSION=""
MAVEN_CENTRAL_USER="$1"
MAVEN_CENTRAL_KEY="$2"
REALM_S3_ACCESS_KEY="$3"
REALM_S3_SECRET_KEY="$4"
DOCS_S3_ACCESS_KEY="$5"
DOCS_S3_SECRET_KEY="$6"
SLACK_WEBHOOK_RELEASES_URL="$7"
SLACK_WEBHOOK_JAVA_CI_URL="$8"
GRADLE_BUILD_PARAMS="$9"

abort_release() {
  # Reporting failures to #realm-java-team-ci is done from Jenkins
  exit 1
}

check_env() {
  echo "Checking environment..."

  # Try to find s3cmd
  path_to_s3cmd=$(type s3cmd)
  if [ -x "$path_to_s3cmd" ]
  then
    echo "Cannot find executable file 's3cmd'. Aborting."
    abort_release
  fi

  # Try to find git
  path_to_git=$(type git)
  if [ -x "$path_to_git" ]
  then
    echo "Cannot find executable file 'git'. Aborting."
    abort_release
  fi

  echo "Environment is OK."
}

verify_release_preconditions() {
  echo "Checking release branch..."
  gitTag=`git describe --tags | tr -d '[:space:]'`
  version=`cat $REALM_JAVA_PATH/version.txt | tr -d '[:space:]'`

  if [ "v$version" = "$gitTag" ]
  then
    RELEASE_VERSION=$version
    echo "Git tag and version.txt matches: $version. Continue releasing."
  else
    echo "Version in version.txt was '$version' while the branch was tagged with '$gitTag'. Aborting release."
    abort_release
  fi
}

verify_changelog() {
  echo "Checking CHANGELOG.md..."
  query="grep -c '^## $RELEASE_VERSION ([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])' $REALM_JAVA_PATH/CHANGELOG.md"

  if [ `eval $query` -ne 1 ]
  then
    echo "Changelog does not appear to be correct. First line should match the version being released and the date should be set. Aborting."
    abort_release
  else
    echo "CHANGELOG date and version is correctly set."
  fi
}

create_javadoc() {
  echo "Creating JavaDoc..."
  cd $REALM_JAVA_PATH
  eval "./gradlew javadoc $GRADLE_BUILD_PARAMS --stacktrace" 
  cd $HERE
}

upload_to_mavenCentral() {
  echo "Releasing on MavenCentral"
  cd $REALM_JAVA_PATH
  # FIXME: Closing and releasing doesn't currently work as the plugin doesn't handle uploads from multiple projects very well. So we might end up
  # with half a release. For this reason we must manually close and release the artifacts using the Maven Central UI:
  # https://oss.sonatype.org/#stagingRepositories
  # ./gradlew mavenCentralUpload closeAndReleaseStagingRepository -PossrhUsername=$MAVEN_CENTRAL_USER -PossrhPassword=$MAVEN_CENTRAL_KEY --stacktrace
  eval "./gradlew mavenCentralUpload $GRADLE_BUILD_PARAMS -PossrhUsername='$MAVEN_CENTRAL_USER' -PossrhPassword='$MAVEN_CENTRAL_KEY' --stacktrace"
  cd $HERE
}

upload_debug_symbols() {
  echo "Uploading native debug symbols..."
  cd $REALM_JAVA_PATH
  ./gradlew distribute -PREALM_S3_ACCESS_KEY=$REALM_S3_ACCESS_KEY -PREALM_S3_SECRET_KEY=$REALM_S3_SECRET_KEY
  cd $HERE
}

upload_javadoc() {
  echo "Uploading docs..."
  cd $REALM_JAVA_PATH
  ./gradlew uploadJavadoc -PSDK_DOCS_AWS_ACCESS_KEY=$DOCS_S3_ACCESS_KEY -PSDK_DOCS_AWS_SECRET_KEY=$DOCS_S3_SECRET_KEY
  cd $HERE
}

notify_slack_channels() {
  echo "Notifying Slack channels..."

  # Read first . Link is the value with ".",")","(" and space removed.
  command="grep '$RELEASE_VERSION' $REALM_JAVA_PATH/CHANGELOG.md | cut -c 4- | sed -e 's/[.)(]//g' | sed -e 's/ /-/g'"
  tag=`eval $command`
  if [ -z "$tag" ]
  then
    echo "\$tag did not resolve correctly. Aborting."
    abort_release
  fi
  current_commit=`git rev-parse HEAD`
  if [ -z "$current_commit" ]
  then
    echo "Could not find current commit. Aborting."
    abort_release
  fi

  link_to_changelog="https://github.com/realm/realm-java/blob/$current_commit/CHANGELOG.md#$tag"
  payload="{ \"username\": \"Realm CI\", \"icon_emoji\": \":realm_new:\", \"text\": \"<$link_to_changelog|*Realm Java $RELEASE_VERSION has been released*>\\nSee the Release Notes for more details. Note, it can take up to 10 minutes before the release is visible on Maven Central.\" }"
  echo $link_to_changelog
  echo "Pinging #realm-releases"
  curl -X POST --data-urlencode "payload=${payload}" ${SLACK_WEBHOOK_RELEASES_URL}
  echo "Pinging #realm-java-team-ci"
  curl -X POST --data-urlencode "payload=${payload}" ${SLACK_WEBHOOK_JAVA_CI_URL}
}

######################################
# Run Release steps
######################################\

check_env
verify_release_preconditions
verify_changelog

if [ "$1" != "verify" ]; then
  create_javadoc
  upload_to_mavenCentral
  upload_debug_symbols
  upload_javadoc
  notify_slack_channels
fi
