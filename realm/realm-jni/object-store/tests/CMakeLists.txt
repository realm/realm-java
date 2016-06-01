include_directories(../external/catch/single_include .)

set(HEADERS
    util/index_helpers.hpp
    util/test_file.hpp
)

set(SOURCES
    collection_change_indices.cpp
    index_set.cpp
    list.cpp
    main.cpp
    parser.cpp
    results.cpp
    transaction_log_parsing.cpp
    util/test_file.cpp
)

add_executable(tests ${SOURCES} ${HEADERS})
target_link_libraries(tests realm-object-store)

create_coverage_target(generate-coverage tests)

add_custom_target(run-tests USES_TERMINAL DEPENDS tests COMMAND ./tests)

add_subdirectory(notifications-fuzzer)
