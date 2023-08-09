package com.adirahav.diraleashkaa.ui.base

import androidx.fragment.app.Fragment

abstract class BaseFragment<VM : BaseViewModel?> : Fragment() {

    // view model
    @JvmField
    protected var viewModel: VM? = null

    protected abstract fun createViewModel(): VM
}