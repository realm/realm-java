Update the files in source as necessary.
The files in target should be exactly the same as those in source, except
that the field previously called 'child' should be called 'childxxx'.
THis should require 3 changes: the field name, the getter and setter, and
the reference in the LinkingObjects annotation.
Use bin/cgen, also in tools, to build the source and target directories
to respective destination directories.  In the destination directories,
remove all .java files and all files that do not refer to either the source
or the target, respectively (including the default modules).  Build jar files.
You now have two jar files, one containing just the source module and the
other containing just the target module.  They both compiled correctly, but
they cannot work with each other, because the target's annotation refers
to the field 'childxxx' which does not exist in the source.
