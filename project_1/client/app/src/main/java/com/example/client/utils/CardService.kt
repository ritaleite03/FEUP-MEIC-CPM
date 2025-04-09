package com.example.client.utils

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object Card {
  var contentMessage = ByteArray(0)        /* Card state */
  var type = 0

  private const val CARD_AID = "F010203040"         // AID for this applet service.
  private const val CMD_SEL_AID = "00A40400"        // SmartCard select AID command

  val SELECT_APDU = hexStringToByteArray(CMD_SEL_AID + String.format("%02X", CARD_AID.length/2) + CARD_AID)  /* SELECT AID command */
  val OK_SW = hexStringToByteArray("9000")               // "OK" status word (0x9000)
  val UNKNOWN_CMD_SW = hexStringToByteArray("0000")      // "UNKNOWN" command status word (0X0000)
}

class CardService : HostApduService() {
  override fun processCommandApdu(command: ByteArray, extra: Bundle?): ByteArray {
    return if (Card.type != 0 && Card.SELECT_APDU.contentEquals(command))
      byteArrayOf(Card.type.toByte()) + Card.contentMessage + Card.OK_SW // send content in response to SELECT AID
    else
      Card.UNKNOWN_CMD_SW // APDU command not recognized
  }

  override fun onDeactivated(cause: Int) { // notify the NFCSendActivity that the Card has finished
    val localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
    val broadcastIntent = Intent(Crypto.ACTION_CARD_DONE)
    localBroadcastManager.sendBroadcast(broadcastIntent)
  }
}