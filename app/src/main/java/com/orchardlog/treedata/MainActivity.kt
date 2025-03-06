package com.orchardlog.treedata

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.orchardlog.treedata.databinding.ActivityMainBinding
import com.orchardlog.treedata.ui.data.model.UserPreferencesViewModel
import com.orchardlog.treedata.utils.RoomBackUp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LifecycleObserver {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var navController: NavController? = null
    private var login = false
    private var logout = true
    private var isLoggedIn = false
    private val userPreferencesViewModel: UserPreferencesViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var backupDate: Long? = null
    private var uid: String? = null

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        binding.appBarMain.toolbar.showOverflowMenu()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_farmer, R.id.nav_farm, R.id.nav_orchard,
                R.id.nav_irrigation,
                R.id.nav_pesticideApplication, R.id.nav_fertilizerApplication,R.id.nav_map,
                R.id.nav_orchardTask
            ), drawerLayout
        )
        setupActionBarWithNavController(navController!!, appBarConfiguration)
        navView.setupWithNavController(navController!!)


        auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener {
            invalidateOptionsMenu()
            if(it.currentUser == null || it.currentUser!!.isAnonymous) {
                login = true
                logout = false
            } else {
                login = false
                logout = true
                uid = it.currentUser!!.uid
                isLoggedIn = true
            }
        }

        lifecycleScope.launch {
            userPreferencesViewModel.setBackupDate()
            userPreferencesViewModel.getBackupDate().observe(this@MainActivity) {
                backupDate = it
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        invalidateOptionsMenu()
        val loginItem = menu.findItem(R.id.nav_login)
        loginItem.isVisible = login
        val logoutItem = menu.findItem(R.id.nav_logout)
        logoutItem.isVisible = logout

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.nav_farmer || item.itemId == R.id.nav_farm
            || item.itemId == R.id.nav_orchard || item.itemId == R.id.nav_version
            || item.itemId == R.id.nav_login) {
            item.onNavDestinationSelected(navController!!)
        } else if(item.itemId == R.id.quit) {
            finishAndRemoveTask()
        } else if(item.itemId == R.id.nav_logout) {
            auth.signOut()
        } else {
            return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()
        if(backupDate != null && isLoggedIn) {
            RoomBackUp.backupDatabase(this@MainActivity, backupDate!!,uid)
        }
    }

    override fun onStop() {
        super.onStop()
        if(backupDate != null && isLoggedIn) {
            RoomBackUp.backupDatabase(this@MainActivity, backupDate!!, uid)
        }
    }


}