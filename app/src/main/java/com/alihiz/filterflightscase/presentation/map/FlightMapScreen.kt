package com.alihiz.filterflightscase.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alihiz.filterflightscase.R
import com.alihiz.filterflightscase.core.ui.components.ErrorCard
import com.alihiz.filterflightscase.core.ui.components.LoadingIndicator
import com.alihiz.filterflightscase.domain.model.Flight
import com.alihiz.filterflightscase.domain.model.MapBounds
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun FlightMapScreen(
    modifier: Modifier = Modifier,
    viewModel: FlightMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var planeIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Istanbul as default center
    val defaultPosition = remember {
        LatLng(41.0082, 28.9784)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 8f)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
            bounds?.let {
                val mapBounds = MapBounds(
                    minLat = it.southwest.latitude,
                    maxLat = it.northeast.latitude,
                    minLon = it.southwest.longitude,
                    maxLon = it.northeast.longitude
                )
                viewModel.fetchFlights(mapBounds)
            }
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }

    // Initialize plane icon and fetch initial flights
    LaunchedEffect(Unit) {
        planeIcon = createPlaneIcon(context)
        
        // Initial fetch after a short delay to ensure map is ready
        kotlinx.coroutines.delay(300)
        
        // Create initial bounds from default camera position
        val initialBounds = MapBounds(
            minLat = defaultPosition.latitude - 2.0,
            maxLat = defaultPosition.latitude + 2.0,
            minLon = defaultPosition.longitude - 3.0,
            maxLon = defaultPosition.longitude + 3.0
        )
        viewModel.fetchFlights(initialBounds)
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FlightMap(
                cameraPositionState = cameraPositionState,
                flights = state.getDisplayFlights(),
                planeIcon = planeIcon
            )

            TopActionButtons(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                isBottomSheetOpen = showBottomSheet,
                onLayersClick = { showBottomSheet = true },
                onRefreshClick = { viewModel.refresh() }
            )

            if (state.isLoading && !state.hasFlights()) {
                LoadingIndicator()
            }

            state.error?.let { error ->
                ErrorCard(
                    message = error,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            ZoomControls(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 32.dp),
                cameraPositionState = cameraPositionState
            )

            if (state.hasFlights()) {
                FlightCountBadge(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    flightCount = state.getFlightCount(),
                    totalCount = state.getTotalFlightCount(),
                    isLimited = state.isLimited(),
                    selectedCountry = state.selectedCountry
                )
            }
        }

        if (showBottomSheet) {
            CountriesBottomSheet(
                countries = state.availableCountries,
                selectedCountry = state.selectedCountry,
                onCountrySelected = { country ->
                    viewModel.filterByCountry(country)
                    showBottomSheet = false
                },
                onClearFilter = {
                    viewModel.clearFilter()
                    showBottomSheet = false
                },
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

@Composable
private fun FlightMap(
    cameraPositionState: CameraPositionState,
    flights: List<Flight>,
    planeIcon: BitmapDescriptor?,
    modifier: Modifier = Modifier
) {
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.SATELLITE
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false
        )
    ) {
        if (planeIcon != null) {
            flights.forEach { flight ->
                if (flight.hasValidPosition()) {
                    FlightMarker(
                        flight = flight,
                        icon = planeIcon
                    )
                }
            }
        }
    }
}


@Composable
private fun FlightMarker(
    flight: Flight,
    icon: BitmapDescriptor
) {
    val position = LatLng(flight.latitude!!, flight.longitude!!)
    val rotation = flight.trueTrack?.toFloat() ?: 0f

    Marker(
        state = MarkerState(position = position),
        title = flight.getDisplayName(),
        snippet = buildString {
            append("${flight.originCountry}")
            flight.baroAltitude?.let { append("\nAlt: ${it.toInt()}m") }
            flight.velocity?.let { append("\nSpeed: ${it.toInt()}m/s") }
        },
        icon = icon,
        rotation = rotation,
        flat = true
    )
}

@Composable
private fun TopActionButtons(
    isBottomSheetOpen: Boolean,
    onLayersClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FloatingActionButton(
            onClick = onLayersClick,
            modifier = Modifier.size(48.dp),
            containerColor = if (isBottomSheetOpen)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_layers),
                contentDescription = null,
                tint = if (isBottomSheetOpen)
                    Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FloatingActionButton(
            onClick = onRefreshClick,
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ZoomControls(
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FloatingActionButton(
            onClick = {
                val currentZoom = cameraPositionState.position.zoom
                cameraPositionState.move(
                    com.google.android.gms.maps.CameraUpdateFactory.zoomTo(currentZoom + 1f)
                )
            },
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_zoom_in),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        FloatingActionButton(
            onClick = {
                val currentZoom = cameraPositionState.position.zoom
                cameraPositionState.move(
                    com.google.android.gms.maps.CameraUpdateFactory.zoomTo(currentZoom - 1f)
                )
            },
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_zoom_out),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FlightCountBadge(
    flightCount: Int,
    totalCount: Int,
    isLimited: Boolean,
    selectedCountry: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plane),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = if (isLimited) "Showing: $flightCount / $totalCount" else "Flights: $flightCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (isLimited) {
                    Text(
                        text = stringResource(R.string.limited_for_performance),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (selectedCountry != null) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(
                        text = selectedCountry,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountriesBottomSheet(
    countries: List<String>,
    selectedCountry: String?,
    onCountrySelected: (String) -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        CountriesBottomSheetContent(
            countries = countries,
            selectedCountry = selectedCountry,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onCountrySelected = onCountrySelected,
            onClearFilter = onClearFilter,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun CountriesBottomSheetContent(
    countries: List<String>,
    selectedCountry: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCountrySelected: (String) -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredCountries = remember(countries, searchQuery) {
        if (searchQuery.isBlank()) {
            countries
        } else {
            countries.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val groupedCountries = remember(filteredCountries) {
        filteredCountries.groupBy { it.first().uppercaseChar() }
            .toSortedMap()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.countries),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_from_countries)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            if (searchQuery.isBlank()) {
                item {
                    CountryItem(
                        countryName = stringResource(R.string.all_countries),
                        isSelected = selectedCountry == null,
                        onClick = onClearFilter,
                        showDivider = true
                    )
                }
            }

            groupedCountries.forEach { (letter, countriesInGroup) ->
                item {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                items(countriesInGroup) { country ->
                    CountryItem(
                        countryName = country,
                        isSelected = country == selectedCountry,
                        onClick = { onCountrySelected(country) },
                        showDivider = false
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CountryItem(
    countryName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .background(
                    if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else Color.Transparent
                )
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = countryName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = countryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            if (isSelected) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

