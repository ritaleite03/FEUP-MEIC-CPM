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
        val result = isoDep.transceive(hexStringToByteArray(NFC.CMD_SEL_AID + String.format("%02X", NFC.CARD_AID.length/2) + NFC.CARD_AID))
        val rLen = result.size
        val status = byteArrayOf(result[rLen-2], result[rLen-1])
        if (RES_OK_SW.contentEquals(status))
          listener(result[0].toInt(), result.sliceArray(1..rLen-3)) // received a key (type == 1) or a list (type == 2)
      }
      catch (e: IOException) {
        Log.e("CardReader", "Error communicating with card: $e")
      }
    }
  }
}