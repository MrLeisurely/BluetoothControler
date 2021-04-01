package droidninja.filepicker.cursors.loadercallbacks

import com.example.bloothcontroler.ui.widget.entity.Document
import com.example.bloothcontroler.ui.widget.entity.FileType



/**
 * Created by gabriel on 10/2/17.
 */

interface FileMapResultCallback {
    fun onResultCallback(files: Map<FileType, List<Document>>)
}

