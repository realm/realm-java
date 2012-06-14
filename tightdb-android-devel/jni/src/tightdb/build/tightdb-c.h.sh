DIR="$(dirname "$0")"

TIGHTDB_C_H="$1"

if python "$DIR/tightdb-c.h.py" 8 >/tmp/tightdb-c.hpp; then
    mv /tmp/tightdb-c.h "$TIGHTDB_C_H"
else
    if [ -e "$TIGHTDB_C_H" ]; then
        echo "WARNING: Failed to update '$TIGHTDB_C_H'"
    else
        exit 1
    fi
fi
