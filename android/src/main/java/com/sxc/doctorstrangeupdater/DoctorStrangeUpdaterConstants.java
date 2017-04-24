package com.sxc.doctorstrangeupdater;

/**
 * Created by jimmydaddy on 2017/4/18.
 */

public class DoctorStrangeUpdaterConstants {

    /**
     * TAG
     */
    public static final String TAG = "[doctor]";

    /**
     * share preference constants
     */
    public static final String DOCTOR_STRANGE_UPDATER_PREFERENCEKEY = "doctorstrange_updater_preference_key";
    //最近一次检查更新时间，备用
    public static final String LAST_CHECKUPDATE_DATE = "doctor_last_check_update_date";
    //当前版本信息
    public static final String CURRENT_JS_CODE_METADATA = "doctor_current_JS_code_metadata";
    //上一次版本信息，保存用于回滚
    public static final String PREVIOUS_JS_CODE_METADATA = "doctor_previous_JS_code_metadata";
    //当前App是否第一次启动
    public static final String APP_VERSION_FIRST_OPEN = "doctor_app_version_first_open";
    //当前js版本是否已经第一次加载过
    public static final String JS_FIRST_LOAD = "doctor_js_first_load";
    //当前js版本第一次加载是否成功
    public static final String JS_FIRST_LOAD_SUCCESS = "doctor_js_first_load_success";
    //上一次回滚的版本号，记录用于避免下载错误版本代码
    public static final String JS_LAST_ROLLBACK_VERSION = "doctor_js_last_roll_back_version";

    public static final String VERSION = "version";

    public static final String DESCRIPTION = "description";

    public static final String MIN_CONTAINER_VERSION = "minContainerVersion";

    public static final String MIN_CONTAINER_BUILD_NUMBER = "minContainerBuidNumber";



    /**
     * path and source names
     */
    //资源根目录
    public static final String SOURCE_ROOT = "JSCode";
    //上一版本资源目录
    public static final String PRE_ROOT = "PreJSCode";
    //jsbundle name
    public static final String BUNDLE_NAME = "doctor.jsbundle";
    //zip
    public static final String ZIP_FILE = "doctor.zip";
    //default metadata name
    public static final String DEFAULT_METADATA_NAME = "metadata.json";
    //default jsbundle asset name
    public static final String DEFAULT_JSBUNDLE_ASSET_NAME = "index.android.bundle";
    //
    public static final String ASSETS_BUNDLE_PREFIX = "assets://";

    public static final String ASSETS_FOLDER = "assets";


    /**
     * error message
     */
    //无法找到包名
    public static final String CAN_NOT_FOUND_PACKAGE_NAME = "[doctor] can not found package name ";
    //version code 错误
    public static final String VERSION_CODE_ERROR = "[doctor] version code error ";
    //folder can not be created
    public static final String FOLDER_CAN_NOT_BE_CREATED = "[doctor] folder can not be created ";
    //failed to remove folder
    public static final String FAILED_TO_REMOVE_FOLDER = "[doctor] failed to remove folder ";
    //failed unzip file
    public static final String FAILED_TO_UNZIP = "[doctor] failed to unzip file";
    //failed to copy assets
    public static final String FAILED_TO_COPY_ASSETS = "[doctor], failed to copy assets";
    //
    public static final String PATCH_FAIL = "[doctor], failed to patch file";
    //
    public static final String PATCH_FILE_NOT_EXIST = "[doctor] patch file not exist";
    //
    public static final String PATCH_ORIGIN_NOT_EXIST = "[doctor] origin file not exist";
    //
    public static final String BACKUP_FOLDER_CREATE_FAILED = "[doctor] back folder create failed";
    //
    public static final String BACKUP_FAILED = "[doctor] backup failed";

    public static final String CAN_NOT_FOUND_BUNDLE = "[doctor] can not found bundle";

    public static final String FAILED_TO_BACK_PRE = "[doctor] failed to back to pre";


    /**
     * success message
     */
    public static final String BACKUP_SUCCESS = "back up success";

    /**
     * color
     */
    public static final String COLOR_SUCCESS = "#1d9c5a";

    public static final String COLOR_ERROR = "#f95372";

    public static final String COLOR_NORMAL = "#66b2ff";

    public static final String COLOR_WARN = "#ff9900";
}
