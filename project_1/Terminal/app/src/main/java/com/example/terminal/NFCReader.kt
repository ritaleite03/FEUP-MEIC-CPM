package com.example.terminal

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import java.io.IOException

private val RES_OK_SW = hexStringToByteArray("9000")

class NFCReader(private val listener: (Int, ByteArray)->Unit) : NfcAdapter.ReaderCallback {
  override fun onTagDiscovered(tag: Tag) {
    val isoDep = IsoDep.get(tag) // Android smartcard reader emulator
    if (isoDep != null) {
      try {
        isoDep.connect() // establish a connection with the card and send 'select aid' command
        Log.d("Test", "Connected")

        val result = isoDep.transceive(hexStringToByteArray(NFC.CMD_SEL_AID + String.format("%02X", NFC.CARD_AID.length/2) + NFC.CARD_AID))
        val rLen = result.size

        Log.d("Test",result.toString())
        Log.d("Test",result.size.toString())

        val status = byteArrayOf(result[rLen-2], result[rLen-1])
        Log.d("Test",result[0].toInt().toString())
        Log.d("Test", "Result bytes: ${result.joinToString(", ") { it.toUByte().toString(16) }}")

        Log.d("Test",status.toString())
        Log.d("Test",RES_OK_SW.toString())
        if (RES_OK_SW.contentEquals(status)) {
          Log.d("Test","Entrou")
          listener(
            result[0].toInt(),
            result.sliceArray(1..rLen - 3)
          )
        }
      }
      catch (e: IOException) {
        Log.e("CardReader", "Error communicating with card: $e")
      }
    }
  }
}