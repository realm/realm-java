# NOTE: THIS SCRIPT IS SUPPOSED TO RUN IN A POSIX SHELL

THIS_DIR="$(dirname "$0")" || exit 1
CORE_LIB_DIR="$(cd "$THIS_DIR/../../tightdb/src/tightdb" && pwd)" || exit 1

LD_LIBRARY_PATH_NAME="LD_LIBRARY_PATH"

# Setup OS specific stuff
OS="$(uname -s)" || exit 1
if [ "$OS" = "Darwin" ]; then
    LD_LIBRARY_PATH_NAME="DYLD_LIBRARY_PATH"
fi

path_list_prepend()
{
    local list_name new_path list
    list_name="$1"
    new_path="$2"
    list="$(eval "printf \"%s\\n\" \"\${$list_name}\"")" || return 1
    if [ "$list" ]; then
        eval "$list_name=\"\$new_path:\$list\""
    else
        eval "$list_name=\"\$new_path\""
    fi
    return 0
}

path_list_prepend "$LD_LIBRARY_PATH_NAME" "$CORE_LIB_DIR" || exit 1

export "$LD_LIBRARY_PATH_NAME"
