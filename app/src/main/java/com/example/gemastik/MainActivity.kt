package com.example.gemastik

// ini Activity yang nge handle 3 fragment, team, history dan scoreboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    // ini inisialisasi analytics biar bisa liat berapa banyak yg login dll
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private lateinit var tb: Toolbar
    private var activeFragment: Int? = null
    private var nextFragment: Int? = null

    // ini yang ngatur buka fragment mana kalo klik apa
    @Suppress("DEPRECATION")
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener{ item ->

        if(item.itemId == R.id.navigation_map && activeFragment != 2){
            tb.title = "MAP"
//            val color = resources.getColor(R.color.colorPrimary)
//            tb.setBackgroundColor(color)
//            tb.setTitleTextColor(resources.getColor(R.color.white))
            val mapFrag = MapFragment.newInstance()
            nextFragment = 2
            openFragment(mapFrag)
            return@OnNavigationItemSelectedListener true
        }

        else if(item.itemId == R.id.navigation_profile && activeFragment != 1){
            tb.title = "PROFILE"
//            val color = resources.getColor(R.color.transparent)
//            tb.setBackgroundColor(color)
//            tb.setTitleTextColor(resources.getColor(R.color.transparent))
            val profileFragment = ProfileFragment.newInstance()
            nextFragment = 1
            openFragment(profileFragment)
            return@OnNavigationItemSelectedListener true
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setContentView(R.layout.activity_main)

        tb = findViewById(R.id.toolbarr)
        setSupportActionBar(tb)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)

        if(savedInstanceState == null){
            toHome()
        }

        tb.title = ""
        tb.setTitleTextColor(resources.getColor(R.color.transparent))

        // ini inisialisasi bottomNavBar nya
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    // fungsi buat pindah fragment
    private fun openFragment(fragment: Fragment){
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        if(activeFragment!! > nextFragment!!){
            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right)
        }
        else if(activeFragment!! < nextFragment!!){
            transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left)
        }
        activeFragment = nextFragment
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // fungsi buat kalo back button dipencet
    override fun onBackPressed() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        val selectedItemId = bottomNavigation.selectedItemId

        if (selectedItemId != R.id.navigation_profile){
            toHome()
            super.onBackPressed()
        }
    }

//    private fun setItem(){
//        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
//        bottomNavigation.selectedItemId = R.id.navigation_team
//        activeFragment = 1
//        nextFragment = 1
//        tb.title = ""
//        tb.setTitleTextColor(resources.getColor(R.color.transparent))
//        openFragment(TeamFragment.newInstance())
//    }

    private fun toHome(){
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.selectedItemId = R.id.navigation_profile
        activeFragment = 1
        nextFragment = 1
        tb.title = "PROFILE"
        tb.setTitleTextColor(resources.getColor(R.color.white))
        openFragment(ProfileFragment.newInstance())
    }
}
