//
// Created by JimmyDaddy on 2017/4/22.
//
#include <jni.h>

#ifndef CAPTAIN_DOCTOR_H
#define CAPTAIN_DOCTOR_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT int JNICALL Java_com_sxc_doctorstrangeupdater_DoctorStrangeUpdater_beginPatch(JNIEnv *env, jclass cls,jstring oldfile, jstring newfile, jstring patchfile);

#ifdef __cplusplus
}
#endif

#endif //CAPTAIN_DOCTOR_H
