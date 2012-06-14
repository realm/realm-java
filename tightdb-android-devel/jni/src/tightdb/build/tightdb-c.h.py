import sys
from Cheetah.Template import Template
templateDef = """#slurp
#compiler-settings
commentStartToken = %%
directiveStartToken = %
#end compiler-settings
/*************************************************************************
 * 
 * TIGHTDB CONFIDENTIAL
 * __________________
 * 
 *  [2011] - [2012] TightDB Inc
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of TightDB Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to TightDB Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from TightDB Incorporated.
 *
 **************************************************************************/
#ifndef TIGHTDB_TIGHTDB_C_H
#define TIGHTDB_TIGHTDB_C_H

#include "c-table.hpp"
#include "query.h"

%for $col in range($max_cols)
%set $num_cols = $col + 1

#define TIGHTDB_TABLE_${num_cols}(TableName%slurp
%for $j in range($num_cols)
, CName$j, CType$j%slurp
%end for
) \\
\\
Table* TableName##_new(void) { \\
    Table *tbl = table_new(); \\
    Spec* spec = table_get_spec(tbl); \\
%for $j in range($num_cols)
	spec_add_column(spec, COLUMN_TYPE_##CType$j, #CName$j); \\
%end for
    table_update_from_spec(tbl, spec_get_ref(spec)); \\
    spec_delete(spec); \\
    return tbl; \\
} \\
\\
void TableName##_add(Table* tbl, %slurp
%for $j in range($num_cols)
%if 0 < $j
, %slurp
%end if
tdb_type_##CType$j value$j%slurp
%end for
) { \\
	table_add(tbl, %slurp
%for $j in range($num_cols)
%if 0 < $j
, %slurp
%end if
value$j%slurp
%end for
); \\
} \\
\\
void TableName##_insert(Table* tbl, size_t row_ndx, %slurp
%for $j in range($num_cols)
%if 0 < $j
, %slurp
%end if
tdb_type_##CType$j value$j%slurp
%end for
) { \\
	table_insert(tbl, row_ndx, %slurp
%for $j in range($num_cols)
%if 0 < $j
, %slurp
%end if
value$j%slurp
%end for
); \\
} \\
\\
%for $j in range($num_cols)
tdb_type_##CType$j TableName##_get_##CName${j}(Table* tbl, size_t row_ndx) { \\
	return table_get_##CType${j}(tbl, $j, row_ndx); \\
} \\
void TableName##_set_##CName${j}(Table* tbl, size_t row_ndx, tdb_type_##CType$j value) { \\
	return table_set_##CType${j}(tbl, $j, row_ndx, value); \\
} \\
%end for


%end for

#endif // TIGHTDB_TIGHTDB_C_H
"""

args = sys.argv[1:]
if len(args) != 1:
	sys.stderr.write("Please specify the maximum number of table columns\n")
	sys.exit(1)
max_cols = int(args[0])
t = Template(templateDef, searchList=[{'max_cols': max_cols}])
sys.stdout.write(str(t))
