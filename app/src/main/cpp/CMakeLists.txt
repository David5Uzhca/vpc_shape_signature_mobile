cmake_minimum_required(VERSION 3.22.1)

project("shapesignatureapp")

# Definimos la ruta a OpenCV relativa a este archivo CMakeLists.txt
# Desde app/src/main/cpp subimos 4 niveles para llegar a la raíz del proyecto:
# 1: main → 2: src → 3: app → 4: Raíz (donde está OpenCV-android-sdk)
if(NOT OpenCV_DIR)
    set(OpenCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../OpenCV-android-sdk/sdk/native/jni")
endif()

find_package(OpenCV REQUIRED)

add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp
        signature.cpp
)

target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
        ${OpenCV_INCLUDE_DIRS}
)

target_link_libraries(${CMAKE_PROJECT_NAME}
        ${OpenCV_LIBS}
        android
        log
)