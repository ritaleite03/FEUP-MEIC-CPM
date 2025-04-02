package com.example.client.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.client.R
import com.example.client.byteArrayToHex
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


class CartFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var btQR = view.findViewById<Button>(R.id.bottom_button_qr)
        var btBar = view.findViewById<Button>(R.id.bottom_button_bar)

        btQR.setOnClickListener { startQRCodeScanner(true) }
        btBar.setOnClickListener { startQRCodeScanner(false) }
    }

    private fun startQRCodeScanner(qr: Boolean = true) {
        val options = ScanOptions().apply {
            if (qr)
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            else
                setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
            setBeepEnabled(false)
            setOrientationLocked(true)
        }
        scanCodeLauncher.launch(options)
    }

    private val scanCodeLauncher = registerForActivityResult(ScanContract()) {
        if (it != null && it.contents != null) {
            val result = it.contents
            val bytes = result.toByteArray(Charsets.ISO_8859_1)
            val bytesHex = byteArrayToHex(bytes)
            val resHex =  "(${bytes.size}) " + bytesHex
            val resUTF = String(bytes, Charsets.UTF_8)
            Toast.makeText(requireContext(), "scanCodeLauncher $result $resHex $resUTF", Toast.LENGTH_SHORT).show()
        }
    }
}