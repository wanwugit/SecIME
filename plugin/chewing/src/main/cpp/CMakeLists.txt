cmake_minimum_required(VERSION 3.18)

# Host tools (gettext/msgfmt) may be installed via msys64 on Windows
if(CMAKE_HOST_WIN32)
    list(APPEND CMAKE_PROGRAM_PATH "C:/msys64/ucrt64/bin")
endif()

project(fcitx5-android-plugin-chewing VERSION ${VERSION_NAME})

# For reproducible build
add_link_options("LINKER:--hash-style=gnu,--build-id=none")

# prefab dependency
find_package(fcitx5 REQUIRED CONFIG)
get_target_property(FCITX5_CMAKE_MODULES fcitx5::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${FCITX5_CMAKE_MODULES} ${CMAKE_MODULE_PATH})

find_package(ECM MODULE)
find_package(Fcitx5Core MODULE)
find_package(Fcitx5Module MODULE)

add_library(Chewing_static STATIC IMPORTED)
set_target_properties(Chewing_static PROPERTIES
        IMPORTED_LOCATION "${PREBUILT_DIR}/libchewing/${ANDROID_ABI}/lib/libchewing_capi.a"
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILT_DIR}/libchewing/${ANDROID_ABI}/include/chewing"
        )

set(CHEWING_TARGET Chewing_static)
option(ENABLE_TEST "" OFF)
add_subdirectory(fcitx5-chewing)

install(DIRECTORY "${PREBUILT_DIR}/chewing-dict/" DESTINATION "${FCITX_INSTALL_DATADIR}/libchewing" COMPONENT prebuilt-assets)
