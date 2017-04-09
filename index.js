
'use strict';

import ReactNative from 'react-native';

import Obj from './lib/obj';
import 'whatwg-fetch';
import Str from './lib/str';
import FS from './lib/fs';

let DoctorStrangeUpdaterModule = ReactNative.NativeModules.DoctorStrangeUpdater;

/**
 * default value
 */

//default source root
const  SOURCE_ROOT = FS.LibraryDirectoryPath + '/JSCode';
//default version host
const  DEFAULT_VERSION_HOST = 'http://doctorstrange.songxiaocai.org/update/version/selectlatest';
//default data download host
const  DEFAULT_DATA_DOWNLOAD_HOST = 'http://doctorstrange.songxiaocai.org/update/download';

const DEFAULT_OPTIONS = {
    versionHost: DEFAULT_VERSION_HOST,
    downloadHost: DEFAULT_DATA_DOWNLOAD_HOST,
}

class DoctorStrangeUpdater {
    /**
     * constant export
     **/
    //jscode version
    JSCODE_VERSION = DoctorStrangeUpdaterModule.jsCodeVersion;
    //App version
    APP_VERISON = DoctorStrangeUpdaterModule.appVersion;
    //BUNDLEID
    BUNDLEID = DoctorStrangeUpdaterModule.bundleIdentifier;
    //system version
    SYSTEM_VERSION = DoctorStrangeUpdaterModule.systemVersion;
    //buildnumber
    BUILD_NUMBER = DoctorStrangeUpdaterModule.buildNumber;
    //brand
    BRAND = DoctorStrangeUpdaterModule.brand;
    //
    BUNDLE_PATH = SOURCE_ROOT+'/doctor.jsbundle';

    /**
     * constants export end
     **/

    constructor(options){
        if (!Obj.isEmpty(options)) {
            this.options = options;
        } else {
            this.options = DEFAULT_OPTIONS;
        }
        const {versionHost, downloadHost, allowCellularDataUse, showProgress, DEBUG, debugVersionHost, debugDownloadHost} = this.options;
        // set hosts

        if (DEBUG) {
            this.versionHost = debugVersionHost || DEFAULT_VERSION_HOST;
            this.downloadHost = debugDownloadHost || DEFAULT_DATA_DOWNLOAD_HOST;
        } else {
            this.versionHost = versionHost || DEFAULT_VERSION_HOST;
            this.downloadHost = downloadHost || DEFAULT_DATA_DOWNLOAD_HOST;
        }

        //
        //
        typeof allowCellularDataUse == 'boolean'? DoctorStrangeUpdaterModule.allowCellularDataUse(allowCellularDataUse) : null ;
        typeof showProgress == 'boolean'? DoctorStrangeUpdaterModule.showProgress(showProgress) : null;


    }

    /**
     * [checkUpdate checkupdate]
     * @method checkUpdate
     * @return {[type]}    [description]
     * @author jimmy
     */
    checkUpdate = () => {
        this.initSourceDir().then((result) => {
            this.initSuccess = result;
            if (this.initSuccess) {
                let params = {
                    bundleId: this.BUNDLEID,
                    version: this.JSCODE_VERSION,
                }
                fetch(
                    this.versionHost,
                    {
                        method: 'post',
                        headers : {
                            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(params)
                    })
                .then((response) => {
                    console.log(response);
                    if(response.status >= 400) {
                        this.options.checkError && this.options.checkError();
                    }
                    return response;
                })
                .then((response) => {
                    return response.json();
                })
                .then((res) => {
                    console.log(res);
                    this.newVersion = res.version;
                    this.newContainerVersion = res.minContainerVersion;
                    this.patchId = res.patchId;
                    this.currentMetaData = res;
                    if (res.serverUrl) {
                        this.downloadHost = res.serverUrl;
                    }
                    if (Str.compareContainerVersion(this.APP_VERISON, this.newContainerVersion)) {
                        if (Str.compareVersion(this.JSCODE_VERSION, this.newVersion)) {
                            this.currentZipDataExist().then((value) => {
                                if (value && this.patchId) {
                                    this.downLoadPatch()
                                } else {
                                    console.log('sss');
                                    this.downLoad();
                                }
                            });
                        } else {
                            console.log('already updated');
                        }
                    } else {
                        console.log('app version too low please upgrade');
                    }

                })
                .catch((err) => {
                    console.log(err);
                    this.options.checkError && this.options.checkError();
                });
            }
        }).catch((err) => {
            this.initSuccess = false;
            this.options.onError && this.options.onError();
            this.reportError(err, 'init fail');
        });



    }

    reportError = (err, errorMessage) => {
        const {errorReportHost} = this.options;
        if (errorReportHost) {
            let params = {
                error: err,
                errorMessage: errorMessage
            }
            fetch(errorReportHost,
                {
                    method: 'post',
                    headers : {
                        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(params)
                })
        }

    }

    showMessageOnStatusBar = (msg: String, color: String) => {
        DoctorStrangeUpdaterModule.showMessageOnStatusBar(msg, color);
    }
    /**
     * [getDoctorStrangeUpdater get instance]
     * @type {[type]}
     */
    static getDoctorStrangeUpdater = (options) => {

        if (!DoctorStrangeUpdater.instance) {
            DoctorStrangeUpdater.instance = new DoctorStrangeUpdater(options);
        } else {
            if (options && Obj.isDifferent(this.options, options)) {
                DoctorStrangeUpdater.instance = new DoctorStrangeUpdater(options);
            }
        }
        return DoctorStrangeUpdater.instance;
    }

    async initSourceDir(){
        let exists = await FS.exists(SOURCE_ROOT);
        if (!exists) {
            let success = await FS.mkdir(SOURCE_ROOT);
            return success;
        } else {
            return exists;
        }

    }

    reload = () => {
        DoctorStrangeUpdaterModule.reload(this.BUNDLE_PATH);
    }
    /**
     * private
     */
    /**
     * [patch patch file]
     * @method patch
     * @param  {[type]} patchPath   [description]
     * @param  {[type]} originPath  [description]
     * @param  {[type]} destination [description]
     * @return {[type]}             [description]
     * @author jimmy
     */
    patch = (patchPath, originPath, destination) => {
        return DoctorStrangeUpdaterModule.patch(patchPath, originPath, destination);
    }



    downLoadPatch = () => {
        //首先拷贝源文件
        let tempDataFile = SOURCE_ROOT+'/temp.zip';
        FS.copyFile(SOURCE_ROOT+'/doctor.zip', tempDataFile).then((value) => {
            const progress = (data) => {
                const percentage = data.bytesWritten/1024/1024;
                this.options.downloadProgress && this.options.downloadProgress(percentage);
            };
            const begin = (res) => {
                this.options.downloadStart && this.options.downloadStart();
            };
            const progressDivider = 1;
            const downloadDestPath = `${SOURCE_ROOT}/temp.patch`;

            const ret = FS.downloadFile({
                fromUrl: this.downloadHost+'?patchId='+this.patchId+'&bundleId='+this.BUNDLEID,
                toFile: downloadDestPath,
                begin,
                progress,
                background: true,
                progressDivider
            });

            ret.promise.then((res) => {
                this.options.downloadEnd && this.options.downloadEnd();
                this.patch(downloadDestPath, tempDataFile, SOURCE_ROOT+'/doctor.zip')
                .then((value) => {
                    FS.uzipFileAtPath(value, SOURCE_ROOT).then(
                        async (res) => {
                        let success = await FS.unlink(tempDataFile);
                        if (!success) {
                            this.reportError(null, 'remove temp file fail [patch]');
                        }
                        success = await FS.unlink(downloadDestPath);
                        if (!success) {
                            this.reportError(null, 'remove temp.patch file fail [patch]');
                        }
                        FS.exists(res).then((exist) => {
                            if(exist){
                                DoctorStrangeUpdaterModule.setMetaData(this.currentMetaData,DoctorStrangeUpdaterModule.currentMetaDataKey)
                                DoctorStrangeUpdaterModule.setMetaData(DoctorStrangeUpdaterModule.currentMetaData,DoctorStrangeUpdaterModule.previousMetaDataKey)
                                this.BUNDLE_PATH = res;
                                if(this.options.askReload){
                                    this.options.askReload((reload) => {
                                        if (reload) {
                                            this.reload();
                                        }
                                    })
                                } else {
                                    this.reload();
                                }
                            }
                        }).catch((err) => {
                            this.options.onError && this.options.onError();
                            this.reportError(err, 'js bundle not exist error [patch]');
                        });
                    }).catch((err) => {

                        this.options.onError && this.options.onError();
                        this.reportError(err, 'unzipfile error [patch]');
                    })
                }).catch((err) => {
                    this.options.onError && this.options.onError();

                    this.reportError(err, 'patch error');
                });
            }).catch((err) => {
                this.options.downloadError && this.options.downloadError(err);
                this.reportError(err, 'download error');
            });
        }).catch((err) => {
            this.downLoad();
            this.reportError(err, 'copy temp files error');
        });


    }

    downLoad = () => {
        const progress = (data) => {
            const percentage = data.bytesWritten/1024/1024;
            this.options.downloadProgress && this.options.downloadProgress(percentage);
        };
        const begin = (res) => {
            this.options.downloadStart && this.options.downloadStart();
        };
        const progressDivider = 1;
        const downloadDestPath = `${SOURCE_ROOT}/doctor.zip`;
        const ret = FS.downloadFile({
            fromUrl: this.downloadHost+'?version='+this.newVersion+'&bundleId='+this.BUNDLEID,
            toFile: downloadDestPath,
            begin,
            progress,
            background: true,
            progressDivider
        });

        ret.promise.then((res) => {
            this.options.downloadEnd && this.options.downloadEnd();
            FS.uzipFileAtPath(downloadDestPath, SOURCE_ROOT).then((res) => {
                FS.exists(res).then((exist) => {
                    if(exist){
                        this.BUNDLE_PATH = res;
                        DoctorStrangeUpdaterModule.setMetaData(this.currentMetaData,DoctorStrangeUpdaterModule.currentMetaDataKey)
                        DoctorStrangeUpdaterModule.setMetaData(DoctorStrangeUpdaterModule.currentMetaData,DoctorStrangeUpdaterModule.previousMetaDataKey)
                        if(this.options.askReload){
                            this.options.askReload((reload) => {
                                if (reload) {
                                    this.reload();
                                }
                            })
                        } else {
                            this.reload();
                        }
                    }
                }).catch((err) => {
                    this.options.onError && this.options.onError();
                    this.reportError(err, 'js bundle not exist error ');
                });
            }).catch((err) => {
                this.options.onError && this.options.onError();
                this.reportError(err, 'unzipfile error ');
            })
        }).catch((err) => {
            this.options.downloadError && this.options.downloadError(err);
            this.reportError(err, 'download error');
        });
    }

    async currentZipDataExist(){
        return await FS.exists(SOURCE_ROOT+'/doctor.zip');
    }

}
module.exports = DoctorStrangeUpdater;
