#include <cstdio>
// Debian package: libproc-dev
// Linker flag   : -lproc
// Documentation : /usr/include/proc/readproc.h
#include <proc/readproc.h>

size_t GetMemUsage()
{
  struct proc_t usage;
  look_up_our_self(&usage);
  return usage.vsize;
}
