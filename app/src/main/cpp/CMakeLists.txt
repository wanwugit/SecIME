cmake_minimum_required(VERSION 3.18)

# Host tools (gettext/msgfmt) may be installed via msys64 on Windows
if(CMAKE_HOST_WIN32)
    list(APPEND CMAKE_PROGRAM_PATH "C:/msys64/ucrt64/bin")
endif()

project(fcitx5-android VERSION ${VERSION_NAME})

# For reproducible build
add_link_options("LINKER:--hash-style=gnu,--build-id=none")

# prefab dependency
find_package(fcitx5 REQUIRED CONFIG)
get_target_property(FCITX5_CMAKE_MODULES fcitx5::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${FCITX5_CMAKE_MODULES} ${CMAKE_MODULE_PATH})

find_package(Fcitx5Core MODULE)
find_package(Fcitx5Module MODULE)

find_package(libime REQUIRED CONFIG)
get_target_property(LIBIME_CMAKE_MODULES libime::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${LIBIME_CMAKE_MODULES} ${CMAKE_MODULE_PATH})

find_package(LibIMECore MODULE)
find_package(LibIMETable MODULE)

find_package(fcitx5-lua REQUIRED CONFIG)
find_package(fcitx5-chinese-addons REQUIRED CONFIG)

include("${FCITX_INSTALL_CMAKECONFIG_DIR}/Fcitx5Utils/Fcitx5CompilerSettings.cmake")

add_subdirectory(po)
add_subdirectory(androidfrontend)
add_subdirectory(androidkeyboard)
add_subdirectory(androidnotification)

# prebuilt libuv
set(libuv_DIR "${PREBUILT_DIR}/libuv/${ANDROID_ABI}/lib/cmake/libuv")
find_package(libuv)

# prebuilt boost
list(APPEND CMAKE_FIND_ROOT_PATH "${PREBUILT_DIR}/boost/${ANDROID_ABI}/lib/cmake")
find_package(Boost 1.86.0 REQUIRED COMPONENTS headers iostreams CONFIG)

# --- Rime addon (embedded from plugin:rime) ---
set(Rime_VERSION "1.12.0")
set(RIME_VERSION "${Rime_VERSION}")
set(REQUIRED_FCITX_VERSION "5.1.13")
set(RIME_DATA_DIR "${CMAKE_INSTALL_DATADIR}/rime-data")
add_definitions(-DFCITX_RIME_VERSION="${Rime_VERSION}")
add_definitions(-DRIME_DATA_DIR="${RIME_DATA_DIR}")
add_definitions(-DFCITX_GETTEXT_DOMAIN="fcitx5-rime")
set(Rime_dependencies
        log
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

set(RIME_TARGET $<LINK_LIBRARY:WHOLE_ARCHIVE,Rime_static>)
add_subdirectory(fcitx5-rime/src)

# install rime-data assets
install(FILES
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/default.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/rime_frost.schema.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/rime_frost.dict.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/rime_frost_t9.schema.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/rime_frost_aux.dict.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/rime_frost_aux.schema.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/melt_eng.dict.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/melt_eng.schema.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/melt_eng_t9.schema.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/radical_pinyin.dict.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/radical_pinyin.schema.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/zh-moqi.gram"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/essay.txt"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/key_bindings.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/punctuation.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/symbols_v.yaml"
        "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/custom_phrase.txt"
        DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data"
        COMPONENT prebuilt-assets
        )

install(DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/cn_dicts/" DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/cn_dicts"
        COMPONENT prebuilt-assets
        FILES_MATCHING PATTERN "*.dict.yaml")

install(DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/en_dicts/" DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/en_dicts"
        COMPONENT prebuilt-assets
        FILES_MATCHING PATTERN "*")

install(DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/lua/" DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/lua"
        COMPONENT prebuilt-assets
        FILES_MATCHING PATTERN "*.lua")

install(DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/rime-data/opencc/" DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/opencc"
        COMPONENT prebuilt-assets
        FILES_MATCHING PATTERN "*")

# --- End Rime addon ---

add_library(native-lib SHARED native-lib.cpp androidaddonloader/androidaddonloader.cpp)
target_compile_definitions(native-lib PRIVATE FCITX_RIME_NO_DBUS)
target_include_directories(native-lib PRIVATE
        fcitx5-rime/src
        )
target_link_libraries(native-lib
        log
        libuv::uv_a
        Fcitx5::Utils
        Fcitx5::Config
        Fcitx5::Core
        Fcitx5::Module::QuickPhrase
        Fcitx5::Module::Unicode
        Fcitx5::Module::Clipboard
        Boost::headers
        Boost::iostreams
        LibIME::Table
        $<LINK_LIBRARY:WHOLE_ARCHIVE,rime>
        )

# copy module libraries from dependency lib
add_custom_target(copy-fcitx5-modules
        COMMAND ${CMAKE_COMMAND} -E copy_if_different
        $<TARGET_PROPERTY:fcitx5::Fcitx5Core,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::Fcitx5Config,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::Fcitx5Utils,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:libime::IMECore,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:libime::IMETable,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::clipboard,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::imselector,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::quickphrase,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::spell,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::unicode,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5-lua::luaaddonloader,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5-chinese-addons::table,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5-chinese-addons::scel2org5,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5-chinese-addons::chttrans,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5-chinese-addons::fullwidth,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5-chinese-addons::pinyinhelper,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5-chinese-addons::punctuation,IMPORTED_LOCATION>
        ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        COMMENT "Copying fcitx5 module libraries to :app"
        )

# install prebuilt assets
install(FILES "${PREBUILT_DIR}/spell-dict/en_dict.fscd" DESTINATION "${FCITX_INSTALL_PKGDATADIR}/spell" COMPONENT prebuilt-assets)
install(DIRECTORY "${PREBUILT_DIR}/chinese-addons-data/pinyinhelper" DESTINATION "${FCITX_INSTALL_PKGDATADIR}" COMPONENT prebuilt-assets)
install(DIRECTORY "${PREBUILT_DIR}/libime/table" DESTINATION "${FCITX_INSTALL_PKGDATADIR}" COMPONENT prebuilt-assets)
install(DIRECTORY "${PREBUILT_DIR}/libime/data/" DESTINATION "${FCITX_INSTALL_DATADIR}/libime" COMPONENT prebuilt-assets)
install(DIRECTORY "${PREBUILT_DIR}/opencc/data/" DESTINATION "${FCITX_INSTALL_DATADIR}/opencc" COMPONENT prebuilt-assets)
