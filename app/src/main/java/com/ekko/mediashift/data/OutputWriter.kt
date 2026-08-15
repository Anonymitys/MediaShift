package com.ekko.mediashift.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream

object OutputWriter {

    /**
     * Copy a file to a SAF document tree location.
     * Returns the URI of the newly created file, or null on failure.
     */
    suspend fun copyToDocumentTree(
        context: Context,
        sourceFile: File,
        treeUri: Uri,
        fileName: String,
        mimeType: String
    ): Uri? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val treeDoc = DocumentFile.fromTreeUri(context, treeUri)
                ?: return@withContext null

            val newFile = treeDoc.createFile(mimeType, fileName)
                ?: return@withContext null

            context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(output)
                }
            }

            newFile.uri
        } catch (e: Exception) {
            null
        }
    }
}
