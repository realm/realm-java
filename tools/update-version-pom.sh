#!/bin/sh
# update version number in POM file
cur_tightdb_version="$1"
tightdb_version="$2"
pomfile="$3"

tmpfile=$(mktemp /tmp/$$.XXXXXX)
found=0
cat $pomfile | while IFS='' read -r line ; do
    if [ $found == 1 ]; then
        newline=$(echo "$line" | sed -e "s/$cur_tightdb_version/$tightdb_version/g")
        echo "$newline" >> $tmpfile
        found=0
    else
        echo "$line" >> $tmpfile
    fi
    if [ "$(echo "$line" | grep -c "artifactId>tightdb")" == 1 ]; then
        found=1
    fi
done
mv $tmpfile $pomfile
rm -f $tmpfile
