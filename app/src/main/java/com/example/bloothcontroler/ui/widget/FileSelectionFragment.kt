package com.example.bloothcontroler.ui.widget

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns

import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloothcontroler.R
import com.example.bloothcontroler.databinding.FileSelectionFragmentBinding
import com.example.bloothcontroler.ui.widget.entity.Document
import com.example.bloothcontroler.ui.widget.entity.FileType
import com.tbruyelle.rxpermissions2.RxPermissions
import droidninja.filepicker.cursors.loadercallbacks.FileMapResultCallback

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


//文件选择
class FileSelectionFragment : BaseBottomSheetDialogFragment<FileSelectionFragmentBinding>() {
    val TAG = "FileSelectionFragment"
    val REQUEST_SERIES = 0x24
    companion object {

//        type:pdf   doc    ppt  xls   txt
        @JvmStatic
        fun getInstance(type: String = ""): FileSelectionFragment {
            val fragment = FileSelectionFragment()
            var bundle = Bundle()
            bundle.putString("type", type)

            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
//        if (Build.VERSION.SDK_INT >= 29){
            getDialog()?.window?.setLayout(
                0,0
            )
//        }
    }

    override fun initContentView(): Int {
        return R.layout.file_selection_fragment
    }
    lateinit var adapter:FileSelectionAdapter
    var type:String=""
    override fun initView(savedInstanceState: Bundle?) {
        arguments?.getString("type")?.let {
            type = it
            var list = type.split(",")
            PickerManager.clearTypes()
            for (item in list) {
                PickerManager.addDocTypes(item)
            }
        }
        binding.tvTitle.text = "Matched files"
        binding.lbClose.setOnClickListener {
            dismiss()
        }
        context?.let {
            adapter =FileSelectionAdapter(mContext = it)
            binding.rvlList.layoutManager = LinearLayoutManager(it)
            binding.rvlList.adapter = adapter
            /*如果是Android10以上设备，则调用系统文件浏览器*/
//            if (Build.VERSION.SDK_INT >= 29) {
                openFileBrowser()
//            }
//            else {
//                RxPermissions(this).requestEach(Manifest.permission.READ_EXTERNAL_STORAGE).subscribe{ permission ->
//                    if (permission.granted){
//                            MediaStoreHelper.getDocs(it.contentResolver,
//                                    PickerManager.getFileTypes(),
//                                    PickerManager.sortingType.comparator,
//                                    object : FileMapResultCallback {
//                                        override fun onResultCallback(files: Map<FileType, List<Document>>) {
//                                            if(isAdded) {
//                                                var listFile = ArrayList<Document>()
//                                                files.forEach {
//                                                    listFile.addAll(it.value)
//                                                }
//                                                adapter.data = listFile
//                                                if(adapter.itemCount <=0)
//                                                {
//                                                    binding.lpData.visibility = View.VISIBLE
//                                                }
//                                                else
//                                                {
//                                                    binding.lpData.visibility = View.GONE
//                                                }
//                                            }
//                                        }
//                                    }
//                            )
//                    }
//                }
//            }
        }

        if(::adapter.isInitialized)
        {
            adapter.onClickFileSelection = object : OnClickFileSelection {
                override fun onClickItem(item: Document?, position: Int) {
                    if(::obtainFilesAddress.isInitialized)
                    {
                        item?.let { obtainFilesAddress.fileUrls(it) }
                    }
                }
            }
        }
    }
    lateinit var obtainFilesAddress: ObtainFilesAddress

    private fun openFileBrowser(){
        val intent =
//                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .apply {
                            // Provide read access to files and sub-directories in the user-selected
                            // directory.
//                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                        }
        startActivityForResult(intent, REQUEST_SERIES)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SERIES){
            data?.data?.let {
                Log.e(TAG,it.toString())
                dumpImageMetaData(it)
            }
        }
        else if (requestCode == REQUEST_SERIES){
            dismiss()
        }
    }

    /**
     * 处理选中的文件，只支持单文件。如果是多选则只处理第一个
     * 该方法涉及IO操作
     */
    private fun dumpImageMetaData(uri: Uri) {

        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.

        val cursor: Cursor? = context?.contentResolver?.query(
            uri, null, null, null, null, null)
        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {
                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.i(TAG, "Display Name: $displayName")

//                Log.i(TAG, "imageId: $imageId")
                var fileType = getFileType(displayName)
                if (fileType == null){
                    showMsg("Unsupported file type")
                    dismiss()
                    return
                }
                Log.i(TAG, "path: ${uri.path}")
                val basePath = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + File.separator
                var path = basePath + displayName
                var file = File(path)
                if (file.exists()){
                    Log.i(TAG,"File exists")
                }
                var inputStream = context?.contentResolver?.openInputStream(uri)
                var outPutStream = FileOutputStream(file)
                var bufferIn = BufferedInputStream(inputStream)
                var bufferOut = BufferedOutputStream(outPutStream)
                val b = ByteArray(1024)
                while (bufferIn.read(b) != -1){
                    bufferOut.write(b)
                }
                bufferIn.close()
                bufferOut.close()
                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
                val size: String = if (!it.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.i(TAG, "Size: $size")
                val document = Document(0, displayName, path)
                document.fileType = fileType
                document.size = size
                if(::obtainFilesAddress.isInitialized) {
                    obtainFilesAddress.fileUrls(document)
                }
                dismiss()
            }
        }
    }

    private fun getFileType(path: String): FileType? {
        var types = PickerManager.getFileTypes()
        for (index in types.indices) {
            for (string in types[index].extensions) {
                if (path.endsWith(string)) return types[index]
            }
        }
        return null
    }

    private fun showMsg(msg:String){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
    }
}

interface ObtainFilesAddress {
    fun fileUrls(fiels: Document)
}