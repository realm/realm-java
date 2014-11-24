#!/bin/bash
#
# This script packages, tests and deploys a release of Realm-Android.
# For manual steps, see Asana release template.
#

set -e

if [ $# -ne 2 ] ; then
    echo "Usage: sh ./deploy-release.sh <old-version> <version>"
    echo "Version could eg be \"0.74.1\""
    exit 1
fi

old_realm_version=$1
new_realm_version=$2
release_branch=ci-release-${new_realm_version}
doc_release_branch=ci-android-release-${new_realm_version}
tag=v${new_realm_version}

# Checks if the repository has any outstanding changes
function check_clean_repo () {
	if [ -n "$(git status --porcelain)" ]; then
	  echo "FAILURE: $1 repository has uncommitted changes. Please make sure they are stashed or commited before continuing"
	  exit 1
	fi
}

# Fast forward repository master to newest version or fails if it is not possible
function update_master () {
	git checkout master

	LOCAL=$(git rev-parse @)
	REMOTE=$(git rev-parse @{u})
	BASE=$(git merge-base @ @{u})

	if [ $LOCAL = $REMOTE ]; then
	    echo "Up-to-date"
	elif [ $LOCAL = $BASE ]; then
	    echo "Need to pull"
        git pull
	else
	    echo "Cannot continue automatically. Manually make sure that master is up-to-date. Then run script again."
	fi
}

# Cleanup branches from a previous release of the same version that failed for some reason
function cleanup_failed_release () {
	set +e
	git show-ref --verify --quiet refs/heads/$1
	if [ $? -eq 0 ] ; then
		git branch -D $1
	fi

	if [ $# -eq 2 ] ; then
		git rev-parse ${tag} --
		if [ $? -eq 0 ]; then
			git tag -d ${tag}
		fi
	fi
	set -e
}

echo ""
echo "Veryfying that repositories are up to date."
echo ""

# Make sure we are in root folder
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${script_dir}/.."

# Prepare realm-java repositorty
check_clean_repo "realm-java"
update_master
cleanup_failed_release ${release_branch} ${tag}

# Prepare realm.io repository
if [ ! -d "../realm.io" ]; then
  echo "Could not find the realm.io project. It should be in the same folder as realm-java."
  exit 1
fi
cd ../realm.io
check_clean_repo "realm.io"
update_master
cleanup_failed_release ${doc_release_branch}
cd -

echo ""
echo "Starting deploy of Realm v.${new_realm_version}"
echo ""

echo "Updating version numbers from ${old_realm_version} to ${new_realm_version}"
echo ""

# Create new release branch
git checkout -b ${release_branch}

# Update version numbers
printf ${new_realm_version} > ./version.txt
sed -i.bak "s/${old_realm_version}/${new_realm_version}/" ./realm-annotations-processor/src/main/java/io/realm/processor/RealmVersionChecker.java
rm ./realm-annotations-processor/src/main/java/io/realm/processor/RealmVersionChecker.java.bak

for dist in ./distribution/Realm*/ ; do
	sed -i.bak "s/io.realm:realm-android:${old_realm_version}/io.realm:realm-android:${new_realm_version}/" ${dist}/app/build.gradle
	rm ${dist}/app/build.gradle.bak
done

# Update date in changelog.txt
sed -i.bak "s/^${new_realm_version}.*$/${new_realm_version} ($(date +'%d %b %Y'))/g" ./changelog.txt
rm ./changelog.txt.bak

# Test example projects
result_code=$(sh ./tools/monkey-examples.sh)
if [ "${result_code}" != "0" ] ; then
	echo "Aborting release"
	exit 1
fi

# Upgrading version number complete, check into release branch and push to GitHub
git add .
git commit -m "Updated version number to ${new_realm_version}"
git tag -a ${tag} -m "Tag release v${new_realm_version}"
git push origin ${release_branch}:${release_branch}
git push origin ${tag}

echo ""
echo "Release branch has been pushed to GitHub. Make a manual pullrequest + merge."
echo ""

# Prepare distribution zip file
sh ./build-distribution.sh
dist_foldername=realm-java-${new_realm_version}
dist_filename=realm-java-${new_realm_version}.zip
cp -R ./distribution ./build/${dist_foldername}
cd ./build
zip -r ${dist_filename} ./${dist_foldername}
cd -

echo ""
echo "Distribution zip file ready in realm-java/build/${dist_filename}"
echo ""


# Upload to binTray and release
./gradlew realm:bintrayUpload

echo ""
echo "Artifacts uploaded to BinTray. Remember to manually release them."
echo ""


# Test distribution projects
result_code=$(sh ./tools/monkey-distribution.sh)
if [ "${result_code}" != "0" ] ; then
	echo "Aborting release"
	exit 1
fi

# Create new documentation
# This assumes that realm-java and realm-io are located in the same folder
echo ""
echo "Creating JavaDoc for ${new_realm_version}"
echo ""
./gradlew clean realm:generateReleaseJavadoc

echo ""
echo "Preparing new version of the homepage docs"
echo ""

cd ../realm.io

# Create release branch
git checkout -b ${doc_release_branch}

# Update documentation with new version
cp ./docs/java/${old_realm_version}/index.md ./docs/java/${new_realm_version}/index.md
sed -i.bak "s/${old_realm_version}/${new_realm_version}/g" ./docs/java/${new_realm_version}/index.md
rm ./docs/java/${new_realm_version}/index.md.bak

# Copy JavaDoc into place
cp -R ../realm-java/realm/build/docs/javadoc/ ./docs/java/${new_realm_version}/api/

# Rewire redirects in _config.yml
old_doc_redirect="java: /docs/java/${old_realm_version}/"
new_doc_redirect="java: /docs/java/${new_realm_version}/"
old_dist_redirect="java: http://static.realm.io/downloads/java/realm-java-${old_realm_version}.zip"
new_dist_redirect="java: http://static.realm.io/downloads/java/realm-java-${new_realm_version}.zip"

sed -i.bak "s?${old_doc_redirect}?${new_doc_redirect}?" ./_config.yml
rm ./_config.yml.bak
sed -i.bak "s?${old_dist_redirect}?${new_dist_redirect}?" ./_config.yml
rm ./_config.yml.bak

# Add new documentation to GitHub
git add .
git commit -m "Added documentation for Realm-Java v${new_realm_version}"
git push origin ${doc_release_branch}:${doc_release_branch}

echo ""
echo "Documentation has been pushed to GitHub. Make a manual pull request and merge."
echo ""

read -p "Deploy distribution file to S3 and update links pr. release template, then press [Enter] key to continue..."
# TODO Upload distribution files to static.realm.io
# TODO Rewire links on static.realm.io

echo "Realm ${new_realm_version} has been deployed. Grab a beer!"