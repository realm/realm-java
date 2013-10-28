# NOTE: THIS SCRIPT IS SUPPOSED TO RUN IN A POSIX SHELL

cd "$(dirname "$0")"
TIGHTDB_JAVA_HOME="$(pwd)"

MODE="$1"
[ $# -gt 0 ] && shift

DEP_JARS="commons-io.jar commons-lang.jar freemarker.jar"

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



# Setup OS specific stuff
OS="$(uname)" || exit 1
ARCH="$(uname -m)" || exit 1
STAT_FORMAT_SWITCH="-c"
NUM_PROCESSORS=""
if [ "$OS" = "Darwin" ]; then
    STAT_FORMAT_SWITCH="-f"
    NUM_PROCESSORS="$(sysctl -n hw.ncpu)" || exit 1
else
    if [ -r /proc/cpuinfo ]; then
        NUM_PROCESSORS="$(cat /proc/cpuinfo | grep -E 'processor[[:space:]]*:' | wc -l)" || exit 1
    fi
fi
if [ "$NUM_PROCESSORS" ]; then
    word_list_prepend MAKEFLAGS "-j$NUM_PROCESSORS" || exit 1
fi
export MAKEFLAGS



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

require_config()
{
    cd "$TIGHTDB_JAVA_HOME" || return 1
    if ! [ -e "config" ]; then
        cat 1>&2 <<EOF
ERROR: Found no configuration!
You need to run 'sh build.sh config [PREFIX]'.
EOF
        return 1
    fi
    echo "Using existing configuration:"
    cat "config" | sed 's/^/    /' || return 1
}

auto_configure()
{
    cd "$TIGHTDB_JAVA_HOME" || return 1
    if [ -e "config" ]; then
        require_config || return 1
    else
        echo "No configuration found. Running 'sh build.sh config'"
        sh build.sh config || return 1
    fi
}

get_config_param()
{
    local name line value
    cd "$TIGHTDB_JAVA_HOME" || return 1
    name="$1"
    if ! [ -e "config" ]; then
        cat 1>&2 <<EOF
ERROR: Found no configuration!
You need to run 'sh build.sh config [PREFIX]'.
EOF
        return 1
    fi
    if ! line="$(grep "^$name:" "config")"; then
        cat 1>&2 <<EOF
ERROR: Failed to read configuration parameter '$name'.
Maybe you need to rerun 'sh build.sh config [PREFIX]'.
EOF
        return 1
    fi
    value="$(printf "%s\n" "$line" | cut -d: -f2-)" || return 1
    value="$(printf "%s\n" "$value" | sed 's/^ *//')" || return 1
    printf "%s\n" "$value"
}


# Sets 'java_home', 'java_bindir', 'java_includedir' and
# 'java_includedir_arch' on success
check_java_home()
{
    local cand bin inc arch found_jni_md_h os_lc
    cand="$1"
    echo "Checking '$cand' as candidate for JAVA_HOME"

    # Locate 'java' 'javac' and 'jni.h'
    bin=""
    inc=""
    if [ "$OS" = "Darwin" ]; then
        if [ -e "$cand/Commands/java" -a -e "$cand/Commands/javac" -a -e "$cand/Headers/jni.h" ]; then
            echo "Found 'Commands/java', 'Commands/javac' and 'Headers/jni.h' in '$cand'"
            bin="Commands"
            inc="Headers"
        else
            echo "Could not find 'Commands/java', 'Commands/javac' and 'Headers/jni.h' in '$cand'"
        fi
    fi
    if ! [ "$bin" ]; then
        if [ -e "$cand/bin/java" -a -e "$cand/bin/javac" -a -e "$cand/include/jni.h" ]; then
            echo "Found 'bin/java', 'bin/javac' and 'include/jni.h' in '$cand'"
            bin="bin"
            inc="include"
        else
            echo "Could not find 'bin/java', 'bin/javac' and 'include/jni.h' in '$cand'"
        fi
    fi

    # Do we need to add a platform dependent include directory?
    arch="none"
    if [ "$inc" ]; then
        if [ -e "$cand/$inc/jni_md.h" ]; then
            echo "Found '$inc/jni_md.h' in '$cand'"
            found_jni_md_h="1"
        else
            os_lc="$(printf "%s\n" "$OS" | awk '{print tolower($0)}')" || return 1
            if [ -e "$cand/$inc/$os_lc/jni_md.h" ]; then
                echo "Found '$inc/$os_lc/jni_md.h' in '$cand'"
                found_jni_md_h="1"
                arch="$cand/$inc/$os_lc"
            else
                echo "Could not find '$inc/jni_md.h' or '$inc/$os_lc/jni_md.h' in '$cand'"
            fi
        fi
    fi

    if [ "$bin" ] && [ "$found_jni_md_h" ]; then
        java_home="$cand"
        java_bindir="$java_home/$bin"
        java_includedir="$java_home/$inc"
        java_includedir_arch="$arch"
    else
        echo "Skipping '$cand'"
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
        if [ -n "$INTERACTIVE" ]; then
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
            echo "JAVA_HOME specified"
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
                echo "'/usr/libexec/java_home -v 1.6+' specifies a JAVA_HOME"
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
                echo "'javac' found in PATH as '$path'"
                check_java_home "$cand" || exit 1
            fi
        fi

        if ! [ "$java_home" ]; then
            echo "ERROR: No JAVA_HOME and no Java compiler in PATH" 1>&2
            exit 1
        fi

        java_cmd="$java_bindir/java"
        javac_cmd="$java_bindir/javac"

        echo "Examining Java command '$java_cmd'"
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
        echo "Using Java command: $java_cmd (version $version)"
        java_version="$version"

        if [ "$install_prefix" = "auto" ]; then
            if [ "$OS" = "Darwin" ]; then
                jni_install_dir="/Library/Java/Extensions"
            else
                # We choose /usr/lib over /usr/local/lib because the
                # latter is not in the default runtime library search
                # path on RedHat and RedHat derived systems.
                jni_install_dir="$(cd "tightdb_jni" && make -s prefix="/usr" get-libdir)" || exit 1
            fi
            jar_install_dir="/usr/local/share/java"
        else
            jni_install_dir="$(cd "tightdb_jni" && make -s prefix="$install_prefix" get-libdir)" || exit 1
            jar_install_dir="$install_prefix/share/java"
        fi

        jar_dirs="/usr/local/share/java /usr/share/java"

        if [ "$OS" = "Darwin" ]; then
            jni_suffix=".jnilib"
            word_list_append jar_dirs "/Library/Java/Extensions"
            word_list_append jar_dirs "/System/Library/Java/Extensions"
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
                    interactive_install_required_jar $x
                    word_list_append "required_jars" /usr/local/share/java/$x
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


        cat >"config" <<EOF
java-version:         $java_version
java-command:         $java_cmd
javac-command:        $javac_cmd
java-includedir:      $java_includedir
java-includedir-arch: $java_includedir_arch
required-jars:        $required_jars
testing-jars:         $testing_jars
install-prefix:       $install_prefix
jni-install-dir:      $jni_install_dir
jar-install-dir:      $jar_install_dir
jni-suffix:           $jni_suffix
EOF
        if [ -z "$INTERACTIVE" ]; then
            echo "New configuration:"
            cat "config" | sed 's/^/    /' || exit 1
            echo "Done configuring"
        fi
        exit 0
        ;;

    "install-report")
        jni_install_dir="$(get_config_param "jni-install-dir")"
        jar_install_dir="$(get_config_param "jar-install-dir")"
        echo "Installed JNI files:"
        find $jni_install_dir -name '*tight*jni*'
        echo "Installed JAR files:"
        find $jar_install_dir -name '*tightdb*jar'
        ;;


    "clean")
        auto_configure || exit 1
        jni_suffix="$(get_config_param "jni-suffix")" || exit 1
        make -C "tightdb_jni" clean LIB_SUFFIX_SHARED="$jni_suffix" || exit 1
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
        echo "Done cleaning"
        exit 0
        ;;

    "build")
        auto_configure || exit 1
        javac_cmd="$(get_config_param "javac-command")" || exit 1
        java_includedir="$(get_config_param "java-includedir")" || exit 1
        java_includedir_arch="$(get_config_param "java-includedir-arch")" || exit 1
        jni_suffix="$(get_config_param "jni-suffix")"   || exit 1

        # Build libtightdb-jni.so
        extra_cflags="-I$java_includedir"
        if [ "$java_includedir_arch" != "none" ]; then
            extra_cflags="$extra_cflags -I$java_includedir_arch"
        fi
        TIGHTDB_ENABLE_FAT_BINARIES="1" make -C "tightdb_jni" EXTRA_CFLAGS="$extra_cflags" LIB_SUFFIX_SHARED="$jni_suffix" || exit 1

        mkdir -p "lib" || exit 1

        # Build tightdb.jar
        echo "Building 'lib/tightdb.jar'"
        dir="tightdb-java-core/src/main"
        tightdb_jar="$TIGHTDB_JAVA_HOME/lib/tightdb.jar"
        (cd "$dir/java" && $javac_cmd            com/tightdb/*.java com/tightdb/internal/*.java  com/tightdb/typed/*.java)   || exit 1
        (cd "$dir/java" && jar cf "$tightdb_jar" com/tightdb/*.class com/tightdb/internal/*.class com/tightdb/typed/*.class) || exit 1
        (cd "lib" && jar i "tightdb.jar") || exit 1

        # Build tightdb-devkit.jar
        echo "Building 'lib/tightdb-devkit.jar'"
        required_jars="$(get_config_param "required-jars")" || exit 1
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

        # Setup links to libraries to make the examples work
        echo "Setting up library symlinks in 'lib' to make examples work"
        mkdir -p "lib" || exit 1
        core_dir="../tightdb"
        core_library_aliases="$(cd "$core_dir/src/tightdb" && make -s get-inst-libraries)" || exit 1
        for x in $core_library_aliases; do
            (cd "lib" && ln -s -f "../$core_dir/src/tightdb/$x") || exit 1
        done
        library_aliases="$(cd "tightdb_jni/src" && make -s get-inst-libraries LIB_SUFFIX_SHARED="$jni_suffix")" || exit 1
        for x in $library_aliases; do
            (cd "lib" && ln -s -f "../tightdb_jni/src/$x") || exit 1
        done
        echo "Done building"
        exit 0
        ;;

    "test")
        require_config || exit 1
        javac_cmd="$(get_config_param "javac-command")" || exit 1
        devkit_jar="$TIGHTDB_JAVA_HOME/lib/tightdb-devkit.jar"
        temp_dir="$(mktemp -d /tmp/tightdb.java.test-debug.XXXX)" || exit 1
        mkdir "$temp_dir/out" || exit 1
        mkdir "$temp_dir/gen" || exit 1

        dir="tightdb-java-test/src/test"
        echo "Building test suite in '$dir'"
        export CLASSPATH="$devkit_jar:$temp_dir/gen:."
        (cd "$dir/../test/java" && $javac_cmd -d "$temp_dir/out" -s "$temp_dir/gen" com/tightdb/test/TestTableModel.java) || exit 1

        path_list_append "CLASSPATH" "../main" || exit 1
        testing_jars="$(get_config_param "testing-jars")" || exit 1
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
        java_cmd="$(get_config_param "java-command")" || exit 1
        (cd "$temp_dir/out" && $java_cmd -Djava.library.path="$TIGHTDB_JAVA_HOME/tightdb_jni/src" org.testng.TestNG -d "$TIGHTDB_JAVA_HOME/test_output" "$testng_xml") || exit 1
        exit 0
        ;;

    "test-debug")
        TIGHTDB_JAVA_DEBUG="1" sh build.sh test || exit 1
        exit 0
        ;;

    "test-examples")
        cd "examples/intro-example" || exit 1
        ant runall || exit 1
        echo "Test passed"
        exit 0
        ;;

    "install")
        require_config || exit 1

        if ! [ "$DESTDIR" ]; then
            jar_list="tightdb-devkit.jar tightdb.jar"
            full_install="yes"
        else
            if [ $(echo $DESTDIR | grep -c "dev$") = 1 ]; then
                jar_list="tightdb-devkit.jar"
                full_install="no"
            else
                jar_list="tightdb.jar"
                full_install="yes"
            fi
        fi

        jni_install_dir="$(get_config_param "jni-install-dir")" || exit 1
        jni_suffix="$(get_config_param "jni-suffix")"           || exit 1
        jar_install_dir="$DESTDIR$(get_config_param "jar-install-dir")" || exit 1

        install -d "$jar_install_dir" || exit 1

        if [ "$full_install" = "yes" ]; then
            make -C "tightdb_jni" install DESTDIR="$DESTDIR" libdir="$jni_install_dir" LIB_SUFFIX_SHARED="$jni_suffix" || exit 1
        fi

        for x in $jar_list; do
            echo "Installing '$jar_install_dir/$x'"
            install -m 644 "lib/$x" "$jar_install_dir" || exit 1
        done


        echo "Done installing"
        exit 0
        ;;

    "uninstall")
        require_config || exit 1

        jar_install_dir="$(get_config_param "jar-install-dir")" || exit 1
        for x in "tightdb-devkit.jar" "tightdb.jar"; do
            echo "Uninstalling '$jar_install_dir/$x'"
            rm -f "$jar_install_dir/$x" || exit 1
        done

        jni_install_dir="$(get_config_param "jni-install-dir")" || exit 1
        jni_suffix="$(get_config_param "jni-suffix")"           || exit 1
        make -C "tightdb_jni" uninstall libdir="$jni_install_dir" LIB_SUFFIX_SHARED="$jni_suffix" || exit 1

        echo "Done uninstalling"
        exit 0
        ;;

    "test-installed")
        require_config || exit 1

        jar_install_dir="$(get_config_param "jar-install-dir")" || exit 1
        devkit_jar="$jar_install_dir/tightdb-devkit.jar"
        export CLASSPATH="$devkit_jar:."
        javac_cmd="$(get_config_param "javac-command")" || exit 1
        temp_dir="$(mktemp -d /tmp/tightdb.java.test-installed.XXXX)" || exit 1
        (cd "test-installed" && $javac_cmd -d "$temp_dir" -s "$temp_dir" java/my/app/Test.java) || exit 1

        install_prefix="$(get_config_param "install-prefix")" || exit 1
        java_cmd="$(get_config_param "java-command")"         || exit 1
        if [ "$install_prefix" != "auto" ]; then
            jni_install_dir="$(get_config_param "jni-install-dir")" || exit 1
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
/*.mk
/tightdb_jni/Makefile
/tightdb_jni/src
/tightdb-java-core
/tightdb-java-generator
/tightdb-java-test
/test-installed
/examples
/prerequisite_jars
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
        (cd "$TARGET_DIR" && tar xzf "$TEMP_DIR/archive.tar.gz") || exit 1
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
        echo "Available modes are: config clean build test test-debug test-examples install uninstall test-installed" 1>&2
        echo "As well as: dist-copy dist-remarks" 1>&2
        exit 1
        ;;

esac
