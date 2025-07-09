package com.thanes.wardstock.services.jna

import com.sun.jna.Library
import com.sun.jna.Native

interface FvLibrary : Library {
  fun fvInit(configPath: String): Int
  fun fvClose(): Int
  fun fvCaptureImage(imageBuffer: ByteArray, length: Int): Int
  fun fvExtractTemplate(image: ByteArray, templateBuffer: ByteArray): Int
  fun fvVerify(template1: ByteArray, template2: ByteArray): Int

  companion object {
    fun load(): FvLibrary {
      System.loadLibrary("Fv")
      System.loadLibrary("myFv")
      return Native.load("myFv", FvLibrary::class.java)
    }
  }
}