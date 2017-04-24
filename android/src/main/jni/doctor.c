//
// Created by JimmyDaddy on 2017/4/20.
//

#include "bspatch.h"
#include "doctor.h"

JNIEXPORT int JNICALL Java_com_sxc_doctorstrangeupdater_DoctorStrangeUpdater_beginPatch(JNIEnv *env, jclass cls, jstring oldfile, jstring newfile, jstring patchfile)
{

    jboolean isCopy;

    const char *c_oldfilestr = NULL;
    c_oldfilestr = (*env)->GetStringUTFChars(env, oldfile, &isCopy);


    const char *c_newfilestr = NULL;
    c_newfilestr = (*env)->GetStringUTFChars(env, newfile, &isCopy);


    const char *c_patchfilestr = NULL;
    c_patchfilestr = (*env)->GetStringUTFChars(env, patchfile, &isCopy);

    int err = beginPatch(c_oldfilestr, c_newfilestr, c_patchfilestr);

    (*env)->ReleaseStringUTFChars(env, oldfile, c_oldfilestr);
    (*env)->ReleaseStringUTFChars(env, patchfile, c_patchfilestr);
    (*env)->ReleaseStringUTFChars(env, newfile, c_newfilestr);

    return err;
};