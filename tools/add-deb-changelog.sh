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

now="$(date "+%a, %d %h %Y %T %z")" || exit 1
relman_user="$(git config --get user.name)" || exit 1
relman_mail="$(git config --get user.email)" || exit 1

# create a new entry in the debian changelog - in reverse order
tempfile="$(mktemp 'tightdb.XXXXXXXX')" || exit 1
printf "$package ($tightdb_version~@CODENAME@-1) UNRELEASED; urgency=low\n" >> "$tempfile" || exit 1
printf "\n" >> "$tempfile" || exit 1
printf "  * Tracking upstream release.\n" >> "$tempfile" || exit 1
printf "\n" >> "$tempfile" || exit 1
printf " -- $relman_user <$relman_mail>  $now\n" >> "$tempfile" || exit 1
printf "\n" >> "$tempfile" || exit 1
printf "\n" >> "$tempfile" || exit 1
temp_result_file="$(mktemp  'tightdb.XXXXXXXX')" || exit 1
cat "$tempfile" "$changelog" > "$temp_result_file" || exit 1
rm -f "$tempfile" || exit 1
mv "$temp_result_file" "$changelog" || exit 1
