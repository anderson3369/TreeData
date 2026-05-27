package com.orchardlog.treedata

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.orchardlog.treedata.databinding.ActivityMainBinding
import com.orchardlog.treedata.shared.auth.AuthService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LifecycleObserver {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var navController: NavController? = null
    private lateinit var bottomNavView: com.google.android.material.bottomnavigation.BottomNavigationView

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavView = binding.appBarMain.bottomNavView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        
        // No longer using setupActionBarWithNavController to achieve iOS parity with clean top.
        // Titles and actions will be handled by the Compose fragments themselves.
        
        navController?.let {
            bottomNavView.setupWithNavController(it)
        }

        bottomNavView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_more) {
                showMoreMenu()
                false
            } else {
                navController?.let { item.onNavDestinationSelected(it) } ?: false
            }
        }


        // Update login/logout menu visibility based on auth state
        lifecycleScope.launch {
            AuthService.authState.collect { _ ->
                invalidateOptionsMenu()
            }
        }
    }

    private fun showMoreMenu() {
        val view = bottomNavView.findViewById<android.view.View>(R.id.nav_more)
        val popup = androidx.appcompat.widget.PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.main, popup.menu)

        // Update menu visibility based on current auth state
        val isSignedIn = AuthService.isSignedIn
        popup.menu.findItem(R.id.nav_login)?.isVisible = !isSignedIn
        popup.menu.findItem(R.id.nav_logout)?.isVisible = isSignedIn

        popup.setOnMenuItemClickListener { item ->
            navController?.let {
                if (item.itemId == R.id.quit) {
                    finishAndRemoveTask()
                    true
                } else if (item.itemId == R.id.nav_logout) {
                    lifecycleScope.launch { AuthService.signOut() }
                    true
                } else {
                    item.onNavDestinationSelected(it)
                }
            } ?: false
        }
        popup.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isSignedIn = AuthService.isSignedIn
        menu.findItem(R.id.nav_login)?.isVisible = !isSignedIn
        menu.findItem(R.id.nav_logout)?.isVisible = isSignedIn
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        navController?.let {
            if(item.itemId == R.id.nav_farmer || item.itemId == R.id.nav_farm
                || item.itemId == R.id.nav_orchard || item.itemId == R.id.nav_version
                || item.itemId == R.id.nav_login) {
                item.onNavDestinationSelected(it)
            } else if(item.itemId == R.id.quit) {
                finishAndRemoveTask()
            } else if(item.itemId == R.id.nav_logout) {
                lifecycleScope.launch { AuthService.signOut() }
            } else {
                return super.onOptionsItemSelected(item)
            }
        } ?: return super.onOptionsItemSelected(item)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }


}
