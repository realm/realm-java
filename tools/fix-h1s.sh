#!/bin/bash -ex

# Assume javadoc has been run
pushd "`dirname "$0"`"/../docs/html

find . -name "*.html" | while read ln
do
  # Make the output SEO friendly by converting the "h2" title to the proper "h1"
  sed -i -e 's|<h2\(.* class="title".*\)</h2>|<h1\1</h1>|' "$ln"
done
find . -iname "*.html-e" | xargs rm

popd
