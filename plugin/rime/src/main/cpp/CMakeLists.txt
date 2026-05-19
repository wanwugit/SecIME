cmake_minimum_required(VERSION 3.18)

# Host tools (gettext/msgfmt) may be installed via msys64 on Windows
if(CMAKE_HOST_WIN32)
    list(APPEND CMAKE_PROGRAM_PATH "C:/msys64/ucrt64/bin")
endif()

project(fcitx5-android-plugin-rime VERSION ${VERSION_NAME})

# For reproducible build
add_link_options("LINKER:--hash-style=gnu,--build-id=none")

# prefab dependency
find_package(fcitx5 REQUIRED CONFIG)
get_target_property(FCITX5_CMAKE_MODULES fcitx5::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${FCITX5_CMAKE_MODULES} ${CMAKE_MODULE_PATH})

find_package(ECM MODULE)
find_package(Fcitx5Core MODULE)
find_package(Fcitx5Module MODULE)

set(Rime_VERSION "1.12.0")
set(Rime_dependencies
        log # glog links to android log
        "${PREBUILT_DIR}/glog/${ANDROID_ABI}/lib/libglog.a"
        "${PREBUILT_DIR}/leveldb/${ANDROID_ABI}/lib/libleveldb.a"
        "${PREBUILT_DIR}/lua/${ANDROID_ABI}/lib/liblua_static.a"
        "${PREBUILT_DIR}/marisa/${ANDROID_ABI}/lib/libmarisa.a"
        "${PREBUILT_DIR}/opencc/${ANDROID_ABI}/lib/libopencc.a"
        "${PREBUILT_DIR}/yaml-cpp/${ANDROID_ABI}/lib/libyaml-cpp.a"
        )

add_library(Rime_static STATIC IMPORTED)
set_target_properties(Rime_static PROPERTIES
        IMPORTED_LOCATION "${PREBUILT_DIR}/librime/${ANDROID_ABI}/lib/librime.a"
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILT_DIR}/librime/${ANDROID_ABI}/include"
        INTERFACE_LINK_LIBRARIES "${Rime_dependencies}"
        )

# https://github.com/rime/librime/blob/1.9.0/src/rime_api.h#L663
# https://stackoverflow.com/questions/805555/ld-linker-question-the-whole-archive-option
# https://cmake.org/cmake/help/latest/manual/cmake-generator-expressions.7.html#link-features
set(RIME_TARGET $<LINK_LIBRARY:WHOLE_ARCHIVE,Rime_static>)
set(RIME_DATA_DIR "${CMAKE_INSTALL_DATADIR}/rime-data")
add_subdirectory(fcitx5-rime)

install(FILES
        "default.yaml"
        "rime_frost.schema.yaml"
        "rime_frost.dict.yaml"
        "rime_frost_t9.schema.yaml"
        "rime_frost_aux.dict.yaml"
        "rime_frost_aux.schema.yaml"
        "melt_eng.dict.yaml"
        "melt_eng.schema.yaml"
        "melt_eng_t9.schema.yaml"
        "radical_pinyin.dict.yaml"
        "radical_pinyin.schema.yaml"
        "zh-moqi.gram"
        "essay.txt"
        "key_bindings.yaml"
        "punctuation.yaml"
        "symbols_v.yaml"
        "custom_phrase.txt"
        DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data"
        COMPONENT prebuilt-assets
        )

install(DIRECTORY "cn_dicts/" DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/cn_dicts"
        COMPONENT prebuilt-assets
        FILES_MATCHING PATTERN "*.dict.yaml")

install(DIRECTORY "en_dicts/" DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/en_dicts"
        COMPONENT prebuilt-assets
        FILES_MATCHING PATTERN "*")

install(DIRECTORY "lua/" DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/lua"
        COMPONENT prebuilt-assets
        FILES_MATCHING PATTERN "*.lua")

install(DIRECTORY "opencc/" DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/opencc"
        COMPONENT prebuilt-assets
        FILES_MATCHING PATTERN "*")
