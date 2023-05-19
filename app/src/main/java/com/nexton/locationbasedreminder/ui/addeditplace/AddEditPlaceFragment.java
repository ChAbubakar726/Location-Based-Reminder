package com.nexton.locationbasedreminder.ui.addeditplace;

import static android.content.ContentValues.TAG;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.nexton.locationbasedreminder.R;
import com.nexton.locationbasedreminder.model.Place;
import com.nexton.locationbasedreminder.ui.AddEditFragment;
import com.nexton.locationbasedreminder.ui.MainActivity;
import com.nexton.locationbasedreminder.ui.addeditreminder.AddEditReminderFragment;
import com.nexton.locationbasedreminder.util.DevicePrefs;
import com.nexton.locationbasedreminder.viewmodel.AddEditPlaceFragmentViewModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class AddEditPlaceFragment extends AddEditFragment implements OnMapReadyCallback, LocationListener, PlaceSelectionListener {

    public static final String BUNDLE_KEY_PLACE = "com.nexton.locationbasedreminder.BUNDLE_KEY_PLACE";
    private static final String BUNDLE_KEY_MAP_VIEW = "com.nexton.locationbasedreminder.BUNDLE_KEY_MAP_VIEW";
    public static final String PREF_KEY_DEFAULT_RANGE = "com.nexton.locationbasedreminder.PREF_KEY_DEFAULT_RANGE";
    public static final int DEFAULT_RANGE = 100;
    private static final float DEFAULT_ZOOM = 15F;

    private TextView radiusTextView;
    private AppCompatSeekBar radiusSeekBar;
    private Place currentPlace;
    private MapView mapView;
    private GoogleMap googleMap;
    private Marker currentPlaceMarker;
    private LocationManager locationManager;
    private Circle radiusCircle;
    private AddEditPlaceFragmentViewModel viewModel;

    private AutocompleteSupportFragment autocompleteSupportFragment;

    public static final int REQUEST_CHECK_SETTING = 1001;
    private LocationRequest locationRequest;

    @Override
    protected View inflateFragment(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_place, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        retrievePlace();

        checkPermissionsStatus();
        checkGPSStatus();

        if (isGoogleServicesAvailable()) {
            initMap(savedInstanceState);
        } else {
            Toast.makeText(requireContext(), getString(R.string.google_services_not_available), Toast.LENGTH_SHORT).show();
        }

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                LatLng latLng = place.getLatLng();
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
                googleMap.moveCamera(update);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

    }


    private void initViews(View view) {
        mapView = view.findViewById(R.id.map_view);
        radiusSeekBar = view.findViewById(R.id.seekbar_radius);
        radiusTextView = view.findViewById(R.id.txt_radius);

        Places.initialize(getActivity().getApplicationContext(), "AIzaSyD6oMd3tI3h0wRoHpFA01vYPRkvnR8f6Tw");
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), "AIzaSyD6oMd3tI3h0wRoHpFA01vYPRkvnR8f6Tw");
        }

        PlacesClient placesClient = Places.createClient(this.getContext());
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        List<com.google.android.libraries.places.api.model.Place.Field> placeFields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(String.valueOf(token), placeFields)
                .build();

        autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteSupportFragment != null) {
            autocompleteSupportFragment.setPlaceFields(placeFields);
        }

    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(AddEditPlaceFragmentViewModel.class);
    }

    private void retrievePlace() {
        if (getArguments() != null && getArguments().getParcelable(BUNDLE_KEY_PLACE) != null) {
            currentPlace = getArguments().getParcelable(BUNDLE_KEY_PLACE);
            ((MainActivity) requireActivity()).getSupportActionBar().setTitle(currentPlace.getName());
        }
    }

    private void initMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(BUNDLE_KEY_MAP_VIEW);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    private boolean isGoogleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(requireContext());
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(requireActivity(), isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(requireContext(), getString(R.string.cannot_connect_play_services), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        startLocationListener();

        if (isFirstMapStart()) {
            // Map started for the first time
            if (inEditMode()) {
                addMarker(currentPlace);
                drawCircle(currentPlace.getRadius(),
                        new LatLng(currentPlace.getLatitude(), currentPlace.getLongitude()));
                goToLocation(currentPlace.getLatitude(), currentPlace.getLongitude(), DEFAULT_ZOOM);
                viewModel.setRadius(currentPlace.getRadius());
            } else {
                viewModel.setRadius(DevicePrefs.getPrefs(requireContext(), PREF_KEY_DEFAULT_RANGE, DEFAULT_RANGE));
            }
        } else {
            // Map restarted due to configuration change such as screen rotation, language change etc..
            if (inEditMode()) {
                addMarker(currentPlace);
                drawCircle(currentPlace.getRadius(),
                        new LatLng(currentPlace.getLatitude(), currentPlace.getLongitude()));
            }
            goToLocation(viewModel.getLastKnownScreenLocation().latitude,
                    viewModel.getLastKnownScreenLocation().longitude, viewModel.getLastKnownZoom());
        }

        radiusSeekBar.setProgress(viewModel.getRadius());
        radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
        radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);

        setOnCameraMoveListener();
        setSeekBarListener();
    }

    private void setOnCameraMoveListener() {
        googleMap.setOnCameraMoveListener(() -> {
            radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
            viewModel.setLastKnownScreenLocation(googleMap.getCameraPosition().target);
            viewModel.setLastKnownZoom(googleMap.getCameraPosition().zoom);
        });
    }

    private void setSeekBarListener() {
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                viewModel.setRadius(progress);

                if(progress>=0 && progress<50){
                    Toast.makeText(getContext(), "Minimum Radius is 50m", Toast.LENGTH_SHORT).show();
                }else if(progress>=100 && progress<200){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 15.0f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=200 && progress<300){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 14.8f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=300 && progress<400){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 14.8f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=400 && progress<500){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 14.6f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=500 && progress<600){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 14.6f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=600 && progress<700){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 14.5f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=700 && progress<800){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 14.4f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=800 && progress<900){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 14.2f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=900 && progress<1000){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 14.0f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=1000 && progress<2000){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 13.0f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=2000 && progress<3000){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 12.0f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=3000 && progress<4000){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 11.0f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else if(progress>=4000 && progress<=5000){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, 10.0f);
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                    googleMap.moveCamera(update);
                }else{
                    radiusTextView.setText(getString(R.string.radius_text, viewModel.getRadius()));
                    radiusCircle = drawCircle(viewModel.getRadius(), googleMap.getCameraPosition().target);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void addMarker(Place place) {
        if (currentPlaceMarker != null) currentPlaceMarker.remove();

        MarkerOptions options = new MarkerOptions()
                .title(place.getName())
                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_icon))
                .snippet(getString(R.string.marker_snippet, place.getRadius()))
                .position(new LatLng(place.getLatitude(), place.getLongitude()));
        currentPlaceMarker = googleMap.addMarker(options);
        configureMarkerSnippet();
    }

    /**
     * Marker snippet normally can't be more than 1 line, this makes it possible. Must add line
     * break to snippet text when setting it.
     */
    private void configureMarkerSnippet() {
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(requireContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(requireContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(requireContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }

    /**
     * Converts vector image to bitmap.
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Circle drawCircle(int radius, LatLng latLng) {
        if (radiusCircle != null) removeCircle();

        CircleOptions options = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .fillColor(getResources().getColor(R.color.colorRadiusFill))
                .strokeColor(getResources().getColor(R.color.colorStroke))
                .strokeWidth(2);
        return googleMap.addCircle(options);
    }

    private void removeCircle() {
        radiusCircle.remove();
        radiusCircle = null;
    }

    /**
     * @return true if map started for the first time, false otherwise
     */
    private boolean isFirstMapStart() {
        return viewModel.getLastKnownScreenLocation() == null && viewModel.getLastKnownZoom() == null;
    }

    private void goToLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        googleMap.moveCamera(update);
    }

    private void startLocationListener() {
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), R.string.grant_location_permission, Toast.LENGTH_SHORT).show();
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
    }

    @Override
    protected void saveMenuItemClicked() {
        navigateToNamePlaceDialog();
    }

    @Override
    protected void addToReminderMenuItemClicked() {
        addToReminder();
    }

    @Override
    protected void deleteItem() {
        deleteCurrentPlace();
    }

    private void navigateToNamePlaceDialog() {
        prepareCurrentPlace();

        Bundle bundle = new Bundle();
        bundle.putParcelable(NamePlaceDialog.BUNDLE_KEY_PLACE, currentPlace);

        navController.navigate(R.id.action_addEditPlaceFragment_to_namePlaceDialog, bundle);
    }

    private void prepareCurrentPlace() {
        if (!inEditMode()) {
            currentPlace = new Place();
        }
        LatLng latLng = googleMap.getCameraPosition().target;
        currentPlace.setLatitude(latLng.latitude);
        currentPlace.setLongitude(latLng.longitude);
        currentPlace.setName(getAddress(latLng.latitude,latLng.longitude));
        currentPlace.setRadius(viewModel.getRadius());
    }

    private void deleteCurrentPlace() {
        viewModel.delete(currentPlace);
    }

    private void addToReminder() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AddEditReminderFragment.BUNDLE_KEY_PLACE_RETRIEVED, currentPlace);
        navController.navigate(R.id.action_addEditPlaceFragment_to_addEditReminderFragment, bundle);
    }

    @Override
    protected boolean inEditMode() {
        return currentPlace != null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Timber.i("x");
        if (location != null && isFirstMapStart() && !inEditMode()) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
            googleMap.animateCamera(cameraUpdate);
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onResume() {
        mapView.onResume();
        checkPermissionsStatus();
        checkGPSStatus();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {

    }

    @Override
    public void onError(@NonNull Status status) {

    }

    public void checkPermissionsStatus() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) this.getContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions((MainActivity) this.getContext(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);

                    }
                }
            }
        }
    }

    public void checkGPSStatus() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getActivity().getApplicationContext())
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTING);
                            } catch (IntentSender.SendIntentException ex) {
                            }
                            break;

                        case  LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }


    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this.getContext(), Locale.getDefault());
        String add = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            add = obj.getAddressLine(0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return add;
    }
}