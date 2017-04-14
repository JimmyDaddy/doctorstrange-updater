'use strict';

import Obj from './obj';

let splitVersion = (versionStr: String) => {
    let numArr = versionStr.split('.');
    return numArr;
}

module.exports = {


    compareVersion: (currentVersion: String, newVersion: String) => {
        if (newVersion && currentVersion) {
            let arr1 = splitVersion(newVersion);
            let arr2 = splitVersion(currentVersion);

            if (!Obj.arrIsEmpty(arr1) && !Obj.arrIsEmpty(arr2) && arr1.length == 3 && arr2.length == 3) {
                if (arr1[0] > arr2[0]) {
                    return true;
                } else if (arr1[0] == arr2[0]) {
                    if (arr1[1] > arr2[1]) {
                        return true;
                    } else if (arr1[1] == arr2[1]) {
                        if (arr1[2] > arr2[2]) {
                            return true;
                        }
                    }
                }
                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }

    },

    compareContainerVersion: (currentContainerVersion: String, newContainerVersion: String) => {
        if (currentContainerVersion && newContainerVersion) {
            let arr1 = splitVersion(newContainerVersion);
            let arr2 = splitVersion(currentContainerVersion);

            if (!Obj.arrIsEmpty(arr1) && !Obj.arrIsEmpty(arr2) && arr1.length == 3 && arr2.length == 3) {
                if (arr2[0] < arr1[0]) {//
                    return false;
                } else if (arr1[0] == arr2[0]) {
                    if (arr2[1] < arr1[1]) {
                        return false;
                    } else if (arr1[1] == arr2[1]) {
                        if (arr2[2] < arr1[2]) {
                            return false;
                        }
                    }
                }
                return true;
            } else {
                return null;
            }
        } else {
            return null;
        }

    }
};
