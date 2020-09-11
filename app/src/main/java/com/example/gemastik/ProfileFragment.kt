package com.example.gemastik

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment: Fragment() {

    private lateinit var rootView: View
    private var buttonLapor:Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        rootView = inflater.inflate(R.layout.profile_fragment, container, false)
        setHasOptionsMenu(true)

        inisialisasiButtonLapor()

        return rootView
    }

    private fun inisialisasiButtonLapor(){
        buttonLapor = rootView.findViewById(R.id.btnLapor)

        buttonLapor!!.setOnClickListener {
            dispatchTakeVideoIntent()
        }
    }

    companion object{
        var TAG = ProfileFragment::class.java.simpleName
        private const val ARG_POSITION: String = "position"
        const val REQUEST_VIDEO_CAPTURE = 1
        fun newInstance(): ProfileFragment{
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, 1)
            fragment.arguments = args
            return fragment
        }
    }

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(context!!.packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Toast.makeText(context, "BERHASIL", Toast.LENGTH_SHORT).show()
        }
    }
}