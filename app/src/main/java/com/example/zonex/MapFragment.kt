package com.example.zonex

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.zonex.network.ConquerRequest
import com.example.zonex.network.HexagonDto
import com.example.zonex.network.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "MapFragment"
        private const val CURRENT_USER_ID = 1L
        private const val DEFAULT_ACTIVITY_ID = 1L
        private const val SEARCH_RADIUS_METERS = 600.0

        // Cores de estilização dos hexágonos
        private val COLOR_USER_FILL = Color.parseColor("#5922C55E")     // ~35% opacidade verde
        private val COLOR_USER_STROKE = Color.parseColor("#22C55E")   // Verde nítido
        private val COLOR_ENEMY_FILL = Color.parseColor("#59EF4444")    // ~35% opacidade vermelho
        private val COLOR_ENEMY_STROKE = Color.parseColor("#EF4444")  // Vermelho nítido
        private val COLOR_NEUTRAL_FILL = Color.TRANSPARENT             // Sem preenchimento opaco
        private val COLOR_NEUTRAL_STROKE = Color.parseColor("#4094A3B8") // Contorno grafite/cinza discreto
    }

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var lastKnownLocation: Location? = null
    private var hasCenteredOnUser = false

    // Estado da atividade
    private var isRunning = false
    private var secondsElapsed = 0
    private var totalDistanceMeters = 0f
    private var sessionConqueredZones = 0
    private val timerHandler = Handler(Looper.getMainLooper())

    // Polígonos atualmente renderizados (hex_id -> Polygon)
    private val activePolygons = mutableMapOf<String, Polygon>()

    // Rota da Corrida e Polígonos Fechados (Paper.io)
    private var routePolyline: com.google.android.gms.maps.model.Polyline? = null
    private val routePoints = mutableListOf<LatLng>()
    private var currentLoopDistanceMeters = 0f
    private val conqueredPolygonsHistory = mutableListOf<PolygonOptions>()

    // Elementos da UI
    private var tvTimer: TextView? = null
    private var tvDistance: TextView? = null
    private var tvUserZones: TextView? = null
    private var tvPace: TextView? = null
    private var hudCard: MaterialCardView? = null
    private var toggleGroup: MaterialButtonToggleGroup? = null
    private var btnStartRun: androidx.appcompat.widget.AppCompatButton? = null
    private var fabLocation: FloatingActionButton? = null
    private var btnSettings: FloatingActionButton? = null

    // Solicitação de permissões de GPS
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            enableMyLocationOnMap()
            startLocationUpdates()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissão de GPS necessária para rastrear e conquistar territórios.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                secondsElapsed++
                val hours = secondsElapsed / 3600
                val minutes = (secondsElapsed % 3600) / 60
                val seconds = secondsElapsed % 60
                
                if (hours > 0) {
                    tvTimer?.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    tvTimer?.text = String.format("00:%02d:%02d", minutes, seconds)
                }
                
                // Atualiza pace médio (min/km)
                if (totalDistanceMeters > 0) {
                    val paceSecondsPerKm = (secondsElapsed / (totalDistanceMeters / 1000f)).toInt()
                    val paceMin = paceSecondsPerKm / 60
                    val paceSec = paceSecondsPerKm % 60
                    tvPace?.text = String.format("%d'%02d\" /km", paceMin, paceSec)
                }

                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ajuste da barra de status
        val statusBarSpacer = view.findViewById<View>(R.id.statusBarSpacer)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val spacerParams = statusBarSpacer.layoutParams
            spacerParams.height = systemBars.top
            statusBarSpacer.layoutParams = spacerParams
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Inicializar SupportMapFragment embutido
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Vincular componentes da UI
        tvTimer = view.findViewById(R.id.tvTimer)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvUserZones = view.findViewById(R.id.tvUserZones)
        tvPace = view.findViewById(R.id.tvPace)
        hudCard = view.findViewById(R.id.hudCard)
        toggleGroup = view.findViewById(R.id.toggleGroup)
        btnStartRun = view.findViewById(R.id.btnStartRun)
        fabLocation = view.findViewById(R.id.fabLocation)
        btnSettings = view.findViewById(R.id.btnSettings)

        val btnRun = view.findViewById<MaterialButton>(R.id.btnRun)
        val btnWalk = view.findViewById<MaterialButton>(R.id.btnWalk)

        toggleGroup?.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val context = requireContext()
                if (checkedId == R.id.btnRun) {
                    btnRun.setTextColor(ContextCompat.getColor(context, R.color.zx_text_primary))
                    btnRun.setIconTintResource(R.color.zx_text_primary)
                    btnWalk.setTextColor(ContextCompat.getColor(context, R.color.zx_text_secondary))
                    btnWalk.setIconTintResource(R.color.zx_text_secondary)
                } else if (checkedId == R.id.btnWalk) {
                    btnWalk.setTextColor(ContextCompat.getColor(context, R.color.zx_text_primary))
                    btnWalk.setIconTintResource(R.color.zx_text_primary)
                    btnRun.setTextColor(ContextCompat.getColor(context, R.color.zx_text_secondary))
                    btnRun.setIconTintResource(R.color.zx_text_secondary)
                }
            }
        }

        // Botão flutuante para recentralizar no usuário
        fabLocation?.setOnClickListener {
            lastKnownLocation?.let { loc ->
                val userLatLng = LatLng(loc.latitude, loc.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16.5f))
            } ?: run {
                Toast.makeText(requireContext(), "Obtendo posição GPS...", Toast.LENGTH_SHORT).show()
                checkAndRequestLocationPermission()
            }
        }

        // Ação Iniciar / Finalizar Corrida
        btnStartRun?.setOnClickListener {
            val context = requireContext()
            if (!isRunning) {
                isRunning = true
                toggleGroup?.visibility = View.GONE
                btnSettings?.visibility = View.GONE
                hudCard?.visibility = View.VISIBLE
                fabLocation?.visibility = View.VISIBLE

                btnStartRun?.text = "FINALIZAR ATIVIDADE"
                btnStartRun?.setBackgroundResource(R.drawable.bg_btn_stop_run)
                btnStartRun?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0)

                // Reseta estado da corrida
                secondsElapsed = 0
                totalDistanceMeters = 0f
                currentLoopDistanceMeters = 0f
                sessionConqueredZones = conqueredPolygonsHistory.size
                tvTimer?.text = "00:00:00"
                tvDistance?.text = "0.00 km"
                tvUserZones?.text = "$sessionConqueredZones Conquistadas"
                tvPace?.text = "0'00\" /km"
                
                // Reinicia Rota (Os polígonos conquistados permanecem no mapa e no histórico)
                routePoints.clear()
                routePolyline?.remove()
                routePolyline = googleMap?.addPolyline(
                    com.google.android.gms.maps.model.PolylineOptions()
                        .color(Color.parseColor("#00E5FF")) // Ciano
                        .width(8f) 
                )
                
                timerHandler.post(timerRunnable)

                // Garante que o GPS está ativo
                checkAndRequestLocationPermission()
            } else {
                // CALCULA MÉTRICAS PARA O RESUMO
                val distStr = String.format("%.2f KM", totalDistanceMeters / 1000f)
                val timeStr = tvTimer?.text?.toString() ?: "00:00:00"
                val paceStr = tvPace?.text?.toString() ?: "0'00\" /KM"
                val areaStr = "${sessionConqueredZones * 1250} m²"
                val xpStr = "+${sessionConqueredZones * 350 + (totalDistanceMeters / 10).toInt()} XP"
                val zonesStr = "$sessionConqueredZones ZONA${if (sessionConqueredZones != 1) "S" else ""}"
                
                val victoryDialog = VictoryDialogFragment.newInstance(
                    distance = distStr,
                    time = timeStr,
                    pace = paceStr,
                    xp = xpStr,
                    area = areaStr,
                    zones = zonesStr
                )
                victoryDialog.show(childFragmentManager, "VictoryDialog")

                isRunning = false
                toggleGroup?.visibility = View.VISIBLE
                btnSettings?.visibility = View.VISIBLE
                hudCard?.visibility = View.GONE
                fabLocation?.visibility = View.GONE

                btnStartRun?.text = "INICIAR CORRIDA"
                btnStartRun?.setBackgroundResource(R.drawable.bg_btn_start_run)
                btnStartRun?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_run, 0, 0, 0)
                
                // Remove rota após finalizar (os polígonos ficam preservados via conqueredPolygonsHistory)
                routePolyline?.remove()
                routePolyline = null

                timerHandler.removeCallbacks(timerRunnable)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Aplicar Estilo Sci-Fi Dark Mode
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark)
            )
            if (!success) {
                Log.e(TAG, "Falha na análise do estilo de mapa escuro.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Não foi possível carregar o estilo de mapa: ${e.message}")
        }

        // Configuração de controles da UI do Google Maps
        map.uiSettings.apply {
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = false // Usamos o nosso FAB estilizado
            isCompassEnabled = true
            isZoomControlsEnabled = false
        }

        // Recarregar hexágonos quando o usuário mover ou dar zoom no mapa
        map.setOnCameraIdleListener {
            val target = map.cameraPosition.target
            fetchVisibleTerritories(target.latitude, target.longitude, SEARCH_RADIUS_METERS)
        }

        // Posição inicial padrão (Av. Paulista, São Paulo) enquanto obtém GPS
        val initialCenter = LatLng(-23.561414, -46.655881)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialCenter, 16.0f))

        // Listener para POIs Comerciais (Zonas Patrocinadas)
        map.setOnPoiClickListener { poi ->
            val sponsorDialog = SponsorBottomSheetDialogFragment.newInstance(poi.name)
            sponsorDialog.show(childFragmentManager, "SponsorBottomSheet")
        }

        // Listener para Polígonos de Jogadores
        map.setOnPolygonClickListener { polygon ->
            val areaVal = kotlin.random.Random.nextInt(800, 2500)
            val detailDialog = TerritoryDetailBottomSheet.newInstance(
                zoneName = "Quarteirão Central", // Num app real, usaríamos Reverse Geocoding
                ownerName = "Lincoln Silva",     // Num app real, puxar do dono do Polygon
                area = "Área: Aprox. $areaVal m²",
                level = "Nível 4"
            )
            detailDialog.show(childFragmentManager, "TerritoryDetail")
        }

        // Restaura e desenha os polígonos conquistados salvos em cache
        val savedPolygons = loadConqueredPolygonsLocal()
        savedPolygons.forEach { points ->
            val polygonOptions = PolygonOptions()
                .addAll(points)
                .fillColor(Color.parseColor("#4D00E5FF")) // Ciano translúcido 30%
                .strokeColor(Color.parseColor("#00E5FF")) // Ciano Sólido
                .strokeWidth(8f)
                .zIndex(10f)
                .clickable(true) // Torna clicável
            
            conqueredPolygonsHistory.add(polygonOptions)
            map.addPolygon(polygonOptions)
        }
        
        sessionConqueredZones = conqueredPolygonsHistory.size
        tvUserZones?.text = "$sessionConqueredZones Conquistadas"

        checkAndRequestLocationPermission()
    }

    private fun checkAndRequestLocationPermission() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationOnMap()
            startLocationUpdates()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocationOnMap() {
        googleMap?.isMyLocationEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (locationCallback != null) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L)
            .setMinUpdateDistanceMeters(3.0f) // Requer deslocamento mínimo de 3 metros
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                
                // Filtro de precisão do GPS (ignora instabilidades e desvios irreais)
                if (location.hasAccuracy() && location.accuracy > 18f) return
                
                // Trata a localização durante a corrida
                if (isRunning) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    routePoints.add(latLng)
                    routePolyline?.points = routePoints
                    
                    // Incrementa distância total
                    if (lastKnownLocation != null) {
                        val dist = location.distanceTo(lastKnownLocation!!)
                        totalDistanceMeters += dist
                        currentLoopDistanceMeters += dist
                        tvDistance?.text = String.format("%.2f km", totalDistanceMeters / 1000f)
                    }
                    
                    checkLoopClosure(location)
                }

                lastKnownLocation = location

                // Centraliza no usuário na primeira leitura válida
                if (!hasCenteredOnUser) {
                    hasCenteredOnUser = true
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16.5f))
                    fetchVisibleTerritories(location.latitude, location.longitude, SEARCH_RADIUS_METERS)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    /**
     * Busca os territórios visíveis no backend via Retrofit
     */
    private fun fetchVisibleTerritories(lat: Double, lng: Double, radiusMeters: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getVisibleTerritories(lat, lng, radiusMeters)
                }

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    // Por enquanto ignoramos a renderização baseada em hexágonos do backend,
                    // já que migramos para a lógica de conquista livre em polígonos (Paper.io).
                    Log.i(TAG, "Territórios visíveis (Hexágonos antigos) recebidos: ${data.hexagonos.size}")
                } else {
                    Log.w(TAG, "Erro ao buscar territórios: código ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha de conexão com backend local: ${e.message}")
            }
        }
    }

    /**
     * Paper.io Mechanic: Verifica fechamento do circuito (Loop Closure).
     */
    private fun checkLoopClosure(currentLocation: Location) {
        // Exige um mínimo de 150m percorridos no loop atual para evitar fechamentos acidentais no início
        if (routePoints.size < 3 || currentLoopDistanceMeters < 150f) return

        val startPoint = routePoints.first()
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude, currentLocation.longitude,
            startPoint.latitude, startPoint.longitude,
            results
        )
        val distanceToStart = results[0]

        // Se o usuário voltou para um raio de 20 metros do ponto inicial
        if (distanceToStart < 20f) {
            // Fecha o polígono e renderiza na tela
            val closedPolygonPoints = routePoints.toList()
            createAndRenderPolygon(closedPolygonPoints)
            
            // Envia para o backend
            submitPolygonToApi(closedPolygonPoints, currentLoopDistanceMeters)
            
            // Salva no cache local
            saveConqueredPolygonLocal(closedPolygonPoints)
            
            // Feedback visual/sonoro
            Toast.makeText(requireContext(), "Território Conquistado!", Toast.LENGTH_SHORT).show()
            
            // Atualiza HUD
            sessionConqueredZones++
            tvUserZones?.text = "$sessionConqueredZones Conquistadas"
            
            // Reseta a rota para permitir novas conquistas no mesmo trajeto
            routePoints.clear()
            routePoints.add(LatLng(currentLocation.latitude, currentLocation.longitude))
            routePolyline?.points = routePoints
            currentLoopDistanceMeters = 0f
        }
    }

    /**
     * Renderiza o loop fechado no mapa com a cor Ciano.
     */
    private fun createAndRenderPolygon(points: List<LatLng>) {
        val polygonOptions = PolygonOptions()
            .addAll(points)
            .fillColor(Color.parseColor("#4D00E5FF")) // Ciano translúcido 30%
            .strokeColor(Color.parseColor("#00E5FF")) // Ciano Sólido
            .strokeWidth(8f)
            .zIndex(10f)
            .clickable(true) // Torna clicável
            
        conqueredPolygonsHistory.add(polygonOptions)
        googleMap?.addPolygon(polygonOptions)
    }

    /**
     * Envia o polígono conquistado para a API
     */
    private fun submitPolygonToApi(points: List<LatLng>, distanceCovered: Float) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pointDtos = points.map { com.example.zonex.network.PointDto(it.latitude, it.longitude) }
                val request = com.example.zonex.network.ConquerPolygonRequest(
                    atividadeId = DEFAULT_ACTIVITY_ID,
                    pontos = pointDtos,
                    distanciaMetros = distanceCovered
                )
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.conquerPolygon(
                        userId = CURRENT_USER_ID,
                        request = request
                    )
                }

                if (response.isSuccessful) {
                    Log.i(TAG, "Polígono conquistado e registrado com sucesso no backend!")
                } else {
                    Log.w(TAG, "Falha ao registrar polígono: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro de rede ao enviar polígono: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        stopLocationUpdates()
        activePolygons.clear()
        conqueredPolygonsHistory.clear()
        googleMap = null
    }

    /**
     * Salva o polígono conquistado no cache local
     */
    private fun saveConqueredPolygonLocal(points: List<LatLng>) {
        val prefs = requireContext().getSharedPreferences("zonex_polygons_cache", android.content.Context.MODE_PRIVATE)
        val gson = Gson()
        val existing = loadConqueredPolygonsLocal().toMutableList()
        existing.add(points)
        
        // Mapeia LatLng para PointDto para serialização limpa
        val dtos = existing.map { list -> list.map { com.example.zonex.network.PointDto(it.latitude, it.longitude) } }
        prefs.edit().putString("polygons", gson.toJson(dtos)).apply()
    }

    /**
     * Carrega os polígonos conquistados do cache local
     */
    private fun loadConqueredPolygonsLocal(): List<List<LatLng>> {
        val prefs = requireContext().getSharedPreferences("zonex_polygons_cache", android.content.Context.MODE_PRIVATE)
        val json = prefs.getString("polygons", null) ?: return emptyList()
        val gson = Gson()
        val type = object : TypeToken<List<List<com.example.zonex.network.PointDto>>>() {}.type
        
        return try {
            val dtos: List<List<com.example.zonex.network.PointDto>> = gson.fromJson(json, type) ?: emptyList()
            // Converte de volta para LatLng
            dtos.map { list -> list.map { LatLng(it.latitude, it.longitude) } }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
