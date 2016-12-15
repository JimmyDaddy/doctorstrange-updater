/**
 * Created by Rahul Jiresal on 01/22/16.
 */

'use strict';

var React = require('react-native');
var DoctorStrangeUpdater = React.NativeModules.DoctorStrangeUpdater;

type Props = {
  isVisible: boolean;
}

module.exports = {
  jsCodeVersion: function() {
  	return DoctorStrangeUpdater.jsCodeVersion;
  }
};
