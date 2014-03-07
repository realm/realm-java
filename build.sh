# NOTE: THIS SCRIPT IS SUPPOSED TO RUN IN A POSIX SHELL


# Enable tracing if TIGHTDB_SCRIPT_DEBUG is set
if [ -e $HOME/.tightdb ]; then
    . $HOME/.tightdb
fi
if [ "$TIGHTDB_SCRIPT_DEBUG" ]; then
    set -x
fi

ORIG_CWD="$(pwd)" || exit 1
ANDROID_DIR="android-lib"

cd "$(dirname "$0")" || exit 1
TIGHTDB_JAVA_HOME="$(pwd)" || exit 1

MODE="$1"
[ $# -gt 0 ] && shift


DEP_JARS="commons-io.jar commons-lang.jar freemarker.jar"

word_list_append()
{
    local list_name new_word list
    list_name="$1"
    new_word="$2"
    list="$(eval "printf \"%s\\n\" \"\${$list_name}\"")" || return 1
    if [ "$list" ]; then
        eval "$list_name=\"\$list \$new_word\""
    else
        eval "$list_name=\"\$new_word\""
    fi
    return 0
}

word_list_prepend()
{
    local list_name new_word list
    list_name="$1"
    new_word="$2"
    list="$(eval "printf \"%s\\n\" \"\${$list_name}\"")" || return 1
    if [ "$list" ]; then
        eval "$list_name=\"\$new_word \$list\""
    else
        eval "$list_name=\"\$new_word\""
    fi
    return 0
}

path_list_append()
{
    local list_name new_path list
    list_name="$1"
    new_path="$2"
    list="$(eval "printf \"%s\\n\" \"\${$list_name}\"")" || return 1
    if [ "$list" ]; then
        eval "$list_name=\"\$list:\$new_path\""
    else
        eval "$list_name=\"\$new_path\""
    fi
    return 0
}

remove_suffix()
{
    local string suffix match_x
    string="$1"
    suffix="$2"
    match_x="$(printf "%s" "$string" | tail -c "${#suffix}" && echo x)" || return 1
    if [ "$match_x" != "${suffix}x" ]; then
        printf "%s\n" "$string"
        return 0
    fi
    printf "%s" "$string" | sed "s/.\{${#suffix}\}\$//" || return 1
    echo
    return 0
}

readlink_f()
{
    local LINK TARGET
    LINK="$1"
    [ -e "$LINK" ] || return 1
    if ! TARGET="$(readlink "$LINK")"; then
        printf "%s\n" "$LINK"
        return 0
    fi
    # If TARGET is relative, then it must be preceeded by dirname of $LINK
    if ! printf "%s\n" "$TARGET" | grep -q '^/'; then
        DIR="$(dirname "$LINK")" || return 1
        if [ "$DIR" != "." ]; then
            TARGET="$DIR/$TARGET"
        fi
    fi
    readlink_f "$TARGET"
}

# Find the path of most recent version of the installed Android NDKs
find_android_ndk()
{
    local ndks ndks_index current_ndk latest_ndk sorted highest result

    ndks_index=0

    # If homebrew is installed...
    if [ -d "/usr/local/Cellar/android-ndk" ]; then
        ndks[$ndks_index]="/usr/local/Cellar/android-ndk"
        ((ndks_index = ndks_index + 1))
    fi
    if [ -d "/usr/local/android-ndk" ]; then
        ndks[$ndks_index]="/usr/local/android-ndk"
        ((ndks_index = ndks_index + 1))
    fi
    if [ "$ndks_index" -eq 0 ]; then
        return 1
    fi

    latest_ndk=""
    result=""
    for ndk in "${ndks[@]}"; do
        for i in $(cd "$ndk" && echo *); do
            if [ -f "$ndk/$i/RELEASE.TXT" ]; then
                current_ndk=$(sed 's/\(r\)\([1-9]\{1,\}\)\([a-z]\)/\1.\2.\3/' < "$ndk/$i/RELEASE.TXT") || return 1
                sorted="$(printf "%s\n%s\n" "$current_ndk" "$latest_ndk" | sort -t . -k 2,2nr -k 3,3r)" || return 1
                highest="$(printf "%s\n" "$sorted" | head -n 1)" || return 1
                if [ $current_ndk = $highest ]; then
                    result=$ndk/$i
                fi
            fi
        done
    done

    if [ -z $result ]; then
        return 1
    fi

    printf "%s\n" "$result"
}

# Setup OS specific stuff
OS="$(uname)" || exit 1
ARCH="$(uname -m)" || exit 1
MAKE="make"
NUM_PROCESSORS=""
if [ "$OS" = "Darwin" ]; then
    NUM_PROCESSORS="$(sysctl -n hw.ncpu)" || exit 1
elif [ -r "/proc/cpuinfo" ]; then
    NUM_PROCESSORS="$(cat /proc/cpuinfo | grep -E 'processor[[:space:]]*:' | wc -l)" || exit 1
fi
if [ "$NUM_PROCESSORS" ]; then
    word_list_prepend MAKEFLAGS "-j$NUM_PROCESSORS" || exit 1
    export MAKEFLAGS
fi


interactive_install_required_jar()
{
    local jar_name
    jar_name="$1"
    echo "jar file $jar_name is not installed."
    echo "Do you wish to install $jar_name (y/n)?"
    read answer
    if [ $(echo "$answer" | grep -c ^[Yy]) = 1 ]; then
        if [ "$OS" = "Darwin" ]; then
            sudo install -d /usr/local/share/java
            sudo install -m 644 prerequisite_jars/$jar_name /usr/local/share/java
        else
            echo "No interactive installation yet - sorry."
            exit 0
        fi
    else
        echo "SKIPPING: cannot proceed without $jar_name"
        exit 0
    fi
}


CONFIG_MK="tightdb_jni/config.mk"

require_config()
{
    cd "$TIGHTDB_JAVA_HOME" || return 1
    if ! [ -e "$CONFIG_MK" ]; then
        cat 1>&2 <<EOF
ERROR: Found no configuration!
You need to run 'sh build.sh config [PREFIX]'.
EOF
        return 1
    fi
    echo "Using existing configuration in $CONFIG_MK:"
    cat "$CONFIG_MK" | sed 's/^/    /' || return 1
}

auto_configure()
{
    cd "$TIGHTDB_JAVA_HOME" || return 1
    if [ -e "$CONFIG_MK" ]; then
        require_config || return 1
    else
        echo "No configuration found. Running 'sh build.sh config' for you."
        sh build.sh config || return 1
    fi
}

get_config_param()
{
    local name line value
    name="$1"
    cd "$TIGHTDB_JAVA_HOME" || return 1
    if ! [ -e "$CONFIG_MK" ]; then
        cat 1>&2 <<EOF
ERROR: Found no configuration!
You need to run 'sh build.sh config [PREFIX]'.
EOF
        return 1
    fi
    if ! line="$(grep "^$name *=" "$CONFIG_MK")"; then
        cat 1>&2 <<EOF
ERROR: Failed to read configuration parameter '$name'.
Maybe you need to rerun 'sh build.sh config [PREFIX]'.
EOF
        return 1
    fi
    value="$(printf "%s\n" "$line" | cut -d= -f2-)" || return 1
    value="$(printf "%s\n" "$value" | sed 's/^ *//')" || return 1
    printf "%s\n" "$value"
}


# Sets 'java_home', 'java_bindir', and 'java_cflags' on success
check_java_home()
{
    local cand bin inc arch found_jni_md_h os_lc
    cand="$1"
    if ! [ "$INTERACTIVE" ]; then
        echo "Checking '$cand' as candidate for JAVA_HOME"
    fi

    # Locate 'java' 'javac' and 'jni.h'
    bin=""
    inc=""
    if [ "$OS" = "Darwin" ]; then
        if [ -e "$cand/Commands/java" -a -e "$cand/Commands/javac" -a -e "$cand/Headers/jni.h" ]; then
            if ! [ "$INTERACTIVE" ]; then
                echo "Found 'Commands/java', 'Commands/javac' and 'Headers/jni.h' in '$cand'"
            fi
            bin="Commands"
            inc="Headers"
        else
            if ! [ "$INTERACTIVE" ]; then
                echo "Could not find 'Commands/java', 'Commands/javac' and 'Headers/jni.h' in '$cand'"
            fi
        fi
    fi
    if ! [ "$bin" ]; then
        if [ -e "$cand/bin/java" -a -e "$cand/bin/javac" -a -e "$cand/include/jni.h" ]; then
            if ! [ "$INTERACTIVE" ]; then
                echo "Found 'bin/java', 'bin/javac' and 'include/jni.h' in '$cand'"
            fi
            bin="bin"
            inc="include"
        else
            if ! [ "$INTERACTIVE" ]; then
                echo "Could not find 'bin/java', 'bin/javac' and 'include/jni.h' in '$cand'"
            fi
        fi
    fi

    # Do we need to add a platform dependent include directory?
    arch=""
    if [ "$inc" ]; then
        if [ -e "$cand/$inc/jni_md.h" ]; then
            if ! [ "$INTERACTIVE" ]; then
                echo "Found '$inc/jni_md.h' in '$cand'"
            fi
            found_jni_md_h="1"
        else
            os_lc="$(printf "%s\n" "$OS" | awk '{print tolower($0)}')" || return 1
            if [ -e "$cand/$inc/$os_lc/jni_md.h" ]; then
                if ! [ "$INTERACTIVE" ]; then
                    echo "Found '$inc/$os_lc/jni_md.h' in '$cand'"
                fi
                found_jni_md_h="1"
                arch="$os_lc"
            else
                if ! [ "$INTERACTIVE" ]; then
                    echo "Could not find '$inc/jni_md.h' or '$inc/$os_lc/jni_md.h' in '$cand'"
                fi
            fi
        fi
    fi

    if [ "$bin" ] && [ "$found_jni_md_h" ]; then
        java_home="$cand"
        java_bindir="$java_home/$bin"
        java_cflags="-I$java_home/$inc"
        if [ "$arch" ]; then
            java_cflags="$java_cflags -I$java_home/$inc/$arch"
        fi
    else
        if ! [ "$INTERACTIVE" ]; then
            echo "Skipping '$cand'"
        fi
    fi
    return 0
}

case "$MODE" in

    "config")
        install_prefix="$1"
        if ! [ "$install_prefix" ]; then
            install_prefix="auto"
        fi

        # install java when in interactive mode (Darwin only)
        if [ "$INTERACTIVE" ]; then
            if [ "$OS" = "Darwin" ]; then
                # FIXME: Use exit status of '/usr/libexec/java_home 2>/dev/null 1>&2' to test for presenece of Java
                # FIXME: Use '/usr/libexec/java_home --request 2>/dev/null 1>&2' to initiate asynchronous interactive installation of Java
                if ! java -version > /dev/null 2>&1 ; then
                    echo "It seems that Java is not installed."
                    echo "Do you wish to skip installation of the TightDB Java bindings (y/n)?"
                    read answer
                    if [ $(echo "$answer" | grep -c ^[yY]) = 1 ]; then
                        echo "Please consider to abort Java installation pop-up."
                        exit 0
                    else
                        echo "Press any key to continue when Java is installed."
                        read answer
                    fi
                fi
            fi
        fi

        java_home=""

        # Check JAVA_HOME when specified
        if ! [ "$java_home" ] && [ "$JAVA_HOME" ]; then
            if ! [ "$INTERACTIVE" ]; then
                echo "JAVA_HOME specified"
            fi
            check_java_home "$JAVA_HOME" || exit 1
        fi

        # On Darwin, check output of '/usr/libexec/java_home'
        if ! [ "$java_home" ] && [ "$OS" = "Darwin" ]; then
            # See also http://blog.hgomez.net/blog/2012/07/20/understanding-java-from-command-line-on-osx
            if ! [ -x "/usr/libexec/java_home" ]; then
                echo "ERROR: '/usr/libexec/java_home' not found or not executable" 1>&2
                exti 1
            fi
            # FIXME: Should we have added '-t JNI' to /usr/libexec/java_home?
            if path="$(/usr/libexec/java_home -v 1.6+ 2>/dev/null)"; then
                if ! [ "$INTERACTIVE" ]; then
                    echo "'/usr/libexec/java_home -v 1.6+' specifies a JAVA_HOME"
                fi
                check_java_home "$path" || exit 1
            fi
        fi

        # As a last resort, try to find 'javac' in PATH
        if ! [ "$java_home" ]; then
            if path="$(which javac 2>/dev/null)"; then
                path="$(readlink_f "$path")" || exit 1
                cand=""
                if [ "$OS" = "Darwin" ]; then
                    dir="$(remove_suffix "$path" "/Commands/javac")" || exit 1
                    if [ "$dir" != "$path" ]; then
                        cand="$dir"
                    fi
                fi
                if ! [ "$cand" ]; then
                    dir="$(remove_suffix "$path" "/bin/javac")" || exit 1
                    if [ "$dir" != "$path" ]; then
                        cand="$dir"
                    fi
                fi
                if ! [ "$cand" ]; then
                    echo "ERROR: Could not determine JAVA_HOME from path of 'javac' command '$path'" 1>&2
                    exit 1
                fi
                if ! [ "$INTERACTIVE" ]; then
                    echo "'javac' found in PATH as '$path'"
                fi
                check_java_home "$cand" || exit 1
            fi
        fi

        if ! [ "$java_home" ]; then
            echo "ERROR: No JAVA_HOME and no Java compiler in PATH" 1>&2
            exit 1
        fi

        java_cmd="$java_bindir/java"
        javac_cmd="$java_bindir/javac"

        if ! [ "$INTERACTIVE" ]; then
            echo "Examining Java command '$java_cmd'"
        fi
        min_ver_major="1"
        min_ver_minor="6"
        version="$($java_cmd -version 2>&1 | grep '^java version' | sed 's/.*"\(.*\)".*/\1/')"
        major="$(printf "%s\n" "$version" | cut -d. -f1)" || exit 1
        minor="$(printf "%s\n" "$version" | cut -d. -f2)" || exit 1
        if ! printf "%s\n" "$major" | grep -q '^[0-9][0-9]*$' || ! printf "%s\n" "$minor" | grep -q '^[0-9][0-9]*$'; then
            echo "ERROR: Could not determine Java version from '$version'" 1>&2
            exit 1
        fi
        if ! [ "$major" -gt "$min_ver_major" -o "$major" -eq "$min_ver_major" -a "$minor" -ge "$min_ver_minor" ] 2>/dev/null; then
            echo "ERROR: Need Java version $min_ver_major.$min_ver_minor or newer (is '$version')" 1>&2
            exit 1
        fi
        if ! [ "$INTERACTIVE" ]; then
            echo "Using Java command: $java_cmd (version $version)"
        fi
        java_version="$version"

        if [ "$install_prefix" = "auto" ]; then
            if [ "$OS" = "Darwin" ]; then
                jni_install_dir="/Library/Java/Extensions"
            else
                # We choose /usr/lib over /usr/local/lib because the
                # latter is not in the default runtime library search
                # path on RedHat and RedHat derived systems.
                jni_install_dir="$(cd "tightdb_jni" && NO_CONFIG_MK="1" $MAKE --no-print-directory prefix="/usr" get-libdir)" || exit 1
            fi
            jar_install_dir="/usr/local/share/java"
        else
            jni_install_dir="$(cd "tightdb_jni" && NO_CONFIG_MK="1" $MAKE --no-print-directory prefix="$install_prefix" get-libdir)" || exit 1
            jar_install_dir="$install_prefix/share/java"
        fi

        jar_dirs="/usr/local/share/java /usr/share/java"

        if [ "$OS" = "Darwin" ]; then
            jni_suffix=".jnilib"
            word_list_append jar_dirs "/Library/Java/Extensions" || exit 1
            word_list_append jar_dirs "/System/Library/Java/Extensions" || exit 1
        else
            jni_suffix=".so"
        fi

        required_jars=""
        for x in $DEP_JARS; do
            found=""
            for y in $jar_dirs; do
                path="$y/$x"
                if [ -e "$path" ]; then
                    found="1"
                    break
                fi
            done
            if ! [ "$found" ]; then
                if ! [ "$INTERACTIVE" ]; then
                    echo "ERROR: Could not find prerequisite JAR '$x'" 1>&2
                    exit 1
                else
                    interactive_install_required_jar "$x"
                    word_list_append "required_jars" "/usr/local/share/java/$x" || exit 1
                fi
            else
                word_list_append "required_jars" "$path" || exit 1
            fi
        done

        # For testing we need testng.jar. It, in turn, requires
        # qdox.jar and bsh.jar. Because some versions also require
        # beust-jcommander.jar, we add it if we can find it.
        testing_jars=""
        jars_paths=""
        found=""
        x="testng.jar"
        for y in $jar_dirs; do
            path="$y/$x"
            if [ -e "$path" ]; then
                found="1"
                break
            fi
        done
        if [ "$found" ]; then
            word_list_append "jar_paths" "$path" || exit 1
            for x in "qdox.jar" "bsh.jar"; do
                found=""
                for y in $jar_dirs; do
                    path="$y/$x"
                    if [ -e "$path" ]; then
                        found="1"
                        break;
                    fi
                done
                if ! [ "$found" ]; then
                    break
                fi
                word_list_append "jar_paths" "$path" || exit 1
            done
            if [ "$found" ]; then
                x="beust-jcommander.jar"
                for y in $jar_dirs; do
                    path="$y/$x"
                    if [ -e "$path" ]; then
                        word_list_append "jar_paths" "$path" || exit 1
                        break
                    fi
                done
                testing_jars="$jar_paths"
            fi
        fi

        # Find TightDB
        if [ -z "$TIGHTDB_CONFIG" ]; then
            TIGHTDB_CONFIG="tightdb-config"
        fi
        if printf "%s\n" "$TIGHTDB_CONFIG" | grep -q '^/'; then
            if ! [ -x "$TIGHTDB_CONFIG" ]; then
                echo "ERROR: TightDB config-program '$TIGHTDB_CONFIG' does not exist" 1>&2
                exit 1
            fi
            tightdb_config_cmd="$TIGHTDB_CONFIG"
        elif ! tightdb_config_cmd="$(which "$TIGHTDB_CONFIG" 2>/dev/null)"; then
            echo "ERROR: TightDB config-program '$TIGHTDB_CONFIG' not found in PATH" 1>&2
            exit 1
        fi
        tightdb_config_dbg_cmd="$tightdb_config_cmd-dbg"
        if ! [ -x "$tightdb_config_dbg_cmd" ]; then
            echo "ERROR: TightDB config-program '$tightdb_config_dbg_cmd' not found" 1>&2
            exit 1
        fi
        tightdb_version="$($tightdb_config_cmd --version)" || exit 1

        tightdb_cflags="$($tightdb_config_cmd --cflags)"         || exit 1
        tightdb_cflags_dbg="$($tightdb_config_dbg_cmd --cflags)" || exit 1
        tightdb_ldflags="$($tightdb_config_cmd --libs)"          || exit 1
        tightdb_ldflags_dbg="$($tightdb_config_dbg_cmd --libs)"  || exit 1

        tightdb_includedir="$($tightdb_config_cmd --includedir)" || exit 1
        tightdb_libdir="$($tightdb_config_cmd --libdir)"         || exit 1
        tightdb_rpath="$tightdb_libdir"

        # `TIGHTDB_DIST_INCLUDEDIR` and `TIGHTDB_DIST_LIBDIR` are set
        # when configuration occurs in the context of a distribution
        # package.
        if [ "$TIGHTDB_DIST_INCLUDEDIR" ] && [ "$TIGHTDB_DIST_LIBDIR" ]; then
            tightdb_includedir="$TIGHTDB_DIST_INCLUDEDIR"
            tightdb_libdir="$TIGHTDB_DIST_LIBDIR"
        else
            tightdb_includedir="$($tightdb_config_cmd --includedir)" || exit 1
            tightdb_libdir="$($tightdb_config_cmd --libdir)"         || exit 1
        fi
        tightdb_rpath="$($tightdb_config_cmd --libdir)" || exit 1

        cflags="-I$tightdb_includedir"
        ldflags="-L$tightdb_libdir -Wl,-rpath,$tightdb_rpath"
        word_list_prepend "tightdb_cflags"      "$cflags"  || exit 1
        word_list_prepend "tightdb_cflags_dbg"  "$cflags"  || exit 1
        word_list_prepend "tightdb_ldflags"     "$ldflags" || exit 1
        word_list_prepend "tightdb_ldflags_dbg" "$ldflags" || exit 1

        # Find Android NDK
        if [ "$NDK_HOME" ]; then
            ndk_home="$NDK_HOME"
        else
            ndk_home="$(find_android_ndk)" || ndk_home="none"
        fi

        android_core_lib="none"
        if [ "$TIGHTDB_ANDROID_CORE_LIB" ]; then
            android_core_lib="$TIGHTDB_ANDROID_CORE_LIB"
            if ! printf "%s\n" "$android_core_lib" | grep -q '^/'; then
                android_core_lib="$ORIG_CWD/$android_core_lib"
            fi
        elif [ -e "../tightdb/build.sh" ]; then
            path="$(cd "../tightdb" || return 1; pwd)" || exit 1
            android_core_lib="$path/$ANDROID_DIR"
        else
            tightdb_echo "Could not find home of TightDB core library built for Android"
        fi

        cat >"$CONFIG_MK" <<EOF
INSTALL_PREFIX      = $install_prefix
JAVA_COMMAND        = $java_cmd
JAVAC_COMMAND       = $javac_cmd
JAVA_VERSION        = $java_version
JAVA_CFLAGS         = $java_cflags
REQUIRED_JARS       = $required_jars
TESTING_JARS        = $testing_jars
JNI_INSTALL_DIR     = $jni_install_dir
JAR_INSTALL_DIR     = $jar_install_dir
JNI_SUFFIX          = $jni_suffix
TIGHTDB_CONFIG      = $tightdb_config_cmd
TIGHTDB_VERSION     = $tightdb_version
TIGHTDB_CFLAGS      = $tightdb_cflags
TIGHTDB_CFLAGS_DBG  = $tightdb_cflags_dbg
TIGHTDB_LDFLAGS     = $tightdb_ldflags
TIGHTDB_LDFLAGS_DBG = $tightdb_ldflags_dbg
NDK_HOME            = $ndk_home
ANDROID_CORE_LIB    = $android_core_lib
EOF
        if ! [ "$INTERACTIVE" ]; then
            echo "New configuration in $CONFIG_MK:"
            cat "$CONFIG_MK" | sed 's/^/    /' || exit 1
            echo "Done configuring"
        fi
        exit 0
        ;;

    "install-report")
        has_installed=0
        jni_install_dir="$(get_config_param "JNI_INSTALL_DIR")"
        jar_install_dir="$(get_config_param "JAR_INSTALL_DIR")"
        find $jni_install_dir -name '*tight*jni*' | while read f; do
            has_installed=1
            echo "  $f"
        done
        find $jar_install_dir -name '*tightdb*jar' | while read f; do
            has_installed=1
            echo "  $f"
        done
        exit $has_installed
        ;;

    "get-version")
        version_file="pom.xml"
        tightdb_version="$(grep "<version>" pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')"
        printf "%s\n" $tightdb_version
        exit 0
        ;;

    "set-version")
        tightdb_version="$1"
        cur_tightdb_version="$(sh build.sh get-version)"

        sh updateversion.sh "$cur_tightdb_version" $tightdb_version || exit 1
        sh tools/add-deb-changelog.sh "$tightdb_version" "$(pwd)/debian/changelog.in" tightdb-java || exit 1
        exit 0
        ;;

    "clean")
        auto_configure || exit 1
        $MAKE -C "tightdb_jni" clean || exit 1
        for x in core generator test; do
            echo "Removing class files in 'tightdb-java-$x'"
            (cd "tightdb-java-$x" && find src/ -type f -name '*.class' -delete) || exit 1
        done
        if [ -e "lib" ]; then
            for x in tightdb.jar tightdb-devkit.jar; do
                echo "Removing 'lib/$x'"
                rm -f "lib/$x" || exit 1
            done
            echo "Removing library symlinks for examples from 'lib'"
            rm -f "lib/"* || exit 1
            rmdir "lib" || exit 1
        fi
        if [ -e "$ANDROID_DIR" ]; then
            rm -rf "$ANDROID_DIR" || exit 1
        fi
        echo "Done cleaning"
        exit 0
        ;;

    "build-jars")
        mkdir -p "lib" || exit 1
        javac_cmd="$(get_config_param "JAVAC_COMMAND")" || exit 1

        # Build tightdb.jar
        echo "Building 'lib/tightdb.jar'"
        dir="tightdb-java-core/src/main"
        tightdb_jar="$TIGHTDB_JAVA_HOME/lib/tightdb.jar"
        (cd "$dir/java" && $javac_cmd            com/tightdb/*.java com/tightdb/internal/*.java  com/tightdb/typed/*.java)   || exit 1
        (cd "$dir/java" && jar cf "$tightdb_jar" com/tightdb/*.class com/tightdb/internal/*.class com/tightdb/typed/*.class) || exit 1
        (cd "lib" && jar i "tightdb.jar") || exit 1

        # Build tightdb-devkit.jar
        echo "Building 'lib/tightdb-devkit.jar'"
        required_jars="$(get_config_param "REQUIRED_JARS")" || exit 1
        manifest_jars="$required_jars"
        word_list_prepend "manifest_jars" "tightdb.jar" || exit 1
        temp_dir="$(mktemp -d /tmp/tightdb.java.build.XXXX)" || exit 1
        manifest="$temp_dir/MANIFEST.MF"
        touch "$manifest" || exit 1
        echo "Class-Path: $manifest_jars" >>"$manifest"
        echo "MANIFEST.MF constains:"
        cat "$manifest" | sed 's/^/    /' || exit 1
        CLASSPATH="$(printf "%s\n" "$required_jars" | sed 's/  */:/g')" || exit 1
        path_list_append CLASSPATH "$tightdb_jar" || exit 1
        export CLASSPATH
        dir="tightdb-java-generator/src/main"
        # FIXME: Must run ResourceGenerator to produce "$dir/java/com/tightdb/generator/Templates.java"
        devkit_jar="$TIGHTDB_JAVA_HOME/lib/tightdb-devkit.jar"
        (cd "$dir" && jar cfm "$devkit_jar" "$manifest" -C resources META-INF) || exit 1
        (cd "$dir/java" && $javac_cmd           com/tightdb/generator/*.java)  || exit 1
        (cd "$dir/java" && jar uf "$devkit_jar" com/tightdb/generator/*.class) || exit 1
        (cd "lib" && jar i "tightdb-devkit.jar") || exit 1
        exit 0
        ;;

    "build-jni")
        mkdir -p "lib" || exit 1

        # FIXME: Consider whether we should call `javah
        # PACKAGE_QUALIFIED_CLASSES` here to recreate the JNI header
        # files before building the JNI library.

        # Build libtightdb-jni.so
        TIGHTDB_ENABLE_FAT_BINARIES="1" $MAKE -C "tightdb_jni" || exit 1

        # Setup links to libraries to make the examples work
        echo "Setting up library symlinks in 'lib' to make examples work"
        if [ "$TIGHTDB_HOME" ]; then
            library_aliases="$(cd "$TIGHTDB_HOME/src/tightdb" && $MAKE --no-print-directory get-inst-libraries)" || exit 1
            for x in $library_aliases; do
                (cd "lib" && ln -s -f "$TIGHTDB_HOME/src/tightdb/$x") || exit 1
            done
        fi
        library_aliases="$(cd "tightdb_jni/src" && $MAKE --no-print-directory get-inst-libraries)" || exit 1
        for x in $library_aliases; do
            (cd "lib" && ln -s -f "../tightdb_jni/src/$x") || exit 1
        done
        exit 0
        ;;

    "build")
        auto_configure || exit 1

        sh build.sh build-jars || exit 1
        sh build.sh build-jni || exit 1

        if ! [ "$INTERACTIVE" ]; then
            echo "Done building"
        fi
        exit 0
        ;;

    "build-android")
        auto_configure || exit 1
        ndk_home="$(get_config_param "NDK_HOME")" || exit 1
        if [ "$ndk_home" = "none" ]; then
            cat 1>&2 <<EOF
ERROR: No Android NDK was found.
Please do one of the following:
 * Install an NDK in /usr/local/android-ndk
 * Provide the path to the NDK in the environment variable NDK_HOME
 * If on OSX and using Homebrew install the package android-sdk
EOF
            exit 1
        fi
        mkdir -p "$ANDROID_DIR" || exit 1
        OLDPATH=$PATH
        for target in "arm" "arm-v7a" "mips" "x86"; do
            temp_dir="$(mktemp -d /tmp/tightdb.build-android.XXXX)" || exit 1
            if [ "$target" = "arm" ]; then
                platform=8
            else
                platform=9
            fi
            $ndk_home/build/tools/make-standalone-toolchain.sh --platform=android-$platform --install-dir=$temp_dir --arch=$target || exit 1
            export PATH=$temp_dir/bin:$OLDPATH
            if [ "$target" = "arm" ]; then
                android_prefix="arm"
                subfolder="armeabi"
            elif [ "$target" = "arm-v7a" ]; then
                android_prefix="arm"
                subfolder="armeabi-v7a"
            elif [ "$target" = "mips" ]; then
                android_prefix="mipsel"
                subfolder="$target"
            elif [ "$target" = "x86" ]; then
                android_prefix="i686"
                subfolder="$target"
            fi
            export CXX="$(cd "$temp_dir/bin" && echo $android_prefix-linux-*-gcc)"
            export LD="$(cd "$temp_dir/bin" && echo $android_prefix-linux-*-g++)"
            extra_cflags="-DANDROID -Os"
            if [ "$target" = "arm" ]; then
                extra_cflags="$extra_cflags -mthumb"
            elif [ "$target" = "arm-v7a" ]; then
                extra_cflags="$extra_cflags -mthumb -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16"
            fi
            denom="android-$target"
            android_core_lib="$(get_config_param "ANDROID_CORE_LIB")" || exit 1
            android_ldflags="-ltightdb-$denom -L$android_core_lib -shared"
            $MAKE -C "tightdb_jni/src" "libtightdb-jni-$denom.so" BASE_DENOM="$denom" CFLAGS_ARCH="$extra_cflags" TIGHTDB_LDFLAGS="$android_ldflags" TIGHTDB_LDFLAGS_DBG="$android_ldflags" LIB_SUFFIX_SHARED=".so" || exit 1
            mkdir -p "$ANDROID_DIR/$subfolder" || exit 1
            cp "tightdb_jni/src/libtightdb-jni-$denom.so" "$ANDROID_DIR/$subfolder/libtightdb-jni.so" || exit 1
            rm -rf $temp_dir
        done
        sh build.sh build-jars || exit 1
        exit 0
        ;;

    "android-package")
        if ! [ -e "$ANDROID_DIR/x86" ]; then
            cat 1>&2 <<EOF
ERROR: No TightDB core files found.
Make sure to run "sh build.sh build-android"
EOF
            exit 1
        fi
        if ! [ -e "lib/tightdb.jar" -a -e "lib/tightdb-devkit.jar" ]; then
            cat 1>&2 <<EOF
ERROR: No jar files found.
Make sure to run "sh build.sh build-android"
EOF
            exit 1
        fi
        tightdb_version="$(sh build.sh get-version)"
        if [ -e "lib/tightdb-android-$tightdb_version.zip" ]; then
            rm -f "lib/tightdb-android-$tightdb_version.zip" || exit 1
        fi
        cp "lib/tightdb.jar" "$ANDROID_DIR" || exit 1
        cp "lib/tightdb-devkit.jar" "$ANDROID_DIR" || exit 1
        (cd "$ANDROID_DIR" && zip -r "../lib/tightdb-android-$tightdb_version.zip" "armeabi" "armeabi-v7a" "mips" "x86" "tightdb.jar" "tightdb-devkit.jar") || exit 1
        ;;

    "test")
        require_config || exit 1
        javac_cmd="$(get_config_param "JAVAC_COMMAND")" || exit 1
        devkit_jar="$TIGHTDB_JAVA_HOME/lib/tightdb-devkit.jar"
        temp_dir="$(mktemp -d /tmp/tightdb.java.test-debug.XXXX)" || exit 1
        mkdir "$temp_dir/out" || exit 1
        mkdir "$temp_dir/gen" || exit 1

        dir="tightdb-java-test/src/test"
        echo "Building test suite in '$dir'"
        export CLASSPATH="$devkit_jar:$temp_dir/gen:."
        (cd "$dir/../test/java" && $javac_cmd -d "$temp_dir/out" -s "$temp_dir/gen" com/tightdb/test/TestTableModel.java) || exit 1

        path_list_append "CLASSPATH" "../main" || exit 1
        testing_jars="$(get_config_param "TESTING_JARS")" || exit 1
        for x in $testing_jars; do
            path_list_append "CLASSPATH" "$x" || exit 1
        done
        sources="$(cd "$dir/java" && find * -type f -name '*.java')" || exit 1
        classes="$(printf "%s\n" "$sources" | sed 's/\.java$/.class/')" || exit 1
        (cd "$dir/java" && $javac_cmd -d "$temp_dir/out" -s "$temp_dir/gen" $sources) || exit 1

        echo "Running test suite in '$dir'"
        testng_xml="$temp_dir/testng.xml"
        cat >"$testng_xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="Test suite" verbose="1"><test name="All"><classes>
EOF
        for x in $classes; do
            y="$(printf "%s\n" "$x" | sed 's/\.class$//' | sed 's|/|.|g')"
            cat >>"$testng_xml" <<EOF
<class name="$y"/>
EOF
        done
        cat >>"$testng_xml" <<EOF
</classes></test></suite>
EOF
        java_cmd="$(get_config_param "JAVA_COMMAND")" || exit 1
        (cd "$temp_dir/out" && $java_cmd -Djava.library.path="$TIGHTDB_JAVA_HOME/tightdb_jni/src" org.testng.TestNG -d "$TIGHTDB_JAVA_HOME/test_output" "$testng_xml") || exit 1
        exit 0
        ;;

    "test-debug")
        TIGHTDB_JAVA_DEBUG="1" sh build.sh test || exit 1
        exit 0
        ;;

    "test-doc")
        echo "Testing ref-doc:"
        rm *.tightdb
        rm *.lock
        cd "doc/ref/examples" || exit 1
        ant refdoc || exit 1
        echo "Test passed"
        exit 0
        ;;

    "test-examples")
        echo "Testing intro examples:"
        cd "examples/intro-example" || exit 1
        ant runall || exit 1
        exit 0
        ;;

    "show-install")
        temp_dir="$(mktemp -d /tmp/tightdb.java.show-install.XXXX)" || exit 1
        mkdir "$temp_dir/fake-root" || exit 1
        DESTDIR="$temp_dir/fake-root" sh build.sh install >/dev/null || exit 1
        (cd "$temp_dir/fake-root" && find * \! -type d >"$temp_dir/list") || exit 1
        sed 's|^|/|' <"$temp_dir/list" || exit 1
        rm -fr "$temp_dir/fake-root" || exit 1
        rm "$temp_dir/list" || exit 1
        rmdir "$temp_dir" || exit 1
        exit 0
        ;;

    "install"|"install-devel"|"install-prod")
        require_config || exit 1

        install_jni=""
        case "$MODE" in
            "install-devel")
                jar_list="tightdb-devkit.jar"
                ;;
            "install-prod")
                jar_list="tightdb.jar"
                install_jni="yes"
                ;;
            *)
                jar_list="tightdb-devkit.jar tightdb.jar"
                install_jni="yes"
                ;;
        esac

        jar_install_dir="$DESTDIR$(get_config_param "JAR_INSTALL_DIR")" || exit 1
        install -d "$jar_install_dir" || exit 1

        for x in $jar_list; do
            echo "Installing '$jar_install_dir/$x'"
            install -m 644 "lib/$x" "$jar_install_dir" || exit 1
        done

        if [ "$install_jni" ]; then
            $MAKE -C "tightdb_jni" install-only DESTDIR="$DESTDIR" || exit 1
        fi

        if ! [ "$INTERACTIVE" ]; then
            echo "Done installing"
        fi
        exit 0
        ;;

    "uninstall")
        require_config || exit 1

        jar_install_dir="$(get_config_param "JAR_INSTALL_DIR")" || exit 1
        for x in "tightdb-devkit.jar" "tightdb.jar"; do
            echo "Uninstalling '$jar_install_dir/$x'"
            rm -f "$jar_install_dir/$x" || exit 1
        done

        $MAKE -C "tightdb_jni" uninstall || exit 1

        echo "Done uninstalling"
        exit 0
        ;;

    "test-installed")
        require_config || exit 1

        jar_install_dir="$(get_config_param "JAR_INSTALL_DIR")" || exit 1
        devkit_jar="$jar_install_dir/tightdb-devkit.jar"
        export CLASSPATH="$devkit_jar:."
        javac_cmd="$(get_config_param "JAVAC_COMMAND")" || exit 1
        temp_dir="$(mktemp -d /tmp/tightdb.java.test-installed.XXXX)" || exit 1
        (cd "test-installed" && $javac_cmd -d "$temp_dir" -s "$temp_dir" java/my/app/Test.java) || exit 1

        install_prefix="$(get_config_param "INSTALL_PREFIX")" || exit 1
        java_cmd="$(get_config_param "JAVA_COMMAND")"         || exit 1
        if [ "$install_prefix" != "auto" ]; then
            jni_install_dir="$(get_config_param "JNI_INSTALL_DIR")" || exit 1
            java_cmd="$java_cmd -Djava.library.path=$jni_install_dir"
        fi
        (cd "$temp_dir" && $java_cmd my.app.Test) || exit 1
        (cd "$temp_dir" && TIGHTDB_JAVA_DEBUG=1 $java_cmd my.app.Test) || exit 1

        echo "Test passed"
        exit 0
        ;;

    "dist-copy")
        # Copy to distribution package
        TARGET_DIR="$1"
        if ! [ "$TARGET_DIR" -a -d "$TARGET_DIR" ]; then
            echo "Unspecified or bad target directory '$TARGET_DIR'" 1>&2
            exit 1
        fi
        TEMP_DIR="$(mktemp -d /tmp/tightdb.java.copy.XXXX)" || exit 1
        cat >"$TEMP_DIR/include" <<EOF
/README.md
/build.sh
/tightdb_jni/generic.mk
/tightdb_jni/project.mk
/tightdb_jni/Makefile
/tightdb_jni/src
/tightdb-java-core
/tightdb-java-generator
/tightdb-java-test
/test-installed
/examples
/prerequisite_jars
/debian/changelog.in
/debian/rules
/debian/compat
/debian/control
/debian/copyright
/debian/tightdb-java-dev.poms
/pom-debian.xml
/pom.xml
EOF
        cat >"$TEMP_DIR/exclude" <<EOF
.gitignore
/tightdb-java-generator/pom.xml
/tightdb-java-test/pom.xml
/tightdb-java-test/src/test/resources
*.dll
EOF
        grep -E -v '^(#.*)?$' "$TEMP_DIR/include" >"$TEMP_DIR/include2" || exit 1
        grep -E -v '^(#.*)?$' "$TEMP_DIR/exclude" >"$TEMP_DIR/exclude2" || exit 1
        sed -e 's/\([.\[^$]\)/\\\1/g' -e 's|\*|[^/]*|g' -e 's|^\([^/]\)|^\\(.*/\\)\\{0,1\\}\1|' -e 's|^/|^|' -e 's|$|\\(/.*\\)\\{0,1\\}$|' "$TEMP_DIR/include2" >"$TEMP_DIR/include.bre" || exit 1
        sed -e 's/\([.\[^$]\)/\\\1/g' -e 's|\*|[^/]*|g' -e 's|^\([^/]\)|^\\(.*/\\)\\{0,1\\}\1|' -e 's|^/|^|' -e 's|$|\\(/.*\\)\\{0,1\\}$|' "$TEMP_DIR/exclude2" >"$TEMP_DIR/exclude.bre" || exit 1
        git ls-files >"$TEMP_DIR/files1" || exit 1
        grep -f "$TEMP_DIR/include.bre" "$TEMP_DIR/files1" >"$TEMP_DIR/files2" || exit 1
        grep -v -f "$TEMP_DIR/exclude.bre" "$TEMP_DIR/files2" >"$TEMP_DIR/files3" || exit 1
        tar czf "$TEMP_DIR/archive.tar.gz" -T "$TEMP_DIR/files3" || exit 1
        (cd "$TARGET_DIR" && tar xzmf "$TEMP_DIR/archive.tar.gz") || exit 1
        if ! [ "$TIGHTDB_DISABLE_MARKDOWN_TO_PDF" ]; then
            (cd "$TARGET_DIR" && pandoc README.md -o README.pdf) || exit 1
        fi
        exit 0
        ;;

    "dist-deb")
        codename=$(lsb_release -s -c)
        (cd debian && sed -e "s/@CODENAME@/$codename/g" changelog.in > changelog) || exit 1
        dpkg-buildpackage -rfakeroot -us -uc || exit 1
        exit 0
        ;;

    "dist-remarks")
cat <<EOF
To help you get started, a simple example is provided in
"tightdb_java/examples/intro-example". First you need to build the
Java extension as described above. Do NOT run "./build clean", as that
will prevent the example from running. For further details, please
consult the README.md file in mentioned directory.
EOF
        exit 0
        ;;

    *)
        echo "Unspecified or bad mode '$MODE'" 1>&2
        echo "Available modes are: config clean build build-android android-package test test-debug test-doc test-examples show-install install uninstall test-installed" 1>&2
        echo "As well as: dist-copy dist-remarks" 1>&2
        exit 1
        ;;

esac
