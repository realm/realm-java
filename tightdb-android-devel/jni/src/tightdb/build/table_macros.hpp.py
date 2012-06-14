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
#ifndef TIGHTDB_TABLE_MACROS_HPP
#define TIGHTDB_TABLE_MACROS_HPP

#include "table_basic.hpp"


%for $i in range($max_cols)
%set $num_cols = $i + 1
#define TIGHTDB_TABLE_${num_cols}(Table%slurp
%for $j in range($num_cols)
, name${j+1}, type${j+1}%slurp
%end for
) \\
struct Table##Spec: ::tightdb::SpecBase { \\
%for $j in range($num_cols)
    typedef ::tightdb::TypeAppend< %slurp
%if $j == 0
void,     %slurp
%else
Columns$j, %slurp
%end if
type${j+1} >::type Columns%slurp
%if $j < $num_cols-1
${j+1}%slurp
%end if
; \\
%end for
 \\
    template<template<int> class Col, class Init> struct ColNames { \\
%for $j in range($num_cols)
        typename Col<$j>::type name${j+1}; \\
%end for
        ColNames(Init i): %slurp
%for $j in range($num_cols)
%if 0 < $j
, %slurp
%end if
name${j+1}%slurp
(i)%slurp
%end for
 {} \\
    }; \\
 \\
    static const char* const* dyn_col_names() \\
    { \\
        static const char* names[] = { %slurp
%for $j in range($num_cols)
%if 0 < $j
, %slurp
%end if
#name${j+1}%slurp
%end for
 }; \\
        return names; \\
    } \\
 \\
    struct ConvenienceMethods { \\
        void add(%slurp
%for $j in range($num_cols)
%if 0 < $j
, %slurp
%end if
type${j+1} name${j+1}%slurp
%end for
) \\
        { \\
            ::tightdb::BasicTable<Table##Spec>* const t = \\
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \\
            t->add((::tightdb::tuple()%slurp
%for $j in range($num_cols)
, name${j+1}%slurp
%end for
)); \\
        } \\
        void insert(std::size_t _i%slurp
%for $j in range($num_cols)
, type${j+1} name${j+1}%slurp
%end for
) \\
        { \\
            ::tightdb::BasicTable<Table##Spec>* const t = \\
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \\
            t->insert(_i, (::tightdb::tuple()%slurp
%for $j in range($num_cols)
, name${j+1}%slurp
%end for
)); \\
        } \\
        void set(std::size_t _i%slurp
%for $j in range($num_cols)
, type${j+1} name${j+1}%slurp
%end for
) \\
        { \\
            ::tightdb::BasicTable<Table##Spec>* const t = \\
                static_cast< ::tightdb::BasicTable<Table##Spec>* >(this); \\
            t->set(_i, (::tightdb::tuple()%slurp
%for $j in range($num_cols)
, name${j+1}%slurp
%end for
)); \\
        } \\
    }; \\
}; \\
typedef ::tightdb::BasicTable<Table##Spec> Table;


%end for
#endif // TIGHTDB_TABLE_MACROS_HPP
"""

args = sys.argv[1:]
if len(args) != 1:
    sys.stderr.write("Please specify the maximum number of table columns\n")
    sys.exit(1)
max_cols = int(args[0])
t = Template(templateDef, searchList=[{'max_cols': max_cols}])
sys.stdout.write(str(t))
