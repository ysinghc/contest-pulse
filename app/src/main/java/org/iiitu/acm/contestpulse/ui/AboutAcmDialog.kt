package org.iiitu.acm.contestpulse.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import org.iiitu.acm.contestpulse.databinding.DialogAboutAcmBinding

class AboutAcmDialog(private val context: Context) {

    fun show() {
        val binding = DialogAboutAcmBinding.inflate(LayoutInflater.from(context))

        AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
