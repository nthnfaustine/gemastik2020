package com.example.gemastik

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONObject
import java.net.URL

class ProfileFragment: Fragment() {

    private lateinit var rootView: View
    private var buttonLapor:Button? = null
    private val url:String = "https://api.covid19api.com/summary"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        rootView = inflater.inflate(R.layout.profile_fragment, container, false)
        setHasOptionsMenu(true)

        inisialisasiButtonLapor()
        myAsyncTask().execute(url)

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

    inner class myAsyncTask: AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            //Sebelum jalan
        }

        override fun onProgressUpdate(vararg values: String?) {

            val json = JSONObject(values[0]!!)
            val country = json.optJSONArray("Countries")
            val indonesia = country!!.getJSONObject(77)
            val namanya = indonesia.getString("Country")
            val jumlahKonfirmasi: String = indonesia.getString("TotalConfirmed")
            val jumlahSembuh = indonesia.getString("TotalRecovered")
            val jumlahMati = indonesia.getString("TotalDeaths")

            rootView.findViewById<TextView>(R.id.jumlah_konfirmasi).text = jumlahKonfirmasi
            rootView.findViewById<TextView>(R.id.jumlah_sembuh).text = jumlahSembuh
            rootView.findViewById<TextView>(R.id.jumlah_kematian).text = jumlahMati

            Log.d("NAMA", namanya)
        }

        override fun onPostExecute(result: String?) {
            //Setelah selesai
        }

        override fun doInBackground(vararg params: String?): String {
            val apiResponse = URL(url).readText()
            publishProgress(apiResponse)

            return " "
        }
    }

}