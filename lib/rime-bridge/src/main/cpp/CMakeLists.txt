cmake_minimum_required(VERSION 3.18)

project(rime-bridge)

add_library(
    rime_jni_bridge
    SHARED
    rime_jni_bridge.cc
)

find_library(log-lib log)

target_link_libraries(
    rime_jni_bridge
    ${log-lib}
)