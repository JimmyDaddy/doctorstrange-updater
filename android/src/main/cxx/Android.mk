
Local_PATH:=$(call.my-dir)//必须位于文件最开始。用来定位源文件位置，$(call my-dir)返回当前目录的路径

include $(CLEAR_VARS)

APP_DEPRECATED_HEADERS := true // Using Unified Headers

Local_MODEL:= doctorstrange  //此句指定.so文件的名称

LOCAL_SRC_FILES := \

 bspatch.c \

 doctor.c \

 bzlib.c \

 blocksort.c \

 compress.c \

 huffman.c \

 randtable.c \

 decompress.c \


include $(BUILD_SHARED_LIBRARY)//最后加编译