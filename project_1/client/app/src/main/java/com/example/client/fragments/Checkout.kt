package com.example.client.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.example.client.MainActivity2
import com.example.client.MainActivity3
import com.example.client.MainActivity4
import com.example.client.R
import com.example.client.getPrivateKey
import com.example.client.utils.Crypto.CRYPTO_EC_SIGN_ALGO
import java.nio.ByteBuffer
import java.security.Signature
import java.util.UUID
import androidx.lifecycle.lifecycleScope
import com.example.client.actionChallengeVouchers
import com.example.client.actionGetVouchers
import com.example.client.domain.listProducts
import com.example.client.fragments.feedback.ErrorFragment
import com.example.client.utils.Crypto.CRYPTO_RSA_ENC_ALGO
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.crypto.Cipher

/**
 * Opens checkout pop up, for the user to select:
 *      - The use of discount;
 *      - The use of vouchers;
 *      - The payment method (QR-Code or NFC)
 *
 * @param fragment Fragment of the cart list.
 */
fun openCheckout(fragment: CartFragment) {

    val sharedPreferences = fragment.requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    val uuid = sharedPreferences.getString("uuid", null)
    val dialogView = fragment.layoutInflater.inflate(R.layout.popup_payment, null)
    val dialog = AlertDialog.Builder(fragment.requireActivity()).setView(dialogView).create()

    fragment.lifecycleScope.launch {

        // get nonce challenge
        var nonce : UUID? = null
        try {
            val result = JSONObject(actionChallengeVouchers(uuid.toString()))
            nonce = UUID.fromString(result.getString("nonce").toString())
        } catch (_ : Exception) {
            ErrorFragment.newInstance("Error - The server was not available. Try again!").show(fragment.parentFragmentManager, "error_popup")
            return@launch
        }

        val activity = fragment.requireActivity() as MainActivity2
        val entry = activity.fetchEntryRSA()

        val buffer = ByteBuffer.allocate(16).apply {
            putLong(nonce.mostSignificantBits)
            putLong(nonce.leastSignificantBits)
        }
        val encrypted = Cipher.getInstance(CRYPTO_RSA_ENC_ALGO).run {
            init(Cipher.ENCRYPT_MODE, getPrivateKey(entry))
            doFinal(buffer.array())
        }

        var vouchers : JSONArray? = null
        try {
            val result = JSONObject(actionGetVouchers(uuid.toString(), encrypted))
            vouchers = result.getJSONArray("vouchers")

        } catch (_ : Exception) {
            ErrorFragment.newInstance("Error - The server was not available. Try again!").show(fragment.parentFragmentManager, "error_popup")
            return@launch
        }

        val optionsVoucher = arrayListOf<String>("None")
        for(i in 0 until vouchers.length()) {
            val voucher = vouchers.getJSONObject(i)
            optionsVoucher.add(voucher.getString("uuid"))
        }

        val spinnerTypePay: Spinner = dialogView.findViewById<Spinner>(R.id.spinner_type_pay)
        setUpSpinner(fragment, listOf<String>("QR-Code", "NFC"), spinnerTypePay)
        val spinnerDiscount : Spinner = dialogView.findViewById<Spinner>(R.id.spinner_discount)
        setUpSpinner(fragment, listOf<String>("Yes", "No"), spinnerDiscount)
        val spinnerVoucher : Spinner = dialogView.findViewById<Spinner>(R.id.spinner_voucher)
        setUpSpinner(fragment, optionsVoucher, spinnerVoucher)

        setUpClose(dialogView, dialog)
        setUpCheckout(dialogView, fragment, spinnerTypePay, spinnerDiscount, spinnerVoucher)
        dialog.show()

    }
}

/**
 * Sets up the spinner with its options.
 *
 * @param fragment Fragment of the cart list.
 * @param options Options available in the spinner.
 * @param spinner Spinner widget.
 */
private fun setUpSpinner(fragment: CartFragment, options: List<String>, spinner: Spinner){
    val adapter = android.widget.ArrayAdapter(fragment.requireContext(), android.R.layout.simple_spinner_item, options)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = adapter
}

/**
 * Sets up the close button.
 *
 * @param dialogView
 * @param dialog
 */
private fun setUpClose(dialogView : View, dialog : AlertDialog) {
    val btnClose: ImageButton = dialogView.findViewById(R.id.btn_close)
    btnClose.setOnClickListener {
        dialog.dismiss()
    }
}

/**
 * Sets up the checkout button.
 *
 * @param dialogView
 * @param fragment Fragment of the cart list.
 * @param spinnerTypePay Spinner with the options of payment.
 * @param spinnerDiscount Spinner with the options of discount use.
 * @param spinnerVoucher Spinner with the options of vouchers.
 */
private fun setUpCheckout(dialogView: View, fragment : CartFragment, spinnerTypePay : Spinner, spinnerDiscount : Spinner, spinnerVoucher : Spinner) {
    val btnCheckout: Button = dialogView.findViewById(R.id.bt_checkout)
    btnCheckout.setOnClickListener {

        val typePay = spinnerTypePay.selectedItem.toString()
        val discount = spinnerDiscount.selectedItem.toString()
        val voucher = spinnerVoucher.selectedItem.toString()

        var activityType : Class<out Activity> = MainActivity3()::class.java
        var useDiscount = false
        var voucherId : UUID? = null

        if (typePay == "NFC") {
           activityType = MainActivity4()::class.java
        }
        if (discount == "Yes") {
            useDiscount = true
        }
        if (voucher !== "None") {
            voucherId = UUID.fromString(voucher)
        }

        redirectCheckout(activityType, fragment, voucherId, useDiscount)
    }
}

/**
 * Change the activity according with the payment method
 *
 * @param activityType Activity type to change to.
 * @param fragment Fragment of the cart list.
 * @param voucherId Id of the voucher selected (can be null).
 * @param useDiscount Boolean that corresponds to the use of discount.
 */
private fun redirectCheckout(activityType : Class<out Activity>, fragment: CartFragment, voucherId: UUID?, useDiscount: Boolean){
    val sharedPreferences = fragment.requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    val uuid = sharedPreferences.getString("uuid", null)
    val products: List<Pair<UUID, Float>> = listProducts.map { product ->
        product.id to product.price
    }

    if (uuid != null && products.isNotEmpty()) {
        val encryptedTag = messageCheckout(fragment, UUID.fromString(uuid), products, voucherId, useDiscount)
        fragment.startActivity(Intent(fragment.requireActivity(), activityType).apply {
            putExtra("data", encryptedTag)
        })
    }
}

/**
 * Generates a checkout message encoded as a ByteArray for payment processing.
 *
 * The generated message includes the following:
 *      - User ID (UUID)
 *      - A list of products, each with an ID (UUID) and price (Short in cents)
 *      - Whether a discount is applied (Boolean)
 *      - Whether a voucher is applied (Boolean)
 *      - An optional voucher ID (UUID)
 *      - A digital signature (to be added later in the process)
 *
 * @param userId The UUID representing the user's ID.
 * @param products A list of pairs, each containing a product's UUID and its price (in cents).
 * @param voucherId An optional UUID representing the voucher ID (if available).
 * @param useDiscount A boolean flag indicating whether a discount is applied.
 *
 * @return ByteArray representing the generated checkout message. Returns `null` if there is an error during generation.
 */
private fun messageCheckout(fragment: CartFragment, userId: UUID, products: List<Pair<UUID, Float>>, voucherId: UUID?, useDiscount: Boolean): ByteArray? {
    try {
        val limitedProducts = products.take(10)
        val dataLen = 16 + 1 + limitedProducts.size * (16 + 4) + 1 + 1 + if (voucherId != null) 16 else 0
        val message = ByteArray(dataLen)

        ByteBuffer.wrap(message, 0, dataLen).apply {
            putLong(userId.mostSignificantBits)
            putLong(userId.leastSignificantBits)
            put(limitedProducts.size.toByte())
            for ((id, price) in limitedProducts) {
                putLong(id.mostSignificantBits)
                putLong(id.leastSignificantBits)
                putFloat(price)
            }
            put(if (useDiscount) 1 else 0)
            put(if (voucherId != null) 1 else 0)
            voucherId?.let {
                putLong(it.mostSignificantBits)
                putLong(it.leastSignificantBits)
            }
        }

        val activity = fragment.requireActivity() as MainActivity2
        val entry = activity.fetchEntryEC()

        val signature = Signature.getInstance(CRYPTO_EC_SIGN_ALGO).run {
            initSign(getPrivateKey(entry))
            update(message)
            sign()
        }

        val sigLen = signature.size
        val finalMessage = ByteArray(dataLen + sigLen)
        System.arraycopy(message, 0, finalMessage, 0, dataLen)
        System.arraycopy(signature, 0, finalMessage, dataLen, sigLen)
        return finalMessage

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}