/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  Alert
} from 'react-native';

import DoctorstrangeUpdater from 'doctorstrange-updater';

export default class example extends Component {

    constructor(props){
        super(props);
        this.state = {
            showDownLoad: false,
            downloadProgress: 0,
        }
    }

    componentDidMount() {
        const updater = DoctorstrangeUpdater.getDoctorStrangeUpdater({
            DEBUG: __DEV__,
            debugVersionHost: 'http://192.168.0.146:3002/update/version/selectlatest',
            debugDownloadHost: 'http://192.168.0.146:3002/update/download',
            debugErrorReportHost: 'http://192.168.0.146:3002/update/errorreport',
            versionHost: `http://doctorstrange.songxiaocai.org/update/version/selectlatest`,
            downloadHost: `http://doctorstrange.songxiaocai.org/update/download`,
            errorReportHost: `http://doctorstrange.songxiaocai.org/update/errorreport`,
            allowCellularDataUse: true,
            showInfo: true,
            downloadStart: () => {
                this.changeState({
                    showDownLoad: false
                })
            },
            downloadProgress: (progress) => {
                this.changeState({
                    downloadProgress: progress,
                })
            },
            downloadEnd: () => {
                this.changeState({
                    showDownLoad: false
                })
            },
            checkError: () => {
                updater.showMessageOnStatusBar('检查更新出错，请检查您的网络设置');
                // Alert.alert(
                //   '检查更新出错',
                //   '请检查您的网络设置',
                // );
            },
            onError: () => {
                Alert.alert(
                  '更新出错',
                  '请检查您的网络设置，并重启app',
                );
            },
            askReload: (reload) => {
                Alert.alert(
                  '新功能准备就绪,是否立即应用？',
                  null,
                  [
                    {text: '取消', onPress: () => {}},
                    {text: '应用', onPress: () => reload(true)}
                  ]
                );
            },
            alreadyUpdated: () => {
                updater.showMessageOnStatusBar('已全部更新');
            },
            needUpdateApp: () => {
                Alert.alert('当前应用版本过低', '请更新', [
                    {text: '取消', onPress: ()=>{}},
                    {text: '确定', onPress: ()=>{
                        Linking.openURL('http://www.songxiaocai.com/captain/download-ios.html');
                    }},
                ]);
            }
        });

        updater.checkUpdate();
    }

    render() {
        return (
          <View style={styles.container}>
            <Text style={styles.welcome}>
              Welcome to React Native!
            </Text>
            <Text style={styles.instructions}>
              To get started, edit index.android.js
            </Text>
            <Text style={styles.instructions}>
              Double tap R on your keyboard to reload,{'\n'}
              Shake or press menu button for dev menu
            </Text>
            <Text style={styles.instructions, {color: 'red'}}>
                Code Version {DoctorstrangeUpdater.getDoctorStrangeUpdater().JSCODE_VERSION}
            </Text>
          </View>
        );
    }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('example', () => example);
