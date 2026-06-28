package com.medic.app.nav.star

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream
import java.security.MessageDigest
import java.time.Instant

/** End-to-end night-sky navigation: detect stars → plate solve → true-north heading. */
class StarNavigationPipeline(
    context: Context,
    private val detector: StarDetector = CvStarDetector(),
    private val precomputedSolver: StarSolver = PrecomputedStarSolver(context),
    private val geometrySolver: StarSolver = GeometryStarSolver()
) {
    private val catalog: StarCatalog = StarCatalogLoader.load(context)

    fun process(
        bitmap: Bitmap,
        imageFileName: String?,
        contentHashHex: String,
        devicePitchDeg: Double,
        deviceAzimuthDeg: Double,
        utc: Instant = Instant.now()
    ): StarNavigationResult {
        val detection = detector.detect(bitmap)
        val solveContext = StarSolveContext(
            detection = detection,
            imageFileName = imageFileName,
            contentHashHex = contentHashHex,
            devicePitchDeg = devicePitchDeg,
            deviceAzimuthDeg = deviceAzimuthDeg,
            utc = utc
        )

        val precomputed = precomputedSolver.solve(solveContext, catalog)
        val solve = if (precomputed.success) {
            precomputed
        } else {
            geometrySolver.solve(solveContext, catalog)
        }

        return StarNavigationResult(
            detection = detection,
            solve = solve,
            catalogName = catalog.name
        )
    }

    companion object {
        fun loadBitmap(context: Context, uri: Uri): Bitmap? {
            return context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }

        fun fileNameFromUri(context: Context, uri: Uri): String? {
            val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
            cursor.use {
                if (!it.moveToFirst()) return null
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                return if (idx >= 0) it.getString(idx) else null
            }
        }

        fun hashStream(input: InputStream): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }

        fun hashUri(context: Context, uri: Uri): String {
            return context.contentResolver.openInputStream(uri)?.use { hashStream(it) }.orEmpty()
        }
    }
}

data class StarNavigationResult(
    val detection: StarDetectionResult,
    val solve: StarSolveResult,
    val catalogName: String
) {
    val headingDeg: Double? get() = solve.trueNorthHeadingDeg
}
