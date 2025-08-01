package com.thanes.wardstock.services.usb

import java.io.FileDescriptor

object SerialPortJNI {
  init {
    System.loadLibrary("serialport")
  }

  external fun openPort(devicePath: String, baudRate: Int): FileDescriptor?

  external fun closePortFromFileDescriptor(fileDescriptor: FileDescriptor?)
}