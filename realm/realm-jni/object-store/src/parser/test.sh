# /bin/sh
llvm-g++ -std=c++11 -I ../../../vendor/PEGTL/ -o test test.cpp parser.cpp
./test
