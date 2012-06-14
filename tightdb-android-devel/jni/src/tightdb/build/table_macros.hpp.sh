DIR="$(dirname "$0")"

TABLE_MACROS_HPP="$1"

if python "$DIR/table_macros.hpp.py" 8 >/tmp/table_macros.hpp; then
    mv /tmp/table_macros.hpp "$TABLE_MACROS_HPP"
else
    if [ -e "$TABLE_MACROS_HPP" ]; then
        echo "WARNING: Failed to update '$TABLE_MACROS_HPP'"
    else
        exit 1
    fi
fi
