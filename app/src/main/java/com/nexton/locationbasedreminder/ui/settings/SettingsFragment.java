package com.nexton.locationbasedreminder.ui.settings;

import static android.content.Context.MODE_PRIVATE;
import static com.nexton.locationbasedreminder.ui.addeditplace.AddEditPlaceFragment.PREF_KEY_DEFAULT_RANGE;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nexton.locationbasedreminder.BuildConfig;
import com.nexton.locationbasedreminder.R;
import com.nexton.locationbasedreminder.ui.BaseFragment;
import com.nexton.locationbasedreminder.ui.MainActivity;
import com.nexton.locationbasedreminder.ui.SplashActivity;
import com.nexton.locationbasedreminder.util.ConfigUtils;
import com.nexton.locationbasedreminder.util.DevicePrefs;
import com.nexton.locationbasedreminder.viewmodel.SettingsFragmentViewModel;

import java.util.Locale;

public class SettingsFragment extends BaseFragment {

    private static final String PLAY_STORE_URL = "market://details?id=com.nexton.locationbasedreminder";
    private static final String EMAIL_ADDRESS = "admin@apenex.com";

    private TextInputLayout defaultRangeTextInputLayout;
    private TextInputEditText defaultRangeEditText;
    private MaterialButton saveButton, rateUsButton, sendFeedbackButton, seeOnGithubButton;
    private TextView appVersionTextView;
    private SettingsFragmentViewModel viewModel;
    private Switch nightModeSwitch;
    private Button languageBtn;
    String isNightMode;
    String whichLang;

    @Override
    protected View inflateFragment(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews(view);
        initViewModel();
        setDefaultRange();
        initValidationObserver();
        setDefaultRangeTextChangeListener();
        setSaveButtonClickListener();
        setRateUsButtonClickListener();
        setSendFeedbackButtonClickListener();
        setAppVersion();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        isNightMode = sharedPreferences.getString("nightMode","Disabled");
        whichLang = sharedPreferences.getString("language","English");

        if(isNightMode.equals("Enabled")){
            nightModeSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            nightModeSwitch.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if(whichLang.equals("English")){
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Resources resources = getActivity().getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }else {
            Locale locale = new Locale("ur");
            Locale.setDefault(locale);
            Resources resources = getActivity().getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        nightModeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nightModeSwitch.isChecked()){
                    isNightMode = "Enabled";
                    myEdit.putString("nightMode",isNightMode);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    myEdit.commit();

                    Intent intent = new Intent(getActivity().getApplicationContext(),SplashActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();

                }else{
                    isNightMode = "Disabled";
                    myEdit.putString("nightMode",isNightMode);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    myEdit.commit();

                    Intent intent = new Intent(getActivity().getApplicationContext(),SplashActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();

                }
            }
        });

        languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final View customLayout = getLayoutInflater().inflate(R.layout.change_language_dialog, null);
                builder.setView(customLayout);
                AlertDialog dialog = builder.create();

                RadioButton
                        englishLang = customLayout.findViewById(R.id.englishLanguage),
                        urduLang = customLayout.findViewById(R.id.urduLanguage);

                Button
                        cancelBtn = customLayout.findViewById(R.id.cancelLangDialog_btn),
                        submitBtn = customLayout.findViewById(R.id.submitLangDialog_btn);

                submitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (englishLang.isChecked()){

                            whichLang = "English";
                            myEdit.putString("language",whichLang);
                            myEdit.commit();

                            Locale locale = new Locale("en");
                            Locale.setDefault(locale);
                            Resources resources = getActivity().getResources();
                            Configuration config = resources.getConfiguration();
                            config.setLocale(locale);
                            resources.updateConfiguration(config, resources.getDisplayMetrics());

                            dialog.dismiss();

                            Intent intent = new Intent(getActivity().getApplicationContext(),SplashActivity.class);
                            startActivity(intent);
                            getActivity().finishAffinity();

                        } else if (urduLang.isChecked()) {

                            whichLang = "Urdu";
                            myEdit.putString("language",whichLang);
                            myEdit.commit();

                            Locale locale = new Locale("ur");
                            Locale.setDefault(locale);
                            Resources resources = getActivity().getResources();
                            Configuration config = resources.getConfiguration();
                            config.setLocale(locale);
                            resources.updateConfiguration(config, resources.getDisplayMetrics());

                            dialog.dismiss();

                            Intent intent = new Intent(getActivity().getApplicationContext(),SplashActivity.class);
                            startActivity(intent);
                            getActivity().finishAffinity();

                        }else{
                            dialog.dismiss();
                        }
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

    }

    private void initViews(View view) {
        defaultRangeTextInputLayout = view.findViewById(R.id.text_input_layout_default_range);
        defaultRangeEditText = view.findViewById(R.id.edit_text_default_range);
        saveButton = view.findViewById(R.id.btn_save);
        rateUsButton = view.findViewById(R.id.btn_rate_us);
        sendFeedbackButton = view.findViewById(R.id.btn_send_feedback);
        appVersionTextView = view.findViewById(R.id.txt_app_version);
        nightModeSwitch = view.findViewById(R.id.nightModeSwitch);
        languageBtn = view.findViewById(R.id.languageBtn);
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SettingsFragmentViewModel.class);
    }

    private void setDefaultRange() {
        String range = String.valueOf(viewModel.getSavedRange());

        defaultRangeEditText.setText(range);
        viewModel.rangeValueChanged(range);
    }

    private void initValidationObserver() {
        viewModel.getSettingsFormState().observe(getViewLifecycleOwner(), settingsFormState -> {
            if (settingsFormState == null) return;

            saveButton.setEnabled(
                    settingsFormState.isDataValid() && settingsFormState.isDefaultRangeChanged());

            defaultRangeTextInputLayout.setErrorEnabled(settingsFormState.isDataValid());

            if (settingsFormState.getDefaultRangeError() != null) {
                defaultRangeEditText.setError(getString(settingsFormState.getDefaultRangeError()));
            }
        });
    }

    private void setDefaultRangeTextChangeListener() {
        defaultRangeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.rangeValueChanged(s.toString());
            }
        });
    }

    private void setSaveButtonClickListener() {
        saveButton.setOnClickListener(v -> {
            String range = defaultRangeEditText.getText().toString();
            DevicePrefs.setPrefs(requireContext(), PREF_KEY_DEFAULT_RANGE, Integer.parseInt(range));
            viewModel.rangeValueChanged(range);
        });
    }

    private void setRateUsButtonClickListener() {
        rateUsButton.setOnClickListener(v ->
                goToUrl(PLAY_STORE_URL)
        );
    }

    private void setSendFeedbackButtonClickListener() {
        sendFeedbackButton.setOnClickListener(v ->
                sendEmail()
        );
    }


    private void goToUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    private void sendEmail() {
        Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
        selectorIntent.setData(Uri.parse("mailto:"));

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL_ADDRESS});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
        emailIntent.setSelector(selectorIntent);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(Intent.createChooser(emailIntent, getString(R.string.email)));
    }

    private void setAppVersion() {
        appVersionTextView.setText(getString(R.string.app_name_and_version, BuildConfig.VERSION_NAME));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navController.popBackStack();
                ConfigUtils.closeKeyboard(requireActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
