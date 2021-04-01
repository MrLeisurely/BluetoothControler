package com.example.bloothcontroler.ui.widget

import android.content.ContentResolver
import android.os.Bundle
import com.example.bloothcontroler.ui.widget.entity.Document
import com.example.bloothcontroler.ui.widget.entity.FileType
import droidninja.filepicker.cursors.loadercallbacks.FileMapResultCallback


import java.util.Comparator



object MediaStoreHelper {



    fun getDocs(contentResolver: ContentResolver,
                fileTypes: List<FileType>,
                comparator: Comparator<Document>?,
                fileResultCallback: FileMapResultCallback
    ) {
        DocScannerTask(contentResolver, fileTypes, comparator, fileResultCallback).execute()
    }
}