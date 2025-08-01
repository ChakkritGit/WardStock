cmake_minimum_required(VERSION 3.10.2)

project("serialport")

add_library(
        serialport
        SHARED
        SerialPort.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        serialport
        ${log-lib}
)
