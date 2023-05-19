package com.nexton.locationbasedreminder.ui.reminders;

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
import com.nexton.locationbasedreminder.model.ReminderWithNotePlacePlaceGroup;
import com.nexton.locationbasedreminder.ui.ListingFragment;
import com.nexton.locationbasedreminder.ui.addeditreminder.AddEditReminderFragment;
import com.nexton.locationbasedreminder.viewmodel.RemindersFragmentViewModel;

public class RemindersFragment extends ListingFragment {

    private ReminderAdapter adapter;

    private RemindersFragmentViewModel viewModel;

    @Override
    protected View inflateFragment(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAddButtonClickListener();
        setAdapterItemClickListener();
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(RemindersFragmentViewModel.class);
    }

    @Override
    protected void initListObserver() {
        viewModel.getAllReminders().observe(this, reminders -> {
            emptyMessageLayout.setVisibility(reminders.isEmpty() ? View.VISIBLE : View.GONE);
            adapter.submitList(reminders);

            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setAddButtonClickListener() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveNetworkConnection()){
                    navController.navigate(R.id.action_remindersFragment_to_addEditReminderFragment);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet!\nPlease connect and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setAdapterItemClickListener() {
        adapter.setOnItemClickListener(reminder -> navigateForEdit(reminder));
    }

    private void navigateForEdit(ReminderWithNotePlacePlaceGroup reminder) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AddEditReminderFragment.BUNDLE_KEY_REMINDER, reminder);
        navController.navigate(R.id.action_remindersFragment_to_addEditReminderFragment, bundle);
    }

    @Override
    protected void initAdapter() {
        adapter = new ReminderAdapter();
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    @Override
    protected void onAdapterItemSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int deletedPosition = viewHolder.getAdapterPosition();
        ReminderWithNotePlacePlaceGroup deletedReminder = adapter.getReminderAt(deletedPosition);

        viewModel.delete(adapter.getReminderAt(viewHolder.getAdapterPosition()));

        Snackbar.make(viewHolder.itemView, getString(R.string.reminder_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), v -> viewModel.insert(deletedReminder))
                .setAnchorView(fab)
                .show();
    }

    @Override
    protected void onDeleteAllOptionSelected() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(getString(R.string.msg_delete_all_reminders))
                .setPositiveButton(getText(R.string.ok), (dialog, which) -> viewModel.deleteAll())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    protected void navigateSettings() {
        navController.navigate(R.id.action_remindersFragment_to_settingsFragment);
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