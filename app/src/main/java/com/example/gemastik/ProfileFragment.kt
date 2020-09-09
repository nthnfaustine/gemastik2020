package com.example.gemastik

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ProfileFragment: Fragment() {

    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        rootView = inflater.inflate(R.layout.profile_fragment, container, false)
        setHasOptionsMenu(true)
        return rootView
    }

    companion object{
        var TAG = ProfileFragment::class.java.simpleName
        private const val ARG_POSITION: String = "position"
        fun newInstance(): ProfileFragment{
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, 1)
            fragment.arguments = args
            return fragment
        }
    }
}