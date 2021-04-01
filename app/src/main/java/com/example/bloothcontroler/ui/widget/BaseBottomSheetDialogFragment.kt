package com.example.bloothcontroler.ui.widget

import android.content.Context

import android.os.Bundle

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.example.bloothcontroler.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**

 */
abstract class BaseBottomSheetDialogFragment<V : ViewDataBinding> : BottomSheetDialogFragment() {
    lateinit var binding: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Mdialog)
    }

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, initContentView(), container, false)
        initView(savedInstanceState)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasAttached = false
        if (::binding.isInitialized) {
            binding.unbind()
        }
    }
    abstract fun initContentView(): Int
    abstract fun initView(savedInstanceState: Bundle?)

    var cancellable: Boolean = false
    var hasAttached = true
}