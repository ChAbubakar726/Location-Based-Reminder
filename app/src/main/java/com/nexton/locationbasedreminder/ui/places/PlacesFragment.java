package com.nexton.locationbasedreminder.ui.places;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.nexton.locationbasedreminder.R;
import com.nexton.locationbasedreminder.model.Place;
import com.nexton.locationbasedreminder.ui.ListingFragment;
import com.nexton.locationbasedreminder.ui.addeditplace.AddEditPlaceFragment;
import com.nexton.locationbasedreminder.viewmodel.PlacesFragmentViewModel;

public class PlacesFragment extends ListingFragment {

    private PlaceAdapter adapter;

    private PlacesFragmentViewModel viewModel;

    @Override
    protected View inflateFragment(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_places, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAddButtonClickListener();
        setAdapterItemClickListener();
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(PlacesFragmentViewModel.class);
    }

    @Override
    protected void initListObserver() {
        viewModel.getAll().observe(this, places -> {
            emptyMessageLayout.setVisibility(places.isEmpty() ? View.VISIBLE : View.GONE);
            adapter.submitList(places);

            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setAddButtonClickListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveNetworkConnection()){
                    navController.navigate(R.id.action_placesFragment_to_addEditPlaceFragment);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet!\nPlease connect and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setAdapterItemClickListener() {
        adapter.setOnItemClickListener(place -> navigateForEdit(place));
    }

    private void navigateForEdit(Place place) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AddEditPlaceFragment.BUNDLE_KEY_PLACE, place);
        navController.navigate(R.id.action_placesFragment_to_addEditPlaceFragment, bundle);
    }

    @Override
    protected void initAdapter() {
        adapter = new PlaceAdapter();
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    @Override
    protected void onAdapterItemSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int deletedPosition = viewHolder.getAdapterPosition();
        Place deletedPlace = adapter.getPlaceAt(deletedPosition);

        viewModel.delete(adapter.getPlaceAt(viewHolder.getAdapterPosition()));

        Snackbar.make(viewHolder.itemView, getString(R.string.place_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), v -> viewModel.insert(deletedPlace))
                .setAnchorView(fab)
                .show();
    }

    @Override
    protected void onDeleteAllOptionSelected() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(getString(R.string.msg_delete_all_places))
                .setPositiveButton(getText(R.string.ok), (dialog, which) -> viewModel.deleteAll())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    protected void navigateSettings() {
        navController.navigate(R.id.action_placesFragment_to_settingsFragment);
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
