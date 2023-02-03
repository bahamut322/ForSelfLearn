cmake_minimum_required(VERSION 3.4.1)

# common
set(lib_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
include_directories(${CMAKE_SOURCE_DIR}/include)
find_library(log-lib log android)
find_library(jnigraphics-lib jnigraphics)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu99 -fvisibility=hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math -flax-vector-conversions")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fvisibility=hidden -fvisibility-inlines-hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math -fno-rtti -fno-exceptions -flax-vector-conversions")
set(CMAKE_LINKER_FLAGS "${CMAKE_LINKER_FLAGS} -Wl,--gc-sections")

# MNN
add_library(mnn SHARED IMPORTED)
set_target_properties(mnn PROPERTIES IMPORTED_LOCATION ${lib_DIR}/${ANDROID_ABI}/libMNN.so)
add_library(mnncore SHARED src/main/cpp/mnnnetnative.cpp src/main/cpp/mnnportraitnative.cpp)
add_definitions(-DMNN_USE_LOGCAT)
target_link_libraries(mnncore mnn ${log-lib} ${jnigraphics-lib})

# BaiduTTS
# 1、添加一个库，在这里可以理解为声明了一个变量，来承载需要引入的库
add_library(
        bd_etts # 表示的是模块名称，可以自己任意取，例如：bd_etts
        SHARED # 这个是固定的，基本上表示共享库
        IMPORTED # 这个也基本上是固定的，表示当前是导入的，跟我们 java 的 import 差不多含义
)
# 2、将第一步声明的库名称(libraryname)，跟具体的库关联到一起
set_target_properties(
        bd_etts # 库的名称
        PROPERTIES IMPORTED_LOCATION # 表示当前库是导入的方式
        ${lib_DIR}/${ANDROID_ABI}/libbd_etts.so # so 动态库的具体路径
)
# 3、这段我也不知道干嘛的，算法引入的时候加了，我不加怕出事
add_definitions(-DMNN_USE_LOGCAT)
target_link_libraries(mnncore bd_etts ${log-lib} ${jnigraphics-lib})

add_library(
        BDSpeechDecoder_V1
        SHARED
        IMPORTED
)
set_target_properties(
        BDSpeechDecoder_V1
        PROPERTIES IMPORTED_LOCATION
        ${lib_DIR}/${ANDROID_ABI}/libBDSpeechDecoder_V1.so
)
add_definitions(-DMNN_USE_LOGCAT)
target_link_libraries(mnncore BDSpeechDecoder_V1 ${log-lib} ${jnigraphics-lib})

add_library(
        gnustl_shared
        SHARED
        IMPORTED
)
set_target_properties(
        gnustl_shared
        PROPERTIES IMPORTED_LOCATION
        ${lib_DIR}/${ANDROID_ABI}/libgnustl_shared.so
)
add_definitions(-DMNN_USE_LOGCAT)
target_link_libraries(mnncore gnustl_shared ${log-lib} ${jnigraphics-lib})
