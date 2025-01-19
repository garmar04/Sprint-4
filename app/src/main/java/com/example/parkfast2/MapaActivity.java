package com.example.parkfast2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private GoogleMap mMap;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);

        // Inicializar elementos de la interfaz
        searchBar = findViewById(R.id.search_bar);
        ImageView reservasImageView = findViewById(R.id.reservas);
        ImageView cuentaImageView = findViewById(R.id.cuenta);

        // Configurar clics en iconos para abrir actividades
        reservasImageView.setOnClickListener(v -> {
            Intent intent = new Intent(MapaActivity.this, ReservasActivity.class);
            startActivity(intent);
        });

        cuentaImageView.setOnClickListener(v -> {
            Intent intent = new Intent(MapaActivity.this, CuentaActivity.class);
            startActivity(intent);
        });

        // Configurar barra de búsqueda
        configurarBarraDeBusqueda();

        // Configurar fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (hasLocationPermission()) {
            configurarMapa();
        } else {
            requestLocationPermission();
        }
    }

    private void configurarMapa() {
        // Establecer la ubicación inicial en Playa de Gandía
        LatLng playaDeGandia = new LatLng(39.001667, -0.166111);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playaDeGandia, 14));

        // Añadir marcadores de ejemplo
        LatLng parking1 = new LatLng(39.002500, -0.168000);
        LatLng parking2 = new LatLng(38.995123, -0.160942);

        Objects.requireNonNull(mMap.addMarker(new MarkerOptions()
                        .position(parking1)
                        .title("Parking Playa de Gandía - Parking 1")
                        .snippet("Cerca de la playa, capacidad para 3 coches")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))))
                .setTag("parking1");

        Objects.requireNonNull(mMap.addMarker(new MarkerOptions()
                        .position(parking2)
                        .title("Parking Playa de Gandía - Parking 2")
                        .snippet("Cerca de la playa, capacidad para 3 coches")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))))
                .setTag("parking2");

        // Configurar clic en marcadores
        mMap.setOnInfoWindowClickListener(marker -> {
            if ("parking1".equals(marker.getTag())) {
                abrirReservaActivity1();
            } else if ("parking2".equals(marker.getTag())) {
                abrirReservaActivity2();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configurarBarraDeBusqueda() {
        // Detectar clic en íconos de búsqueda o limpiar texto
        searchBar.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableStart = searchBar.getCompoundDrawables()[0]; // Lupa
                if (drawableStart != null && event.getRawX() <= (searchBar.getLeft() + drawableStart.getBounds().width())) {
                    if (hasLocationPermission()) {
                        buscarUbicacion();
                    } else {
                        requestLocationPermission();
                    }
                    return true;
                }
                Drawable drawableEnd = searchBar.getCompoundDrawables()[2]; // X
                if (drawableEnd != null && event.getRawX() >= (searchBar.getRight() - drawableEnd.getBounds().width())) {
                    searchBar.setText("");
                    return true;
                }
            }
            return false;
        });

        // Mostrar u ocultar la "X" según el texto
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                @SuppressLint("UseCompatLoadingForDrawables") Drawable drawableEnd = s.length() > 0 ?
                        getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel) : null;
                searchBar.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(android.R.drawable.ic_menu_search),
                        null,
                        drawableEnd,
                        null
                );
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void buscarUbicacion() {
        String ubicacion = searchBar.getText().toString();

        if (ubicacion.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce una ubicación.", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocationName(ubicacion, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address direccion = direcciones.get(0);
                LatLng latLng = new LatLng(direccion.getLatitude(), direccion.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(ubicacion)
                        .icon(BitmapDescriptorFactory.defaultMarker()));
            } else {
                Toast.makeText(this, "No se encontró la ubicación especificada.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al buscar la ubicación. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Se necesita permiso de ubicación para mostrar tu posición en el mapa.", Toast.LENGTH_LONG).show();
        }
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación concedido.", Toast.LENGTH_SHORT).show();
                if (mMap != null) {
                    configurarMapa(); // Configurar el mapa ahora que se tienen permisos
                }
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado. Mapa limitado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirReservaActivity1() {
        Intent intent = new Intent(MapaActivity.this, ReservaActivity1.class);
        startActivity(intent);
    }

    private void abrirReservaActivity2() {
        Intent intent = new Intent(MapaActivity.this, ReservaActivity2.class);
        startActivity(intent);
    }
}
