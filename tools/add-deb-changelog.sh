# Do not edit here - go to core repository
# add an entry to the debian changelog template
# usage: add-deb-changelog.sh <version> <path-to-changelog.in> <package-name>
tightdb_version=$1
changelog=$2
package=$3

if [ -z "$tightdb_version" ]; then
    echo "No new version"
    exit 1
fi

if [ -z "$changelog" ]; then
    echo "No path to changelog.in"
    exit 1
fi

if [ -z "$package" ]; then
    echo "No package name"
    exit 1
fi

now="$(date --rfc-822)" || exit 1
relman_user="$(git config --get user.name)" || exit 1
relman_mail="$(git config --get user.email)" || exit 1

# create a new entry in the debian changelog - in reverse order
sed -s -i '1i\\' "$changelog" || exit 1
sed -s -i '1i\\' "$changelog" || exit 1
sed -i -e "1i\ -- $relman_user <$relman_mail>  $now" "$changelog" || exit 1
sed -s -i '1i\\' "$changelog" || exit 1
sed -i -e '1i\ \ * Tracking upstream release.' "$changelog" || exit 1
sed -s -i '1i\\' "$changelog" || exit 1
sed -i -e "1i $package ($tightdb_version~@CODENAME@-1) UNRELEASED; urgency=low" "$changelog" || exit 1
