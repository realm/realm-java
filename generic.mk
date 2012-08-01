THIS_MAKEFILE := $(lastword $(MAKEFILE_LIST))
ROOT = $(patsubst %/,%,$(dir $(THIS_MAKEFILE)))
CONFIG_MK = $(ROOT)/config.mk

-include $(CONFIG_MK)

SOURCE_DIR ?= src



# Functions:

FOLD_TARGET = $(subst .,_,$(subst -,_,$(1)))
GET_LIBRARY_NAME = $(patsubst %.a,%,$(1))$(2)
GET_OBJECTS_FROM_SOURCES = $(patsubst %.c,%$(2),$(patsubst %.cpp,%$(2),$(1)))
GET_OBJECTS_FOR_TARGET = $(call GET_OBJECTS_FROM_SOURCES,$($(call FOLD_TARGET,$(1))_SOURCES),$(2))
GET_NOINST_LIBS_FOR_TARGET = $(foreach x,$($(call FOLD_TARGET,$(1))_NOINST_LIBADD),$(call GET_LIBRARY_NAME,$(x),$(2)))
GET_LDFLAGS_HELPER = $($(if $(filter undefined,$(origin $(1)$(2))),$(1),$(1)$(2)))
GET_LDFLAGS_FOR_TARGET = $(call GET_LDFLAGS_HELPER,$(call FOLD_TARGET,$(1))_LDFLAGS,$(2))



SOURCE_ROOT   = $(ROOT)/$(SOURCE_DIR)
INC_FLAGS     = -I$(SOURCE_ROOT)
INC_FLAGS_ABS = -I$(abspath $(SOURCE_ROOT))

LIBRARIES = $(lib_LIBRARIES) $(NOINST_LIBRARIES)
PROGRAMS  = $(bin_PROGRAMS) $(NOINST_PROGRAMS) $(TEST_PROGRAMS)

OBJECTS_SHARED = $(foreach x,$(lib_LIBRARIES),$(call GET_OBJECTS_FOR_TARGET,$(x),.dyn.o))
OBJECTS_STATIC = $(foreach x,$(LIBRARIES) $(PROGRAMS),$(call GET_OBJECTS_FOR_TARGET,$(x),.o))
OBJECTS_DEBUG  = $(foreach x,$(LIBRARIES) $(PROGRAMS),$(call GET_OBJECTS_FOR_TARGET,$(x),.dbg.o))
OBJECTS_COVER  = $(foreach x,$(LIBRARIES) $(TEST_PROGRAMS),$(call GET_OBJECTS_FOR_TARGET,$(x),.cov.o))
OBJECTS = $(sort $(OBJECTS_SHARED) $(OBJECTS_STATIC) $(OBJECTS_DEBUG) $(OBJECTS_COVER))

TARGETS_LIB_SHARED        = $(foreach x,$(lib_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),.so))
TARGETS_LIB_STATIC        = $(foreach x,$(lib_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),.a))
TARGETS_LIB_DEBUG         = $(foreach x,$(lib_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),-dbg.a))
TARGETS_LIB_COVER         = $(foreach x,$(lib_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),-cov.a))
TARGETS_NOINST_LIB        = $(foreach x,$(NOINST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),.a))
TARGETS_NOINST_LIB_DEBUG  = $(foreach x,$(NOINST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),-dbg.a))
TARGETS_NOINST_LIB_COVER  = $(foreach x,$(NOINST_LIBRARIES),$(call GET_LIBRARY_NAME,$(x),-cov.a))
TARGETS_PROG              = $(bin_PROGRAMS)
TARGETS_PROG_DEBUG        = $(patsubst %,%-dbg,$(bin_PROGRAMS))
TARGETS_PROG_COVER        = $(patsubst %,%-cov,$(bin_PROGRAMS))
TARGETS_NOINST_PROG       = $(NOINST_PROGRAMS)
TARGETS_NOINST_PROG_DEBUG = $(patsubst %,%-dbg,$(NOINST_PROGRAMS))
TARGETS_NOINST_PROG_COVER = $(patsubst %,%-cov,$(NOINST_PROGRAMS))
TARGETS_TEST_PROG         = $(TEST_PROGRAMS)
TARGETS_TEST_PROG_DEBUG   = $(patsubst %,%-dbg,$(TEST_PROGRAMS))
TARGETS_TEST_PROG_COVER   = $(patsubst %,%-cov,$(TEST_PROGRAMS))

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
$(OBJECTS) $(TARGETS): Makefile $(CONFIG_MK) $(THIS_MAKEFILE)




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
	$(LD_SHARED) $(2) -o $(1)
endef

define STATIC_LIBRARY_RULE
$(1): $(2)
	$$(AR) $$(ARFLAGS) $(1) $(2)
endef

define STATIC_LIBRARY_RULES
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),.a),$(call GET_OBJECTS_FOR_TARGET,$(1),.o))
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),-dbg.a),$(call GET_OBJECTS_FOR_TARGET,$(1),.dbg.o))
$(call STATIC_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),-cov.a),$(call GET_OBJECTS_FOR_TARGET,$(1),.cov.o))
endef

define LIBRARY_RULES
$(call SHARED_LIBRARY_RULE,$(call GET_LIBRARY_NAME,$(1),.so),$(call GET_OBJECTS_FOR_TARGET,$(1),.dyn.o))
$(call STATIC_LIBRARY_RULES,$(1))
endef

$(foreach x,$(lib_LIBRARIES),$(eval $(call LIBRARY_RULES,$(x))))
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
$(call PROGRAM_RULE,$(1),$(call GET_OBJECTS_FOR_TARGET,$(1),.o) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),.a),$(call GET_LDFLAGS_FOR_TARGET,$(1),))
$(call PROGRAM_RULE_DEBUG,$(1)-dbg,$(call GET_OBJECTS_FOR_TARGET,$(1),.dbg.o) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),-dbg.a),$(call GET_LDFLAGS_FOR_TARGET,$(1),_DEBUG))
$(call PROGRAM_RULE_COVER,$(1)-cov,$(call GET_OBJECTS_FOR_TARGET,$(1),.cov.o) $(call GET_NOINST_LIBS_FOR_TARGET,$(1),-cov.a),$(call GET_LDFLAGS_FOR_TARGET,$(1),_COVER))
endef

$(foreach x,$(PROGRAMS),$(eval $(call PROGRAM_RULES,$(x))))



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
