find_program(LCOV_PATH lcov)
find_program(GENHTML_PATH genhtml)
find_program(GCOVR_PATH gcovr PATHS ${CMAKE_SOURCE_DIR}/tests)

set(CMAKE_CXX_FLAGS_COVERAGE "-g -O0 -fprofile-arcs -ftest-coverage"
    CACHE STRING "Flags used by the C++ compiler during coverage builds.")
mark_as_advanced(CMAKE_CXX_FLAGS_COVERAGE)

if(CMAKE_BUILD_TYPE STREQUAL "Coverage")
  if(NOT (LCOV_PATH AND GENHTML_PATH AND GCOVR_PATH))
    message(FATAL_ERROR "Generating a coverage report requires lcov and gcovr")
  endif()

  function(create_coverage_target targetname testrunner)
    add_custom_target(${targetname}
      # Clear previous coverage information
      COMMAND ${LCOV_PATH} --directory . --zerocounters

      # Run the tests
      COMMAND ${testrunner}

      # Generate new coverage report
      COMMAND ${LCOV_PATH} --directory . --capture --output-file coverage.info
      COMMAND ${LCOV_PATH} --extract coverage.info '${CMAKE_SOURCE_DIR}/src/*' --output-file coverage.info.cleaned
      COMMAND ${GENHTML_PATH} -o coverage coverage.info.cleaned
      COMMAND ${CMAKE_COMMAND} -E remove coverage.info coverage.info.cleaned

      COMMAND echo Open coverage/index.html in your browser to view the coverage report.

      WORKING_DIRECTORY ${CMAKE_BINARY_DIR}
    )

    add_custom_target(${targetname}-cobertura
      COMMAND ${testrunner}
      COMMAND ${GCOVR_PATH} -x -r ${CMAKE_SOURCE_DIR}/src -o coverage.xml
      COMMAND echo Code coverage report written to coverage.xml

      WORKING_DIRECTORY ${CMAKE_BINARY_DIR}
    )
  endfunction()
else()
  function(create_coverage_target targetname testrunner)
    add_custom_target(${targetname}
      COMMAND echo "Configure with -DCMAKE_BUILD_TYPE=Coverage to generate coverage reports")

    add_custom_target(${targetname}-cobertura
      COMMAND echo "Configure with -DCMAKE_BUILD_TYPE=Coverage to generate coverage reports")
  endfunction()
endif()
