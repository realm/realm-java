#!/bin/bash -ex

usage() {
cat <<EOF
Usage: $0 <javadoc-root-dir>
EOF
}

if [ "$#" -ne 1 ] ; then
  usage
  exit 1
fi

# Assume Dokka has been run
pushd $1

find . -name "*.html" | while read ln
do
  # Make the output SEO friendly by converting the "h2" title to the proper "h1"
  sed -i -e 's|<h2\(.* class="title".*\)</h2>|<h1\1</h1>|' "$ln"
done
find . -iname "*.html-e" | xargs rm || true

popd
