# Note:
# $@  The name of the target file (the one before the colon)
# $<  The name of the first (or only) prerequisite file
#     (the first one after the colon)
# $^  The names of all the prerequisite files (space separated)
# $*  The stem (the bit which matches the % wildcard in the rule definition.
#

ROOT = ../..

INC_FLAGS     = -I$(ROOT)/src
INC_FLAGS_ABS = -I$(abspath $(ROOT)/src)

SOURCES           = $(wildcard *.c *.cpp)
INSTALLED_HEADERS = $(wildcard *.h *.hpp)
LIB_SHARED   = libtightdb.so
LIB_STATIC   = libtightdb.a
LIB_DEBUG    = libtightdb_d.a
LIB_COVERAGE = libtightdb_c.a
TARGETS = $(LIB_STATIC) $(LIB_SHARED) $(LIB_DEBUG) $(LIB_COVERAGE)

all: shared
.PHONY: all

install: shared static installdirs
	$(INSTALL_LIB) $(LIB_SHARED) $(LIB_STATIC) $(DESTDIR)$(libdir)/
	$(INSTALL_HEADER) $(INSTALLED_HEADERS) $(DESTDIR)$(includedir)/tightdb/
.PHONY: install

installdirs:
	$(INSTALL_DIR) $(DESTDIR)$(libdir)/ $(DESTDIR)$(includedir)/tightdb/
.PHONY: installdirs

uninstall:
	rm -f $(DESTDIR)$(includedir)/tightdb/*.hpp
	rm -f $(DESTDIR)$(includedir)/tightdb/*.h
	rmdir $(DESTDIR)$(includedir)/tightdb
	rm -f $(DESTDIR)$(libdir)/$(LIB_SHARED)
	rm -f $(DESTDIR)$(libdir)/$(LIB_STATIC)
.PHONY: uninstall

shared: $(LIB_SHARED)
.PHONY: shared

static: $(LIB_STATIC)
.PHONY: static

debug: $(LIB_DEBUG)
.PHONY: debug

cover: $(LIB_COVERAGE)
.PHONY: cover



CONFIG_MK = $(ROOT)/config.mk
include $(CONFIG_MK)

STATIC_OBJECTS = $(patsubst %.c,%.o,$(patsubst %.cpp,%.o,$(SOURCES)))
SHARED_OBJECTS   = $(STATIC_OBJECTS:.o=.dyn.o)
DEBUG_OBJECTS    = $(STATIC_OBJECTS:.o=.dbg.o)
COVERAGE_OBJECTS = $(STATIC_OBJECTS:.o=.cov.o)
OBJECTS = $(STATIC_OBJECTS) $(SHARED_OBJECTS) $(DEBUG_OBJECTS) $(COVERAGE_OBJECTS)

$(OBJECTS): Makefile $(CONFIG_MK)



# Code generation
TABLE_MACROS_HPP = table_macros.hpp
$(TABLE_MACROS_HPP): build/table_macros.hpp.sh build/table_macros.hpp.py Makefile $(CONFIG_MK)
	$(SHELL) build/table_macros.hpp.sh $(TABLE_MACROS_HPP)
$(OBJECTS): $(TABLE_MACROS_HPP)



clean:
	$(RM) *.d *.o *.gcno *.gcda $(TARGETS)
.PHONY: clean


# Library creation

$(LIB_STATIC): $(STATIC_OBJECTS)
	$(AR) $(ARFLAGS) $@ $(STATIC_OBJECTS)

$(LIB_SHARED): $(SHARED_OBJECTS)
        # FIXME: add -Wl,-soname and -Wl,-rpath
	$(LD_SHARED) $(SHARED_OBJECTS) -o $@

$(LIB_DEBUG): $(DEBUG_OBJECTS)
	$(AR) $(ARFLAGS) $@ $(DEBUG_OBJECTS)

$(LIB_COVERAGE): $(COVERAGE_OBJECTS)
	$(AR) $(ARFLAGS) $@ $(COVERAGE_OBJECTS)


# Compiling + automatic dependencies

%.o: %.c
	$(CC_STATIC) $(CFLAGS) $(INC_FLAGS) -MMD -MP -c $< -o $@

%.o: %.cpp
	$(CXX_STATIC) $(CXXFLAGS) $(INC_FLAGS) -MMD -MP -c $< -o $@

%.dyn.o: %.c
	$(CC_SHARED) $(CFLAGS) $(INC_FLAGS) -MMD -MP -c $< -o $@

%.dyn.o: %.cpp
	$(CXX_SHARED) $(CXXFLAGS) $(INC_FLAGS) -MMD -MP -c $< -o $@

%.dbg.o: %.c
	$(CC_DEBUG) $(CFLAGS) $(INC_FLAGS) -MMD -MP -c $< -o $@

%.dbg.o: %.cpp
	$(CXX_DEBUG) $(CXXFLAGS) $(INC_FLAGS) -MMD -MP -c $< -o $@

%.cov.o: %.c
	$(CC_COVERAGE) $(CFLAGS) $(INC_FLAGS_ABS) -MMD -MP -c $(abspath $<) -o $(abspath $@)

%.cov.o: %.cpp
	$(CXX_COVERAGE) $(CXXFLAGS) $(INC_FLAGS_ABS) -MMD -MP -c $(abspath $<) -o $(abspath $@)

-include $(OBJECTS:.o=.d)
