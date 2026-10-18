package com.scamshield.app

import android.content.Context
import android.net.Uri

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object ScreenshotReader {

    suspend fun readText(
        context: Context,
        uri: Uri
    ): String =
        suspendCancellableCoroutine { continuation ->

            val recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            try {

                val image = InputImage.fromFilePath(context, uri)

                recognizer
                    .process(image)
                    .addOnSuccessListener { visionText ->
                        recognizer.close()
                        if (continuation.isActive) {
                            continuation.resume(visionText.text)
                        }
                    }
                    .addOnFailureListener {
                        recognizer.close()
                        if (continuation.isActive) {
                            continuation.resume("")
                        }
                    }

            } catch (_: Exception) {

                recognizer.close()

                if (continuation.isActive) {
                    continuation.resume("")
                }
            }
        }
}