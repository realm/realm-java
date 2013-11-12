from="<version>$1"
to="<version>$2"
if [ $# -eq 2 ]; then

  	grep -r $from */pom.xml
  	echo 'Update all above matches?(y/n)'
  	read answer
  	if [ "$answer" == "y" ]; then
		grep -rl $from */pom.xml | xargs sed -i -e "s/$from/$to/g"
    	echo Updated.
    else
    	echo Skipped update.
    fi
else
	echo 'Updates all tightdb version number in all pom.xml files.'
	echo 'Usage: updatepomversion.sh old-versionnumber new-versionnumber'
	echo '      e.g. updatepomversion.sh 0.1.2 0.1.3'
	echo        
fi