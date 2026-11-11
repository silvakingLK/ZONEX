package com.example.zonex

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var videoView: VideoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showLoginScreen()
    }

    override fun onResume() {
        super.onResume()
        // Retoma o vídeo se o app voltar do background e estiver na tela de login
        videoView?.start()
    }

    private fun showLoginScreen() {
        setContentView(R.layout.layout_login)
        val mainView = findViewById<View>(R.id.btnLogin).parent.parent as View // Referencia a raiz agora
        
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Removemos o padding global no container do vídeo para ele ficar fullscreen
            // Mas repassamos os insets para a View interna que segura os campos
            val formContainer = findViewById<View>(R.id.tvLogo).parent as View
            formContainer.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuração do Vídeo de Fundo
        videoView = findViewById(R.id.vvBackground)
        val uri = Uri.parse("android.resource://$packageName/${R.raw.bg_login_video}")
        videoView?.setVideoURI(uri)
        
        videoView?.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVolume(0f, 0f) // Mudo
            
            // Trata o scale/aspect ratio para Center Crop sem distorção
            val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
            val screenRatio = videoView!!.width / videoView!!.height.toFloat()
            val scaleX = videoRatio / screenRatio
            if (scaleX >= 1f) {
                videoView?.scaleX = scaleX
            } else {
                videoView?.scaleY = 1f / scaleX
            }
            
            videoView?.start()
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            // Limpa o videoView antes de trocar de tela
            videoView?.stopPlayback()
            videoView = null
            showMainScreen()
        }
    }

    private lateinit var mapFragment: Fragment
    private lateinit var eventsFragment: Fragment
    private lateinit var activitiesFragment: Fragment
    private lateinit var profileFragment: Fragment
    private lateinit var activeFragment: Fragment

    private fun showMainScreen() {
        setContentView(R.layout.layout_main)
        
        val mainRoot = findViewById<View>(R.id.mainRoot)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        
        ViewCompat.setOnApplyWindowInsetsListener(mainRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomNav.setPadding(
                bottomNav.paddingLeft,
                bottomNav.paddingTop,
                bottomNav.paddingRight,
                systemBars.bottom
            )
            insets
        }

        // Inicializa ou recupera Fragments para não destruir o Google Maps
        mapFragment = supportFragmentManager.findFragmentByTag("map") ?: MapFragment()
        eventsFragment = supportFragmentManager.findFragmentByTag("events") ?: EventsFragment()
        activitiesFragment = supportFragmentManager.findFragmentByTag("activities") ?: ActivitiesFragment()
        profileFragment = supportFragmentManager.findFragmentByTag("profile") ?: ProfileFragment()
        
        activeFragment = mapFragment

        if (supportFragmentManager.findFragmentByTag("map") == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                add(R.id.fragmentContainer, activitiesFragment, "activities").hide(activitiesFragment)
                add(R.id.fragmentContainer, eventsFragment, "events").hide(eventsFragment)
                add(R.id.fragmentContainer, mapFragment, "map")
            }.commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> switchFragment(mapFragment)
                R.id.nav_territories -> switchFragment(eventsFragment)
                R.id.nav_activities -> switchFragment(activitiesFragment)
                R.id.nav_profile -> switchFragment(profileFragment)
            }
            true
        }
    }
    
    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()
        activeFragment = fragment
    }

    fun logout() {
        // Limpa os fragments pendentes
        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        // Retorna para a tela de login
        showLoginScreen()
    }
}
