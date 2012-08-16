THIS_MAKEFILE := $(lastword $(MAKEFILE_LIST))
ROOT = $(patsubst %/,%,$(dir $(THIS_MAKEFILE)))
CONFIG_MK = $(ROOT)/config.mk

-include $(CONFIG_MK)

SOURCE_DIR ?= src


SUFFIX_OBJ_SHARED = .dyn.o
SUFFIX_OBJ_STATIC = .o
SUFFIX_OBJ_DEBUG  = .dbg.o
SUFFIX_OBJ_COVER  = .cov.o

SUFFIX_LIB_SHARED = .so
SUFFIX_LIB_STATIC = .a
SUFFIX_LIB_DEBUG  = -dbg.a
SUFFIX_LIB_COVER  = -cov.a

SUFFIX_PROG_DEBUG  = -dbg
SUFFIX_PROG_COVER  = -cov



# Functions:

FOLD_TARGET = $(subst .,_,$(subst -,_,$(1)))
GET_LIBRARY_NAME = $(patsubst %.a,%,$(1))$(2)
GET_OBJECTS_FROM_SOURCES = $(patsubst %.c,%$(2),$(patsubst %.cpp,%$(2),$(1)))
GET_OBJECTS_FOR_TARGET = $(call GET_OBJECTS_FROM_SOURCES,$($(call FOLD_TARGET,$(1))_SOURCES),$(2))
GET_NOINST_LIBS_FOR_TARGET = $(foreach x,$($(call FOLD_TARGET,$(1))_NOINST_LIBADD),$(call GET_LIBRARY_NAME,$(x),$(2)))
GET_FLAGS_HELPER = $($(if $(filter undefined,$(origin $(1)$(2))),$(1),$(1)$(2)))
GET_CFLAGS_FOR_OBJECT = $(call GET_FLAGS_HELPER,$(call FOLD_TARGET,$(1))_CFLAGS,$(2))
GET_LDFLAGS_FOR_TARGET = $(call GET_FLAGS_HELPER,$(call FOLD_TARGET,$(1))_LDFLAGS,$(2))
#GET_INST_LIB_TARGETS = $(foreach x,$($(1)_LIBRARIES),$(foreach y,$(SUFFIX_LIB_SHARED) $(SUFFIX_LIB_STATIC) $(SUFFIX_LIB_DEBUG),$(call GET_LIBRARY_NAME,$(x),$(y))))


SOURCE_ROOT   = $(ROOT)/$(SOURCE_DIR)
INC_FLAGS     = -I$(SOURCE_ROOT)
INC_FLAGS_ABS = -I$(abspath $(SOURCE_ROOT))

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
TARGETS_PROG_DEBUG        = $(patsubst %,%$(SUFFIX_PROG_DEBUG),$(INST_PROGRAMS))
TARGETS_PROG_COVER        = $(patsubst %,%$(SUFFIX_PROG_COVER),$(INST_PROGRAMS))
TARGETS_NOINST_PROG       = $(NOINST_PROGRAMS)
TARGETS_NOINST_PROG_DEBUG = $(patsubst %,%$(SUFFIX_PROG_DEBUG),$(NOINST_PROGRAMS))
TARGETS_NOINST_PROG_COVER = $(patsubst %,%$(SUFFIX_PROG_COVER),$(NOINST_PROGRAMS))
TARGETS_TEST_PROG         = $(TEST_PROGRAMS)
TARGETS_TEST_PROG_DEBUG   = $(patsubst %,%$(SUFFIX_PROG_DEBUG),$(TEST_PROGRAMS))
TARGETS_TEST_PROG_COVER   = $(patsubst %,%$(SUFFIX_PROG_COVER),$(TEST_PROGRAMS))

TARGETS_DEFAULT    = $(TARGETS_LIB_SHARED) $(TARGETS_LIB_STATIC) $(TARGETS_LIB_DEBUG) $(TARGETS_NOINST_LIB) $(TARGETS_PROG) $(TARGETS_NOINST_PROG)
TARGETS_DEBUG      = $(TARGETS_LIB_DEBUG) $(TARGETS_NOINST_LIB_DEBUG) $(TARGETS_PROG_DEBUG) $(TARGETS_NOINST_PROG_DEBUG)
TARGETS_COVER      = $(TARGETS_LIB_COVER) $(TARGETS_NOINST_LIB_COVER) $(TARGETS_PROG_COVER) $(TARGETS_NOINST_PROG_COVER)
TARGETS_TEST       = $(TARGETS_LIB_SHARED) $(TARGETS_NOINST_LIB) $(TARGETS_TEST_PROG)
TARGETS_TEST_DEBUG = $(TARGETS_LIB_DEBUG) $(TARGETS_NOINST_LIB_DEBUG) $(TARGETS_TEST_PROG_DEBUG)
TARGETS_TEST_COVER = $(TARGETS_LIB_COVER) $(TARGETS_NOINST_LIB_COVER) $(TARGETS_TEST_PROG_COVER)
TARGETS            = $(TARGETS_LIB_SHARED) $(TARGETS_LIB_STATIC) $(TARGETS_LIB_STATIC_DEBUG) $(TARGETS_LIB_STATIC_COVER) $(TARGETS_NOINST_LIB) $(TARGETS_NOINST_LIB_DEBUG) $(TARGETS_NOINST_LIB_COVER) $(TARGETS_PROG) $(TARGETS_PROG_DEBUG) $(TARGETS_PROG_COVER) $(TARGETS_NOINST_PROG) $(TARGETS_NOINST_PROG_DEBUG) $(TARGETS_NOINST_PROG_COVER) $(TARGETS_TEST_PROG) $(TARGETS_TEST_PROG_DEBUG) $(TARGETS_TEST_PROG_COVER)

RECURSIVE_MODES = default debug cover clean install uninstall test test-debug test-cover

.PHONY: all
all: default

default/local: $(TARGETS_DEFAULT)

debug/local: $(TARGETS_DEBUG)

cover/local: $(TARGETS_COVER)

ifneq ($(strip $(TARGETS)),)
clean/local:
	$(RM) $(strip *.d *.o *.gcno *.gcda $(TARGETS))
endif

install/local: $(TARGETS_DEFAULT)
#	$(INSTALL_LIB) $(call GET_INST_LIB_TARGETS,lib) $(DESTDIR)$(libdir)/
# FIXME: Implement

uninstall/local:
# FIXME: Implement

test/local: $(TARGETS_TEST)
	$(subst ;,,$(subst ; ./,; && ./,$(patsubst %,./%;,$(TARGETS_TEST_PROG))))

test-debug/local: $(TARGETS_TEST_DEBUG)
	$(subst ;,,$(subst ; ./,; && ./,$(patsubst %,./%;,$(TARGETS_TEST_PROG_DEBUG))))

ifneq ($(strip $(TARGETS_TEST_COVER)),)
test-cover/local: $(TARGETS_TEST_COVER)
	$(RM) *.gcda
	$(subst ;,,$(subst ; ./,; && ./,$(patsubst %,./%;,$(TARGETS_TEST_PROG_COVER))))
endif


# Update everything if any makefile has changed
DEP_MAKEFILES = Makefile $(CONFIG_MK) $(THIS_MAKEFILE)
$(OBJECTS) $(TARGETS): $(DEP_MAKEFILES)


# Disable all suffix rules and some interfering implicit pattern rules
.SUFFIXES:
%: %.o
%: %.c
%: %.cpp


# Subdirectories

define SUBDIR_DEP_RULE
ifeq ($(3),.)
subdir/$(1)/$(2): $(2)/local
else
subdir/$(1)/$(2): subdir/$(3)/$(2)
endif
endef

define SUBDIR_MODE_RULES
.PHONY: subdir/$(1)/$(2)
$$(foreach x,$$($$(call FOLD_TARGET,$(1))_DEPENDENCIES),$$(eval $$(call SUBDIR_DEP_RULE,$(1),$(2),$$(x))))
ifeq ($(2),default)
subdir/$(1)/$(2):
	@$$(MAKE) -C $(1)
else
subdir/$(1)/$(2):
	@$$(MAKE) -C $(1) $(2)
endif
endef

SUBDIR_RULES = $(foreach x,$(RECURSIVE_MODES),$(eval $(call SUBDIR_MODE_RULES,$(1),$(x))))

$(foreach x,$(SUBDIRS),$(eval $(call SUBDIR_RULES,$(x))))

define REVUSIVE_MODE_RULES
.PHONY: $(1) $(1)/local
$(1): $(1)/local $$(patsubst %,subdir/%/$(1),$$(SUBDIRS))
endef

$(foreach x,$(RECURSIVE_MODES),$(eval $(call REVUSIVE_MODE_RULES,$(x))))



# LIBRARIES

define SHARED_LIBRARY_RULE
$(1): $(2)
        # FIXME: add -Wl,-soname and -Wl,-rpath
	$(LD_SHARED) $(2) $(3) -o $(1)
endef

define STATIC_LIBRARY_RULE
$(1): $(2)
	$$(AR) $$(ARFLAGS) $(1) $(2)
endef

define STATIC_LIBRARY_RULES
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),$(SUFFIX_LIB_STATIC)),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_STATIC)))
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),$(SUFFIX_LIB_DEBUG)),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_DEBUG)))
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),$(SUFFIX_LIB_COVER)),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_COVER)))
endef

define LIBRARY_RULES
$(call SHARED_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),$(SUFFIX_LIB_SHARED)),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_SHARED)),$(call GET_LDFLAGS_FOR_TARGET,$(1),))
$(call STATIC_LIBRARY_RULES,$(1))
endef

$(foreach x,$(INST_LIBRARIES),$(eval $(call LIBRARY_RULES,$(x))))
$(foreach x,$(NOINST_LIBRARIES),$(eval $(call STATIC_LIBRARY_RULES,$(x))))



# PROGRAMS

define PROGRAM_RULE
$(1): $(2)
	$$(LD_STATIC) $(2) $(3) -o $(1)
endef

define PROGRAM_RULE_DEBUG
$(1): $(2)
	$$(LD_DEBUG) $(2) $(3) -o $(1)
endef

define PROGRAM_RULE_COVER
$(1): $(2)
	$$(LD_COVERAGE) $(2) $(3) -o $(1)
endef

define PROGRAM_RULES
$(call PROGRAM_RULE,$(1),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_STATIC)) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_STATIC)),$(call GET_LDFLAGS_FOR_TARGET,$(1),))
$(call PROGRAM_RULE_DEBUG,$(1)$(SUFFIX_PROG_DEBUG),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_DEBUG)) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_DEBUG)),$(call GET_LDFLAGS_FOR_TARGET,$(1),_DEBUG))
$(call PROGRAM_RULE_COVER,$(1)$(SUFFIX_PROG_COVER),$(call GET_OBJECTS_FOR_TARGET,$(1),$(SUFFIX_OBJ_COVER)) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),$(SUFFIX_LIB_COVER)),$(call GET_LDFLAGS_FOR_TARGET,$(1),_COVER))
endef

$(foreach x,$(PROGRAMS),$(eval $(call PROGRAM_RULES,$(x))))



# Flex and Bison

%.flex.cpp %.flex.hpp: %.flex $(DEP_MAKEFILES)
	flex --outfile=$*.flex.cpp --header-file=$*.flex.hpp $<

%.bison.cpp %.bison.hpp: %.bison $(DEP_MAKEFILES)
	bison --output=$*.bison.cpp --defines=$*.bison.hpp $<



# Compiling + automatic dependencies

%$(SUFFIX_OBJ_SHARED): %.c
	$(CC_SHARED) $(CFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$@,) $(INC_FLAGS) -MMD -MP -c $< -o $@

%$(SUFFIX_OBJ_SHARED): %.cpp
	$(CXX_SHARED) $(CXXFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$@,) $(INC_FLAGS) -MMD -MP -c $< -o $@

%$(SUFFIX_OBJ_STATIC): %.c
	$(CC_STATIC) $(CFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$@,) $(INC_FLAGS) -MMD -MP -c $< -o $@

%$(SUFFIX_OBJ_STATIC): %.cpp
	$(CXX_STATIC) $(CXXFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$@,) $(INC_FLAGS) -MMD -MP -c $< -o $@

%$(SUFFIX_OBJ_DEBUG): %.c
	$(CC_DEBUG) $(CFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$@,_DEBUG) $(INC_FLAGS) -MMD -MP -c $< -o $@

%$(SUFFIX_OBJ_DEBUG): %.cpp
	$(CXX_DEBUG) $(CXXFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$@,_DEBUG) $(INC_FLAGS) -MMD -MP -c $< -o $@

%$(SUFFIX_OBJ_COVER): %.c
	$(CC_COVERAGE) $(CFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$@,_COVER) $(INC_FLAGS_ABS) -MMD -MP -c $(abspath $<) -o $(abspath $@)

%$(SUFFIX_OBJ_COVER): %.cpp
	$(CXX_COVERAGE) $(CXXFLAGS) $(call GET_CFLAGS_FOR_OBJECT,$@,_COVER) $(INC_FLAGS_ABS) -MMD -MP -c $(abspath $<) -o $(abspath $@)

-include $(OBJECTS:.o=.d)
