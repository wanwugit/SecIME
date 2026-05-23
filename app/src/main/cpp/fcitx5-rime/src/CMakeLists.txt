set(RIME_SOURCES
    rimestate.cpp
    rimeengine.cpp
    rimecandidate.cpp
    rimesession.cpp
    rimeaction.cpp
    rimefactory.cpp
)

set(RIME_LINK_LIBRARIES
    Fcitx5::Core
    Fcitx5::Config
    ${RIME_TARGET}
)

# DBus and Notifications not available on Android
add_definitions(-DFCITX_RIME_NO_DBUS)

# Build rime as STATIC library linked into native-lib (not MODULE plugin)
add_library(rime STATIC ${RIME_SOURCES})
set_target_properties(rime PROPERTIES FCITX_ADDON TRUE)
target_link_libraries(rime ${RIME_LINK_LIBRARIES})

# Rime is linked as STATIC library into native-lib, override addon type
set(FCITX_ADDON_TYPE "StaticLibrary")

# Install rime addon and inputmethod conf files
configure_file(rime-addon.conf.in.in rime-addon.conf.in @ONLY)
configure_file("${CMAKE_CURRENT_BINARY_DIR}/rime-addon.conf.in" rime-addon.conf)
configure_file(rime.conf.in rime.conf)
install(FILES "${CMAKE_CURRENT_BINARY_DIR}/rime.conf" DESTINATION "${FCITX_INSTALL_PKGDATADIR}/inputmethod" COMPONENT config)
install(FILES "${CMAKE_CURRENT_BINARY_DIR}/rime-addon.conf" RENAME rime.conf DESTINATION "${FCITX_INSTALL_PKGDATADIR}/addon" COMPONENT config)