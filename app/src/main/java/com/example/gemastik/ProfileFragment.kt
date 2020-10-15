package com.example.gemastik

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.android.synthetic.main.ganti_nama.view.*
import kotlinx.android.synthetic.main.profile_fragment.view.*
import org.json.JSONObject
import java.net.URL
import java.util.*

class ProfileFragment: Fragment() {

    private lateinit var rootView: View
    private var buttonLapor:Button? = null
    private val url:String = "https://api.covid19api.com/summary"

    private var mAuth: FirebaseAuth? = null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    private lateinit var tvNama: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
//    private lateinit var locationCallback: LocationCallback

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        rootView = inflater.inflate(R.layout.profile_fragment, container, false)
        setHasOptionsMenu(true)

        mAuth = FirebaseAuth.getInstance()
        inisialisasiButtonLapor()
        MyAsyncTask().execute(url)

        inisialisasiProfile()
        inisialisasiLokasi()
        inisialisasiTanggal()



        return rootView
    }

    private fun inisialisasiButtonLapor(){
        buttonLapor = rootView.findViewById(R.id.btnLapor)

        buttonLapor!!.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    companion object{
        var TAG = ProfileFragment::class.java.simpleName
        private const val ARG_POSITION: String = "position"
//        const val REQUEST_VIDEO_CAPTURE = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        fun newInstance(): ProfileFragment{
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, 1)
            fragment.arguments = args
            return fragment
        }
    }

//    private fun dispatchTakeVideoIntent() {
//        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
//            takeVideoIntent.resolveActivity(context!!.packageManager)?.also {
//                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
//            }
//        }
//    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle("Laporkan")
            builder.setMessage("Tolong ambil gambar dari kerumunan orang")
            builder.setPositiveButton("Ok"){_, _ ->
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
            builder.setNegativeButton("Cancel"){_, _ -> }

        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap

            // TODO: 15/10/20 ml nya

            // Options
            val options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                .enableMultipleObjects()
                .build()

            // Convert bitmap ke firebasevisionimage
            val image = InputImage.fromBitmap(imageBitmap, 0)

            // Detect mukanya
            val detector = ObjectDetection.getClient(options)

            detector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    var personCounter = 0
                    for (obj in detectedObjects) {
                        personCounter += 1
                        obj.boundingBox
                    }
                    Toast.makeText(context, "Person detected: $personCounter", Toast.LENGTH_SHORT).show()
                    Log.d("HASIL", personCounter.toString())
                }
                .addOnFailureListener { e ->
                    Log.d("Result ml", e.toString())
                }
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class MyAsyncTask: AsyncTask<String, String, String>() {

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

    private fun inisialisasiProfile(){
        val currentUser = mAuth!!.currentUser
        tvNama = rootView.textView
        tvNama.setOnClickListener {
            alertDialog()
        }

        myRef.child("Users").child(currentUser!!.uid).child("nama")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val namaPengguna = snapshot.getValue(String::class.java)
                    tvNama.text = namaPengguna
                }
            })
    }

    private fun alertDialog(){
        val currentUser = mAuth!!.currentUser
        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.ganti_nama, null)
        val mBuilder = AlertDialog.Builder((activity as AppCompatActivity))
            .setView(mDialogView)
            .setTitle("Nama anda")
        val mAlertDialog = mBuilder.show()
        mDialogView.okay.setOnClickListener {
            mAlertDialog.dismiss()
            val namaPengguna = mDialogView.dialog_nama.text.toString()
            myRef.child("Users").child(currentUser!!.uid).child("nama").setValue(namaPengguna)
            Toast.makeText(activity, "nama pengguna menjadi: $namaPengguna", Toast.LENGTH_SHORT).show()
        }
        mDialogView.cancel.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    // fungsi buat kasih lokasi, minta permission juga
    private fun inisialisasiLokasi() {
        if (ActivityCompat.checkSelfPermission(rootView.context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity as AppCompatActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MapFragment.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener(activity as AppCompatActivity) { location ->
            if (location != null) {
                lastLocation = location

                val addresses: List<Address>
                val geocoder = Geocoder(activity, Locale.getDefault())

                addresses = geocoder.getFromLocation(
                    lastLocation.latitude,
                    lastLocation.longitude,
                    1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                val city: String = addresses[0].locality
                val state: String = addresses[0].adminArea

                val alamat = "$city, $state"

                val tvLokasi = rootView.findViewById<TextView>(R.id.tv_lokasi)
                tvLokasi.text = alamat
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun inisialisasiTanggal(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR).toString()
        val month = c.get(Calendar.MONTH).toString()
        val day = c.get(Calendar.DATE).toString()
        val dateInString = "$day/$month/$year"

        val tanggal:TextView = rootView.findViewById(R.id.tanggal)
        tanggal.text = "Informasi per $dateInString"
    }


}