'use strict'

import ReactNative, { NativeEventEmitter } from 'react-native'
import base64 from 'base-64'
import utf8 from 'utf8'

let FSManager = ReactNative.NativeModules.DoctorFSManager

const myEventListener = new NativeEventEmitter(FSManager)

type MkdirOptions = {
  RNFSURLIsExcludedFromBackupKey?: boolean;
};

type Headers = { [name: string]: string };

type DownloadBeginCallbackResult = {
  statusCode: number;     // The HTTP status code
  contentLength: number;  // The total size in bytes of the download resource
  headers: Headers;       // The HTTP response headers from the server
};

type DownloadProgressCallbackResult = {
  contentLength: number;  // The total size in bytes of the download resource
  bytesWritten: number;   // The number of bytes written to the file so far
};

type DownloadFileOptions = {
  fromUrl: string;          // URL to download file from
  toFile: string;           // Local filesystem path to save the file to
  headers?: Headers;        // An object of headers to be passed to the server
  background?: boolean;
  progressDivider?: number;
  begin?: (res: DownloadBeginCallbackResult) => void;
  progress?: (res: DownloadProgressCallbackResult) => void;
};

type DownloadResult = {
  statusCode: number;     // The HTTP status code
  bytesWritten: number;   // The number of bytes written to the file
};

let FS = {
  MainBundlePath: FSManager.MainBundlePath,
  CachesDirectoryPath: FSManager.CachesDirectoryPath,
  DocumentDirectoryPath: FSManager.DocumentDirectoryPath,
  ExternalDirectoryPath: FSManager.ExternalDirectoryPath,
  ExternalStorageDirectoryPath: FSManager.ExternalStorageDirectoryPath,
  TemporaryDirectoryPath: FSManager.TemporaryDirectoryPath,
  LibraryDirectoryPath: FSManager.LibraryDirectoryPath,
  PicturesDirectoryPath: FSManager.PicturesDirectoryPath,

  moveFile (filepath: string, destPath: string): Promise<void> {
    return FSManager.moveFile(filepath, destPath).then(() => void 0)
  },

  mkdir (filepath: string, options: MkdirOptions = {}): Promise<void> {
    return FSManager.mkdir(filepath, options).then(() => void 0)
  },

  readFile (filepath: string, encodingOrOptions?: any): Promise<string> {
    var options = {
      encoding: 'utf8'
    }

    if (encodingOrOptions) {
      if (typeof encodingOrOptions === 'string') {
        options.encoding = encodingOrOptions
      } else if (typeof encodingOrOptions === 'object') {
        options = encodingOrOptions
      }
    }

    return FSManager.readFile(filepath).then((b64) => {
      var contents

      if (options.encoding === 'utf8') {
        contents = utf8.decode(base64.decode(b64))
      } else if (options.encoding === 'ascii') {
        contents = base64.decode(b64)
      } else if (options.encoding === 'base64') {
        contents = b64
      } else {
        throw new Error('Invalid encoding type "' + String(options.encoding) + '"')
      }

      return contents
    })
  },

  copyFile (filepath: string, destPath: string): Promise<void> {
    return FSManager.copyFile(filepath, destPath).then(() => void 0)
  },

  unlink (filepath: string): Promise<void> {
    return FSManager.unlink(filepath).then(() => void 0)
  },

  exists (filepath: string): Promise<boolean> {
    return FSManager.exists(filepath)
  },

  stopDownload (): void {
    FSManager.stopDownload()
  },

  writeFile (filepath: string, contents: string, encodingOrOptions?: any): Promise<void> {
    var b64

    var options = {
      encoding: 'utf8'
    }

    if (encodingOrOptions) {
      if (typeof encodingOrOptions === 'string') {
        options.encoding = encodingOrOptions
      } else if (typeof encodingOrOptions === 'object') {
        options = encodingOrOptions
      }
    }

    if (options.encoding === 'utf8') {
      b64 = base64.encode(utf8.encode(contents))
    } else if (options.encoding === 'ascii') {
      b64 = base64.encode(contents)
    } else if (options.encoding === 'base64') {
      b64 = contents
    } else {
      throw new Error('Invalid encoding type "' + options.encoding + '"')
    }

    return FSManager.writeFile(filepath, b64).then(() => void 0)
  },

  downloadFile (options: DownloadFileOptions): {promise: Promise<DownloadResult> } {
    if (typeof options !== 'object') throw new Error('downloadFile: Invalid value for argument `options`')
    if (typeof options.fromUrl !== 'string') throw new Error('downloadFile: Invalid value for property `fromUrl`')
    if (typeof options.toFile !== 'string') throw new Error('downloadFile: Invalid value for property `toFile`')
    if (options.headers && typeof options.headers !== 'object') throw new Error('downloadFile: Invalid value for property `headers`')
    if (options.background && typeof options.background !== 'boolean') throw new Error('downloadFile: Invalid value for property `background`')
    if (options.progressDivider && typeof options.progressDivider !== 'number') throw new Error('downloadFile: Invalid value for property `progressDivider`')

    var subscriptions = []

    if (options.begin) {
      subscriptions.push(myEventListener.addListener('DownloadBegin', options.begin))
    }

    if (options.progress) {
      subscriptions.push(myEventListener.addListener('DownloadProgress', options.progress))
    }

    var bridgeOptions = {
      fromUrl: options.fromUrl,
      toFile: options.toFile,
      headers: options.headers || {},
      background: !!options.background,
      progressDivider: options.progressDivider || 0
    }
    return {
      promise: FSManager.downloadFile(bridgeOptions).then(res => {
        subscriptions.forEach(sub => sub.remove())
        return res
      })
    }
  },

  uzipFileAtPath (filePath, destination) {
    return FSManager.uzipFileAtPath(filePath, destination)
  }
}

module.exports = FS
