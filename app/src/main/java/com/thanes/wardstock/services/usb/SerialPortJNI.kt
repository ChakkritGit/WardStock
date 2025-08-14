//package com.thanes.wardstock.services.usb
//
//import java.io.FileDescriptor
//
//object SerialPortJNI {
//  init {
//    System.loadLibrary("serialport")
//  }
//
//  external fun openPort(devicePath: String, baudRate: Int): FileDescriptor?
//
//  external fun closePortFromFileDescriptor(fileDescriptor: FileDescriptor?)
//}

package com.thanes.wardstock.services.usb

object SerialPortJNI {

  init {
    System.loadLibrary("serialport")
  }

  external fun openPort(devicePath: String, baudRate: Int): Int

  external fun readBytes(fd: Int, buffer: ByteArray, length: Int): Int

  external fun writeBytes(fd: Int, data: ByteArray): Int

  external fun closePort(fd: Int)
}