macro(build_fuzzer_variant variant)
    add_executable(${variant} command_file.hpp command_file.cpp ${variant}.cpp)
    target_link_libraries(${variant} realm-object-store)
    set_target_properties(${variant} PROPERTIES
      EXCLUDE_FROM_ALL 1
      EXCLUDE_FROM_DEFAULT_BUILD 1)
endmacro()

build_fuzzer_variant(fuzzer)
build_fuzzer_variant(fuzz-sorted-query)
build_fuzzer_variant(fuzz-unsorted-query)
build_fuzzer_variant(fuzz-sorted-linkview)
build_fuzzer_variant(fuzz-unsorted-linkview)
