cmake_minimum_required(VERSION 3.18)

# Host tools (gettext/msgfmt) may be installed via msys64 on Windows
if(CMAKE_HOST_WIN32)
    list(APPEND CMAKE_PROGRAM_PATH "C:/msys64/ucrt64/bin")
endif()

project(fcitx5-android-plugin-sayura VERSION ${VERSION_NAME})

# For reproducible build
add_link_options("LINKER:--hash-style=gnu,--build-id=none")

# prefab dependency
find_package(fcitx5 REQUIRED CONFIG)
get_target_property(FCITX5_CMAKE_MODULES fcitx5::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${FCITX5_CMAKE_MODULES} ${CMAKE_MODULE_PATH})

find_package(ECM MODULE)
find_package(Fcitx5Core MODULE)
find_package(Fcitx5Module MODULE)

add_subdirectory(fcitx5-sayura)
