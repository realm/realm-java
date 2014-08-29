from="<version>$1"
to="<version>$2"
if [ $# -eq 2 ]; then
  grep -r $from --include "*pom*.xml" .
  echo ' WARNING: This is certainly NOT foolproof. It will replace ANY occurence of the version given for ANY artifact.'
  echo 'Update all above matches?(y/n)'
  read answer
  if [ "$answer" == "y" ]; then
	grep -rl $from --include "*pom*.xml" . | xargs sed -i -e "s/$from/$to/g"
   	echo Updated.
  else
   	echo Skipped update.
  fi
else
	echo 'Updates all tightdb version number in all pom.xml files.'
	echo 'Usage: updateversion.sh old-versionnumber new-versionnumber'
	echo "      e.g. updateversion.sh '0.1.2' '0.1.3'"
	echo
fi
