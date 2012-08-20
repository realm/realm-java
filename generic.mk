# Generic makefile that captures some of the ideas of GNU Automake,
# especially in regard to naming targets.
#
# Author: Kristian Spangsege
#
# This makefile requires GNU Make, it has been tested with version
# 3.81.
#
# All variables listed in the section CONFIG VARIABLES are available
# for modification in config.mk, and they may also be overridden on
# the command line. For example, to enable PTHREADS and disable
# automatic dependency tracking, you could do this:
#
#   make CFLAGS_PTHREAD=-pthread CFLAGS_AUTODEP=""
#
# If CFLAGS is specified in the environment or on the command line, it
# will replace the value of CFLAGS_DEFAULT. Similarly with CXXFLAGS,
# LDFLAGS, and ARFLAGS.
#
# If EXTRA_CFLAGS is specified on the command line, its contents will
# be added to CFLAGS_DEFAULT (or to CFLAGS, if it is
# specified). Similarly with CXXFLAGS and LDFLAGS.
#
# If CC, CXX, LD, or AR is specified in the environment or on the
# command line, its value will be respected.
#
# NOTE: When you change the configuration specified via the
# environment or the command line from one invocation of make to the
# next, you should always start with a 'make clean'. MAKE does this
# automatically when you change config.mk.
#
# In config.mk you may check the value of CC_AND_CXX_ARE_GCC_LIKE. If
# it has a nonempty value, then both the C and the C++ compiler is
# mostly command line compatible with GCC (gcc, llvm-gcc, clang).


# CONFIG VARIABLES

# Relative path to root of source tree. If specified, a corresponding
# include option (-I) is added to the compiler command
# line. Specifying it, also permits installation of headers. Headers
# will be installed under the same relative path as they have with
# respect to the directory specified here. Headers are marked for
# installation by adding them to the INST_HEADERS variable in the
# local Makefile.
SOURCE_ROOT =

CFLAGS_DEFAULT   =
CXXFLAGS_DEFAULT = $(CFLAGS_DEFAULT)
CFLAGS_OPTIMIZE  =
CFLAGS_DEBUG     =
CFLAGS_COVERAGE  =
CFLAGS_SHARED    =
CFLAGS_PTHREAD   =
CFLAGS_INCLUDE   =
CFLAGS_AUTODEP   =
LDFLAGS_DEFAULT  =
LDFLAGS_OPTIMIZE = $(CFLAGS_OPTIMIZE)
LDFLAGS_SHARED   = $(CFLAGS_SHARED)
LDFLAGS_DEBUG    = $(CFLAGS_DEBUG)
LDFLAGS_COVERAGE = $(CFLAGS_COVERAGE)
LDFLAGS_PTHREAD  = $(CFLAGS_PTHREAD)
ARFLAGS_DEFAULT  = csr

# Installation (GNU style)
prefix          = /usr/local
exec_prefix     = $(prefix)
includedir      = $(prefix)/include
bindir          = $(exec_prefix)/bin
libdir          = $(exec_prefix)/lib
INSTALL         = install
INSTALL_DIR     = $(INSTALL) -d
INSTALL_DATA    = $(INSTALL) -m 644
INSTALL_LIBRARY = $(INSTALL)
INSTALL_PROGRAM = $(INSTALL)

SHARED_OBJ_DENOM    = .pic
DEBUG_OBJ_DENOM     = .dbg
COVERAGE_OBJ_DENOM  = .cov
DEBUG_LIB_DENOM     = -dbg
COVERAGE_LIB_DENOM  = -cov
DEBUG_PROG_DENOM    = -dbg
COVERAGE_PROG_DENOM = -cov

# When set to an empty value, 'make install' will also attempt to
# build everything as if by 'make all'. Set this variable to a
# nonempty value if you would rather want 'install' to simply accept
# what is there and attempt an installation from that.
NO_BUILD_ON_INSTALL =



# SETUP A GCC-LIKE DEFAULT COMPILER IF POSSIBLE

CC_SPECIFIED        = $(filter-out undefined default,$(origin CC))
CXX_SPECIFIED       = $(filter-out undefined default,$(origin CXX))
LD_SPECIFIED        = $(filter-out undefined default,$(origin LD))
CC_OR_CXX_SPECIFIED = $(or $(CC_SPECIFIED),$(CXX_SPECIFIED))
FILTER_GCC          = $(filter gcc gcc-% g++ g++-%,$(1))
FILTER_LLVM_GCC     = $(filter llvm-gcc llvm-gcc-% llvm-g++ llvm-g++-%,$(1))
FILTER_CLANG        = $(filter clang clang-% clang++ clang++-%,$(1))
ifeq ($(CC_OR_CXX_SPECIFIED),)
# Neither CC nor CXX is specified
HAVE_GCC      = $(and $(shell which gcc), $(shell which g++))
HAVE_LLVM_GCC = $(and $(shell which llvm-gcc), $(shell which llvm-g++))
HAVE_CLANG    = $(and $(shell which clang), $(shell which clang++))
ifneq ($(HAVE_GCC),)
CC  = gcc
CXX = g++
else ifneq ($(HAVE_LLVM_GCC),)
CC  = llvm-gcc
CXX = llvm-g++
else ifneq ($(HAVE_CLANG),)
CC  = clang
CXX = clang++
endif
else ifeq ($(CC_SPECIFIED),)
# CC is not specified, but CXX is
ifneq ($(call FILTER_GCC,$(CXX)),)
CC = $(patsubst g++%,gcc%,$(CXX))
else ifneq ($(call FILTER_LLVM_GCC,$(CXX)),)
CC = $(patsubst llvm-g++%,llvm-gcc%,$(CXX))
else ifneq ($(call FILTER_CLANG,$(CXX)),)
CC = $(patsubst clang++%,clang%,$(CXX))
endif
else ifeq ($(CXX_SPECIFIED),)
# CXX is not specified, but CC is
ifneq ($(call FILTER_GCC,$(CC)),)
CXX = $(patsubst gcc%,g++%,$(CC))
else ifneq ($(call FILTER_LLVM_GCC,$(CC)),)
CXX = $(patsubst llvm-gcc%,llvm-g++%,$(CC))
else ifneq ($(call FILTER_CLANG,$(CC)),)
CXX = $(patsubst clang%,clang++%,$(CC))
endif
endif
FILTER_GCC_LIKE = $(or $(call FILTER_GCC,$(1)),$(call FILTER_LLVM_GCC,$(1)),$(call FILTER_CLANG,$(1)))
CC_AND_CXX_ARE_GCC_LIKE = $(and $(call FILTER_GCC_LIKE,$(CC)),$(call FILTER_GCC_LIKE,$(CXX)))
ifneq ($(CC_AND_CXX_ARE_GCC_LIKE),)
# Linker - use the C++ compiler by default
ifeq ($(LD_SPECIFIED),)
LD = $(CXX)
endif
CFLAGS_DEFAULT   = -Wall
CFLAGS_OPTIMIZE  = -O3
CFLAGS_DEBUG     = -ggdb3
CFLAGS_COVERAGE  = --coverage
CFLAGS_SHARED    = -fPIC -DPIC
CFLAGS_AUTODEP   = -MMD -MP
LDFLAGS_OPTIMIZE =
LDFLAGS_SHARED   = -shared $(CFLAGS_SHARED)
LDFLAGS_DEBUG    =
endif

# Workaround for CLANG < v3.1 ignoring LIBRARY_PATH
ifneq ($(or $(call FILTER_CLANG,$(CC)),$(call FILTER_CLANG,$(CXX))),)
CLANG_VERSION = $(shell clang --version | grep -i 'clang version' | sed 's/.*clang version \([^ ][^ ]*\).*/\1/' | sed 's/[._-]/ /g')
CLANG_MAJOR = $(word 1,$(CLANG_VERSION))
CLANG_MINOR = $(word 2,$(CLANG_VERSION))
ifeq ($(shell echo $$(($(CLANG_MAJOR) < 3 || ($(CLANG_MAJOR) == 3 && $(CLANG_MINOR) < 1)))),1)
LDFLAGS_DEFAULT += $(foreach x,$(subst :, ,$(LIBRARY_PATH)),-L$(x))
endif
endif



# LOAD PROJECT SPECIFIC CONFIGURATION

THIS_MAKEFILE := $(lastword $(MAKEFILE_LIST))
ROOT = $(patsubst %/,%,$(dir $(THIS_MAKEFILE)))
ABS_ROOT = $(abspath $(ROOT))
CONFIG_MK = $(ROOT)/config.mk
-include $(CONFIG_MK)



# SETUP BUILD COMMANDS

CFLAGS_SPECIFIED    = $(filter-out undefined default,$(origin CFLAGS))
CXXFLAGS_SPECIFIED  = $(filter-out undefined default,$(origin CXXFLAGS))
LDFLAGS_SPECIFIED   = $(filter-out undefined default,$(origin LDFLAGS))
ARFLAGS_SPECIFIED   = $(filter-out undefined default,$(origin ARFLAGS))
ifeq ($(CFLAGS_SPECIFIED),)
CFLAGS = $(CFLAGS_DEFAULT)
endif
ifeq ($(CXXFLAGS_SPECIFIED),)
CXXFLAGS = $(CXXFLAGS_DEFAULT)
endif
ifeq ($(LDFLAGS_SPECIFIED),)
LDFLAGS = $(LDFLAGS_DEFAULT)
endif
ifeq ($(ARFLAGS_SPECIFIED),)
ARFLAGS = $(ARFLAGS_DEFAULT)
endif
EXTRA_CFLAGS   =
EXTRA_CXXFLAGS = $(EXTRA_CFLAGS)
EXTRA_LDFLAGS  =
CFLAGS   := $(CFLAGS) $(EXTRA_CFLAGS)
CXXFLAGS := $(CXXFLAGS) $(EXTRA_CXXFLAGS)
LDFLAGS  := $(LDFLAGS) $(EXTRA_LDFLAGS)

CC_STATIC       = $(CC) $(CFLAGS_OPTIMIZE) $(CFLAGS_PTHREAD)
CC_SHARED       = $(CC) $(CFLAGS_OPTIMIZE) $(CFLAGS_SHARED) $(CFLAGS_PTHREAD)
CC_DEBUG        = $(CC) $(CFLAGS_DEBUG) $(CFLAGS_PTHREAD)
CC_COVERAGE     = $(CC) $(CFLAGS_COVERAGE) $(CFLAGS_PTHREAD)

CXX_STATIC      = $(CXX) $(CFLAGS_OPTIMIZE) $(CFLAGS_PTHREAD)
CXX_SHARED      = $(CXX) $(CFLAGS_OPTIMIZE) $(CFLAGS_SHARED) $(CFLAGS_PTHREAD)
CXX_DEBUG       = $(CXX) $(CFLAGS_DEBUG) $(CFLAGS_PTHREAD)
CXX_COVERAGE    = $(CXX) $(CFLAGS_COVERAGE) $(CFLAGS_PTHREAD)

LD_STATIC       = $(LD) $(LDFLAGS_OPTIMIZE) $(LDFLAGS_PTHREAD)
LD_SHARED       = $(LD) $(LDFLAGS_SHARED) $(LDFLAGS_PTHREAD)
LD_DEBUG        = $(LD) $(LDFLAGS_DEBUG) $(LDFLAGS_PTHREAD)
LD_COVERAGE     = $(LD) $(LDFLAGS_COVERAGE) $(LDFLAGS_PTHREAD)

# Apply local directory config
CFLAGS_INCLUDE += $(ADD_CFLAGS_INCLUDE)



SUFFIX_OBJ_SHARED = $(SHARED_OBJ_DENOM).o
SUFFIX_OBJ_STATIC = .o
SUFFIX_OBJ_DEBUG  = $(DEBUG_OBJ_DENOM).o
SUFFIX_OBJ_COVER  = $(COVERAGE_OBJ_DENOM).o
SUFFIX_LIB_SHARED = .so
SUFFIX_LIB_STATIC = .a
SUFFIX_LIB_DEBUG  = $(DEBUG_LIB_DENOM).a
SUFFIX_LIB_COVER  = $(COVERAGE_LIB_DENOM).a
SUFFIX_PROG_DEBUG = $(DEBUG_PROG_DENOM)
SUFFIX_PROG_COVER = $(COVERAGE_PROG_DENOM)



# Functions:

EMPTY =
COND_PREPEND = $(if $(2),$(1)$(2),)
COND_APPEND = $(if $(1),$(1)$(2),)
EQUALS = $(if $(word 2,$(sort $(1) $(2))),,$(1))
PATH_DIFF_HELP = $(if $(and $(1),$(2),$(call EQUALS,$(word 1,$(1)),$(word 1,$(2)))),$(call PATH_DIFF_HELP,$(wordlist 2,$(words $(1)),$(1)),$(wordlist 2,$(words $(2)),$(2))),$(subst $(EMPTY) ,/,$(strip $(patsubst %,..,$(2)) $(1))))
PATH_DIFF = $(call PATH_DIFF_HELP,$(subst /, ,$(abspath $(1))),$(subst /, ,$(abspath $(2))))
FOLD_TARGET = $(subst .,_,$(subst -,_,$(1)))
GET_LIBRARY_NAME = $(patsubst %.a,%,$(1))$(2)
GET_OBJECTS_FROM_SOURCES = $(patsubst %.c,%$(2),$(patsubst %.cpp,%$(2),$(1)))
GET_OBJECTS_FOR_TARGET = $(call GET_OBJECTS_FROM_SOURCES,$($(call FOLD_TARGET,$(1))_SOURCES),$(2))
GET_INST_LIBS_FOR_TARGET = $(foreach x,$($(call FOLD_TARGET,$(1))_LIBADD),$(call GET_LIBRARY_NAME,$(x),$(2)))
GET_NOINST_LIBS_FOR_TARGET = $(foreach x,$($(call FOLD_TARGET,$(1))_NOINST_LIBADD),$(call GET_LIBRARY_NAME,$(x),$(2)))
GET_LIBS_FOR_TARGET = $(call GET_INST_LIBS_FOR_TARGET,$(1),$(2)) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),$(2))
GET_FLAGS_HELPER = $($(if $(filter undefined,$(origin $(1)$(2))),$(1),$(1)$(2)))
GET_CFLAGS_FOR_OBJECT = $(call GET_FLAGS_HELPER,$(call FOLD_TARGET,$(1))_CFLAGS,$(2))
GET_LDFLAGS_FOR_TARGET = $(call GET_FLAGS_HELPER,$(call FOLD_TARGET,$(1))_LDFLAGS,$(2))
GET_DEPS_FOR_TARGET = $($(call FOLD_TARGET,$(1))_DEPS)
GET_DEPS_FOR_TARGET2 = $(warning GET_DEPS_FOR_TARGET GET_DEPS_FOR_TARGET GET_DEPS_FOR_TARGET xx$(1)xx)

INC_FLAGS         = $(CFLAGS_INCLUDE)
INC_FLAGS_ABS     = $(CFLAGS_INCLUDE)
ifneq ($(SOURCE_ROOT),)
INC_FLAGS        += -I$(ROOT)/$(SOURCE_ROOT)
INC_FLAGS_ABS    += -I$(ABS_ROOT)/$(SOURCE_ROOT)
endif

INST_LIBRARIES = $(foreach x,lib $(EXTRA_INSTALL_PREFIXES),$($(x)_LIBRARIES))
INST_PROGRAMS  = $(foreach x,bin $(EXTRA_INSTALL_PREFIXES),$($(x)_PROGRAMS))

LIBRARIES = $(INST_LIBRARIES) $(NOINST_LIBRARIES)
PROGRAMS  = $(INST_PROGRAMS)  $(NOINST_PROGRAMS) $(TEST_PROGRAMS)

OBJECTS_SHARED = $(foreach x,$(INST_LIBRARIES),$(call GET_OBJECTS_FOR_TARGET,$(x),$(SUFFIX_OBJ_SHARED)))
OBJECTS_STATIC = $(foreach x,$(LIBRARIES) $(PROGRAMS),$(call GET_OBJECTS_FOR_TARGET,$(x),$(SUFFIX_OBJ_STATIC)))
OBJECTS_DEBUG  = $(foreach x,$(LIBRARIES) $(PROGRAMS),$(call GET_OBJECTS_FOR_TARGET,$(x),$(SUFFIX_OBJ_DEBUG)))
OBJECTS_COVER  = $(foreach x,$(LIBRARIES) $(TEST_PROGRAMS),$(call GET_OBJECTS_FOR_TARGET,$(x),$(SUFFIX_OBJ_COVER)))
OBJECTS = $(sort $(OBJECTS_SHARED) $(OBJECTS_STATIC) $(OBJECTS_DEBUG) $(OBJECTS_COVER))

TARGETS_LIB_SHARED        = $(foreach x,$(INST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),$(SUFFIX_LIB_SHARED)))
TARGETS_LIB_STATIC        = $(foreach x,$(INST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),$(SUFFIX_LIB_STATIC)))
TARGETS_LIB_DEBUG         = $(foreach x,$(INST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),$(SUFFIX_LIB_DEBUG)))
TARGETS_LIB_COVER         = $(foreach x,$(INST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),$(SUFFIX_LIB_COVER)))
TARGETS_NOINST_LIB        = $(foreach x,$(NOINST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),$(SUFFIX_LIB_STATIC)))
TARGETS_NOINST_LIB_DEBUG  = $(foreach x,$(NOINST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),$(SUFFIX_LIB_DEBUG)))
TARGETS_NOINST_LIB_COVER  = $(foreach x,$(NOINST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),$(SUFFIX_LIB_COVER)))
TARGETS_PROG              = $(INST_PROGRAMS)
TARGETS_PROG_DEBUG        = $(foreach x,$(INST_PROGRAMS),$(x)$(SUFFIX_PROG_DEBUG))
TARGETS_PROG_COVER        = $(foreach x,$(INST_PROGRAMS),$(x)$(SUFFIX_PROG_COVER))
TARGETS_NOINST_PROG       = $(NOINST_PROGRAMS)
TARGETS_NOINST_PROG_DEBUG = $(foreach x,$(NOINST_PROGRAMS),$(x)$(SUFFIX_PROG_DEBUG))
TARGETS_NOINST_PROG_COVER = $(foreach x,$(NOINST_PROGRAMS),$(x)$(SUFFIX_PROG_COVER))
TARGETS_TEST_PROG         = $(TEST_PROGRAMS)
TARGETS_TEST_PROG_DEBUG   = $(foreach x,$(TEST_PROGRAMS),$(x)$(SUFFIX_PROG_DEBUG))
TARGETS_TEST_PROG_COVER   = $(foreach x,$(TEST_PROGRAMS),$(x)$(SUFFIX_PROG_COVER))

TARGETS_DEFAULT    = $(TARGETS_LIB_SHARED) $(TARGETS_LIB_STATIC) $(TARGETS_LIB_DEBUG) $(TARGETS_NOINST_LIB) $(TARGETS_PROG) $(TARGETS_NOINST_PROG)
TARGETS_NODEBUG    = $(TARGETS_LIB_SHARED) $(TARGETS_LIB_STATIC) $(TARGETS_NOINST_LIB) $(TARGETS_PROG) $(TARGETS_NOINST_PROG)
TARGETS_SHARED     = $(TARGETS_LIB_SHARED) $(TARGETS_NOINST_LIB) $(TARGETS_PROG) $(TARGETS_NOINST_PROG)
TARGETS_STATIC     = $(TARGETS_LIB_STATIC) $(TARGETS_NOINST_LIB) $(TARGETS_PROG) $(TARGETS_NOINST_PROG)
TARGETS_DEBUG      = $(TARGETS_LIB_DEBUG) $(TARGETS_NOINST_LIB_DEBUG) $(TARGETS_PROG_DEBUG) $(TARGETS_NOINST_PROG_DEBUG)
TARGETS_COVER      = $(TARGETS_LIB_COVER) $(TARGETS_NOINST_LIB_COVER) $(TARGETS_PROG_COVER) $(TARGETS_NOINST_PROG_COVER)
TARGETS_TEST       = $(TARGETS_LIB_STATIC) $(TARGETS_NOINST_LIB) $(TARGETS_TEST_PROG)
TARGETS_TEST_DEBUG = $(TARGETS_LIB_DEBUG) $(TARGETS_NOINST_LIB_DEBUG) $(TARGETS_TEST_PROG_DEBUG)
TARGETS_TEST_COVER = $(TARGETS_LIB_COVER) $(TARGETS_NOINST_LIB_COVER) $(TARGETS_TEST_PROG_COVER)
TARGETS            = $(TARGETS_LIB_SHARED) $(TARGETS_LIB_STATIC) $(TARGETS_LIB_DEBUG) $(TARGETS_LIB_COVER) $(TARGETS_NOINST_LIB) $(TARGETS_NOINST_LIB_DEBUG) $(TARGETS_NOINST_LIB_COVER) $(TARGETS_PROG) $(TARGETS_PROG_DEBUG) $(TARGETS_PROG_COVER) $(TARGETS_NOINST_PROG) $(TARGETS_NOINST_PROG_DEBUG) $(TARGETS_NOINST_PROG_COVER) $(TARGETS_TEST_PROG) $(TARGETS_TEST_PROG_DEBUG) $(TARGETS_TEST_PROG_COVER)

RECURSIVE_MODES = default nodebug shared static debug cover clean install uninstall test-norun test-debug-norun test test-debug test-cover memtest memtest-debug

.PHONY: all
all: default

default/local:    $(TARGETS_DEFAULT)
nodebug/local:    $(TARGETS_NODEBUG)
shared/local:     $(TARGETS_SHARED)
static/local:     $(TARGETS_STATIC)
debug/local:      $(TARGETS_DEBUG)
cover/local:      $(TARGETS_COVER)
test-norun:       $(TARGETS_TEST)
test-debug-norun: $(TARGETS_TEST_DEBUG)


# Update everything if any makefile has changed
DEP_MAKEFILES = Makefile $(CONFIG_MK) $(THIS_MAKEFILE)
$(OBJECTS) $(TARGETS): $(DEP_MAKEFILES)
$(OBJECTS): $(GENERATED_SOURCES)


# Disable all suffix rules and some interfering implicit pattern rules
.SUFFIXES:
%: %.o
%: %.c
%: %.cpp



# SUBDIRECTORIES

define SUBDIR_DEP_RULE
ifeq ($(3),.)
subdir/$(1)/$(2): $(2)/local
else
subdir/$(1)/$(2): subdir/$(3)/$(2)
endif
endef

define SUBDIR_MODE_RULES
.PHONY: subdir/$(1)/$(2)
$$(foreach x,$$($$(call FOLD_TARGET,$(1))_DEPS),$$(eval $$(call SUBDIR_DEP_RULE,$(1),$(2),$$(x))))
ifeq ($(2),default)
subdir/$(1)/$(2):
	@$$(MAKE) -C $(1)
else
subdir/$(1)/$(2):
	@$$(MAKE) -C $(1) $(2)
endif
endef

SUBDIR_RULES = $(foreach x,$(RECURSIVE_MODES),$(eval $(call SUBDIR_MODE_RULES,$(1),$(x))))

$(foreach x,$(SUBDIRS) $(PASSIVE_SUBDIRS),$(eval $(call SUBDIR_RULES,$(x))))

define RECURSIVE_MODE_RULES
.PHONY: $(1) $(1)/local $(1)/after
$(1): $(1)/local $$(patsubst %,subdir/%/$(1),$$(SUBDIRS)) $(1)/after
$(1)/after: $(1)/local $$(patsubst %,subdir/%/$(1),$$(SUBDIRS))
endef

$(foreach x,$(RECURSIVE_MODES),$(eval $(call RECURSIVE_MODE_RULES,$(x))))



# CLEANING

ifneq ($(strip $(TARGETS)),)
define CLEANING_RULES

.PHONY: clean/extra
clean/local: clean/extra
	$(RM) $(strip *.d *.o *.gcno *.gcda $(TARGETS))
	$(foreach x,$(EXTRA_CLEAN_DIRS),$(RM) $(x)/*.d $(x)/*.o $(x)/*.gcno $(x)/*.gcda
	)

endef
$(eval $(CLEANING_RULES))
endif



# INSTALL / UNINSTALL

.PHONY: install/header/dir install/lib/dirs install/prog/dirs
.PHONY: install/headers install/libs install/progs
.PHONY: uninstall/libs uninstall/progs uninstall/extra

ifeq ($(NO_BUILD_ON_INSTALL),)
install/local: $(TARGETS_DEFAULT)
install/libs: $(TARGETS_LIB_SHARED) $(TARGETS_LIB_STATIC) $(TARGETS_LIB_DEBUG)
install/progs: $(TARGETS_PROG)
endif

install/local: install/headers install/libs install/progs
uninstall/after: uninstall/progs uninstall/libs uninstall/extra

HEADER_INSTALL_DIR =
ifneq ($(INST_HEADERS),)
ifeq ($(SOURCE_ROOT),)
$(warning Cannot install headers without a value for SOURCE_ROOT)
else
HEADER_REL_PATH = $(call PATH_DIFF,.,$(ROOT)/$(SOURCE_ROOT))
SOURCE_ABS_ROOT = $(ABS_ROOT)/$(SOURCE_ROOT)
INSIDE_SOURCE = $(call EQUALS,$(SOURCE_ABS_ROOT)$(call COND_PREPEND,/,$(HEADER_REL_PATH)),$(abspath .))
ifeq ($(INSIDE_SOURCE),)
$(warning Cannot install headers lying outside SOURCE_ROOT)
else
HEADER_INSTALL_DIR = $(DESTDIR)$(includedir)$(call COND_PREPEND,/,$(HEADER_REL_PATH))
endif
endif
endif

GET_LIB_INSTALL_DIR   = $(if $($(1)_DIR),$($(1)_DIR),$(DESTDIR)$(libdir))
GET_PROG_INSTALL_DIR  = $(if $($(1)_DIR),$($(1)_DIR),$(DESTDIR)$(bindir))

INSTALL_RECIPE_DIR    = $(if $(1),$(INSTALL_DIR) $(1),)
INSTALL_RECIPE_LIB    = $(if $(2),$(INSTALL_LIBRARY) $(2) $(call GET_LIB_INSTALL_DIR,$(1)),)
INSTALL_RECIPE_PROG   = $(if $(2),$(INSTALL_PROGRAM) $(2) $(call GET_PROG_INSTALL_DIR,$(1)),)
UNINSTALL_RECIPE_LIB  = $(RM) $(call GET_LIB_INSTALL_DIR,$(1))/$(2)
UNINSTALL_RECIPE_PROG = $(RM) $(call GET_PROG_INSTALL_DIR,$(1))/$(2)

GET_INST_LIB_TARGETS  = $(foreach x,$($(1)_LIBRARIES),$(foreach y,$(SUFFIX_LIB_SHARED) $(SUFFIX_LIB_STATIC) $(SUFFIX_LIB_DEBUG),$(call GET_LIBRARY_NAME,$(x),$(y))))
GET_INST_PROG_TARGETS = $($(1)_PROGRAMS)

define INSTALL_RULES

ifneq ($(HEADER_INSTALL_DIR),)
install/headers: install/header/dir
	$(INSTALL_DATA) $(INST_HEADERS) $(HEADER_INSTALL_DIR)
install/header/dir:
	$(INSTALL_DIR) $(HEADER_INSTALL_DIR)
endif

install/libs: install/lib/dirs
	$(foreach x,lib $(EXTRA_INSTALL_PREFIXES),$(call INSTALL_RECIPE_LIB,$(x),$(call GET_INST_LIB_TARGETS,$(x)))
	)

install/progs: install/prog/dirs
	$(foreach x,bin $(EXTRA_INSTALL_PREFIXES),$(call INSTALL_RECIPE_PROG,$(x),$(call GET_INST_PROG_TARGETS,$(x)))
	)

uninstall/libs:
	$(foreach x,lib $(EXTRA_INSTALL_PREFIXES),$(foreach y,$(call GET_INST_LIB_TARGETS,$(x)),$(call UNINSTALL_RECIPE_LIB,$(x),$(y))
	))

uninstall/progs:
	$(foreach x,bin $(EXTRA_INSTALL_PREFIXES),$(foreach y,$(call GET_INST_PROG_TARGETS,$(x)),$(call UNINSTALL_RECIPE_PROG,$(x),$(y))
	))

install/lib/dirs:
	$(call INSTALL_RECIPE_DIR,$(strip $(foreach x,lib $(EXTRA_INSTALL_PREFIXES),$(if $($(x)_LIBRARIES),$(call GET_LIB_INSTALL_DIR,$(x)),))))

install/prog/dirs:
	$(call INSTALL_RECIPE_DIR,$(strip $(foreach x,bin $(EXTRA_INSTALL_PREFIXES),$(if $($(x)_PROGRAMS),$(call GET_PROG_INSTALL_DIR,$(x)),))))

endef

$(eval $(INSTALL_RULES))



# TESTING

define TEST_RULES

test/local: $(TARGETS_TEST)
	$(foreach x,$(TARGETS_TEST_PROG),./$(x)
	)

test-debug/local: $(TARGETS_TEST_DEBUG)
	$(foreach x,$(TARGETS_TEST_PROG_DEBUG),./$(x)
	)

memtest/local: $(TARGETS_TEST)
	$(foreach x,$(TARGETS_TEST_PROG),valgrind --quiet --error-exitcode=1 --track-origins=yes --leak-check=yes --leak-resolution=low ./$(x) --no-error-exit-staus
	)

memtest-debug/local: $(TARGETS_TEST_DEBUG)
	$(foreach x,$(TARGETS_TEST_PROG_DEBUG),valgrind --quiet --error-exitcode=1 --track-origins=yes --leak-check=yes --leak-resolution=low ./$(x) --no-error-exit-staus
	)

ifneq ($(strip $(TARGETS_TEST_COVER)),)
test-cover/local: $(TARGETS_TEST_COVER)
	$(RM) *.gcda
	$(foreach x,$(TARGETS_TEST_PROG_COVER),-./$(x)
	)
endif

endef

$(eval $(TEST_RULES))



# LINKING PROGRAMS

define PROGRAM_RULE
$(1): $(2) $(3)
	$(strip $(LD_STATIC) $(2) $(LDFLAGS) $(4)) -o $(1)
endef

define PROGRAM_RULE_DEBUG
$(1): $(2) $(3)
	$(strip $(LD_DEBUG) $(2) $(LDFLAGS) $(4)) -o $(1)
endef

define PROGRAM_RULE_COVER
$(1): $(2) $(3)
	$(strip $(LD_COVERAGE) $(2) $(LDFLAGS) $(4)) -o $(1)
endef

define INST_PROGRAM_RULES
$(call PROGRAM_RULE,$(1),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_STATIC)) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_STATIC)),$(2),$(call GET_LDFLAGS_FOR_TARGET,$(1),))
$(call PROGRAM_RULE_DEBUG,$(1)$(SUFFIX_PROG_DEBUG),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_DEBUG)) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_DEBUG)),$(2),$(call GET_LDFLAGS_FOR_TARGET,$(1),_DEBUG))
$(call PROGRAM_RULE_COVER,$(1)$(SUFFIX_PROG_COVER),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_COVER)) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_COVER)),$(2),$(call GET_LDFLAGS_FOR_TARGET,$(1),_COVER))
endef

define NOINST_PROGRAM_RULES
$(call PROGRAM_RULE,$(1),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_STATIC)) $(call GET_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_STATIC)),$(2),$(call GET_LDFLAGS_FOR_TARGET,$(1),))
$(call PROGRAM_RULE_DEBUG,$(1)$(SUFFIX_PROG_DEBUG),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_DEBUG)) $(call GET_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_DEBUG)),$(2),$(call GET_LDFLAGS_FOR_TARGET,$(1),_DEBUG))
$(call PROGRAM_RULE_COVER,$(1)$(SUFFIX_PROG_COVER),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_COVER)) $(call GET_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_COVER)),$(2),$(call GET_LDFLAGS_FOR_TARGET,$(1),_COVER))
endef

$(foreach x,$(INST_PROGRAMS),$(eval $(call INST_PROGRAM_RULES,$(x),$(call GET_DEPS_FOR_TARGET,$(x)))))
$(foreach x,$(NOINST_PROGRAMS) $(TEST_PROGRAMS),$(eval $(call NOINST_PROGRAM_RULES,$(x),$(call GET_DEPS_FOR_TARGET,$(x)))))



# CREATING LIBRARIES

# target, objects_and_libs, extra_deps, extra_flags
define SHARED_LIBRARY_RULE
$(1): $(2) $(3)
        # FIXME: add -Wl,-soname and -Wl,-rpath
	$(strip $(LD_SHARED) $(2) $(LDFLAGS) $(4)) -o $(1)
endef

define STATIC_LIBRARY_RULE
$(1): $(2)
	$(strip $(AR) $(ARFLAGS)) $(1) $(2)
endef

define STATIC_LIBRARY_RULES
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),$(SUFFIX_LIB_STATIC)),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_STATIC)))
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),$(SUFFIX_LIB_DEBUG)),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_DEBUG)))
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),$(SUFFIX_LIB_COVER)),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_COVER)))
endef

define LIBRARY_RULES
$(call SHARED_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),$(SUFFIX_LIB_SHARED)),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_SHARED)),$(2),$(call GET_LDFLAGS_FOR_TARGET,$(1),))
$(call STATIC_LIBRARY_RULES,$(1))
endef

$(foreach x,$(INST_LIBRARIES),$(eval $(call LIBRARY_RULES,$(x),$(call GET_DEPS_FOR_TARGET,$(x)))))
$(foreach x,$(NOINST_LIBRARIES),$(eval $(call STATIC_LIBRARY_RULES,$(x))))



# FLEX AND BISON

%.flex.cpp %.flex.hpp: %.flex $(DEP_MAKEFILES)
	flex --outfile=$*.flex.cpp --header-file=$*.flex.hpp $<

%.bison.cpp %.bison.hpp: %.bison $(DEP_MAKEFILES)
	bison --output=$*.bison.cpp --defines=$*.bison.hpp $<



# COMPILING + AUTOMATIC DEPENDENCIES

%$(SUFFIX_OBJ_SHARED): %.c
	$(strip $(CC_SHARED) $(CFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$*.o,) $(INC_FLAGS) $(CFLAGS_AUTODEP)) -c $< -o $@

%$(SUFFIX_OBJ_SHARED): %.cpp
	$(strip $(CXX_SHARED) $(CXXFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$*.o,) $(INC_FLAGS) $(CFLAGS_AUTODEP)) -c $< -o $@

%$(SUFFIX_OBJ_STATIC): %.c
	$(strip $(CC_STATIC) $(CFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$*.o,) $(INC_FLAGS) $(CFLAGS_AUTODEP)) -c $< -o $@

%$(SUFFIX_OBJ_STATIC): %.cpp
	$(strip $(CXX_STATIC) $(CXXFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$*.o,) $(INC_FLAGS) $(CFLAGS_AUTODEP)) -c $< -o $@

%$(SUFFIX_OBJ_DEBUG): %.c
	$(strip $(CC_DEBUG) $(CFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$*.o,_DEBUG) $(INC_FLAGS) $(CFLAGS_AUTODEP)) -c $< -o $@

%$(SUFFIX_OBJ_DEBUG): %.cpp
	$(strip $(CXX_DEBUG) $(CXXFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$*.o,_DEBUG) $(INC_FLAGS) $(CFLAGS_AUTODEP)) -c $< -o $@

%$(SUFFIX_OBJ_COVER): %.c
	$(strip $(CC_COVERAGE) $(CFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$*.o,_COVER) $(INC_FLAGS_ABS) $(CFLAGS_AUTODEP)) -c $(abspath $<) -o $(abspath $@)

%$(SUFFIX_OBJ_COVER): %.cpp
	$(strip $(CXX_COVERAGE) $(CXXFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$*.o,_COVER) $(INC_FLAGS_ABS) $(CFLAGS_AUTODEP)) -c $(abspath $<) -o $(abspath $@)

-include $(OBJECTS:.o=.d)
