package com.cybercom.passenger.flows.signup;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.accounts.AccountActivity;
import com.cybercom.passenger.flows.car.CarDetailActivity;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.utils.ValidateEmailHelper;
import com.cybercom.passenger.utils.ValidatePersonalNumberHelper;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.os.Build.VERSION_CODES.M;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String GENDER_MALE = "Male";
    private static final String GENDER_FEMALE = "Female";
    private static final String LOGIN_ARRAY = "loginArray";
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private SignUpViewModel mViewModel;
    private Button mNextButton;
    private EditText mEmail, mPassword;
    private EditText mFullName;
    private EditText mPersonalNumber;
    private EditText mPhone;
    private TextView mMaleTextSelect;
    private TextView mFemaleTextSelect;
    private String mSaveRadioButtonAnswer;
    private boolean mCheckEmailValidation = false;
    private boolean mCheckPasswordValidation = false;
    private boolean mCheckPersonalNumberValidation = false;

    private ImageView mImageViewProfile;
    private ImageView mMaleIcon;
    private ImageView mFemaleIcon;

    private LinearLayout mMaleLayout;
    private LinearLayout mFemaleLayout;
    private ProgressBar progressBar;
    private String mPassenger;
    private String mDriver;
    private String mRegisterType;
    private int mType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.signup_title);

        mViewModel = ViewModelProviders.of(this).get(SignUpViewModel.class);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        mPassword = findViewById(R.id.edittext_signup_password);
        mEmail = findViewById(R.id.edittext_loginscreen_password);
        mFullName = findViewById(R.id.edittext_signup_fullName);
        mPersonalNumber = findViewById(R.id.edittext_loginscreen_email);
        mPhone = findViewById(R.id.edittext_signup_phone);
        mNextButton = findViewById(R.id.button_signup_next);
        mNextButton.setOnClickListener(this);
        mImageViewProfile = findViewById(R.id.imageview_signup_profile);
        mImageViewProfile.setOnClickListener(this);

        mMaleLayout = findViewById(R.id.maleLayout);
        mMaleLayout.setOnClickListener(this);
        mFemaleLayout = findViewById(R.id.femaleLayout);
        mFemaleLayout.setOnClickListener(this);

        mMaleTextSelect = findViewById(R.id.male_text_select);
        mFemaleTextSelect = findViewById(R.id.female_text_select);
        mMaleIcon = findViewById(R.id.male_icon_select);
        mFemaleIcon = findViewById(R.id.female_icon_select);

        mPassenger = getResources().getString(R.string.signup_passenger);
        mDriver = getResources().getString(R.string.signup_driver);
        mRegisterType = getResources().getString(R.string.signup_type);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        mRegisterType = extras.getString(mRegisterType);
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        mNextButton.setText(R.string.next);
    }

    private void initUI(){
        mMaleLayout.setSelected(true);
        mMaleTextSelect.setTextColor(getResources().getColor(R.color.colorWhite));
        mFemaleTextSelect.setTextColor(getResources().getColor(R.color.colorBlue));
        mMaleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_male_white));
        mFemaleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_woman_blue));
        mSaveRadioButtonAnswer = GENDER_MALE;

        mPersonalNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus && !mPersonalNumber.getText().toString().isEmpty()) {
                validatePersonalNumber(mPersonalNumber.getText().toString());
            }
        });

        mEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !mEmail.getText().toString().isEmpty()) {
                validateEmail(mEmail.getText().toString());
                mEmail.setError(ValidateEmailHelper.isValidEmailAddress(
                        mEmail.getText().toString()));
            }
        });

        mPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus && !mPassword.getText().toString().isEmpty()) {
                validatePassword(mPassword.getText().toString());
            }
        });
    }

    @Override
    public void onClick(final View v) {
        switch(v.getId()) {
            case R.id.maleLayout:
                if(!mMaleLayout.isSelected()){
                    mMaleLayout.setSelected(true);
                    mFemaleLayout.setSelected(false);
                    mMaleTextSelect.setTextColor(getResources().getColor(R.color.colorWhite));
                    mFemaleTextSelect.setTextColor(getResources().getColor(R.color.colorBlue));
                    mMaleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_male_white));
                    mFemaleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_woman_blue));
                    mSaveRadioButtonAnswer = GENDER_MALE;
                }
                break;
            case R.id.femaleLayout:
                if(!mFemaleLayout.isSelected()){
                    mFemaleLayout.setSelected(true);
                    mMaleLayout.setSelected(false);
                    mMaleTextSelect.setTextColor(getResources().getColor(R.color.colorBlue));
                    mFemaleTextSelect.setTextColor(getResources().getColor(R.color.colorWhite));
                    mMaleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_male_blue));
                    mFemaleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_woman_white));
                    mSaveRadioButtonAnswer = GENDER_FEMALE;
                }
                break;

            case R.id.button_signup_next:
                handleNextClicked();
                break;

            case R.id.imageview_signup_profile:
                checkPermissions(SignUpActivity.this);
                break;
        }
    }

    private void handleNextClicked() {
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        final String fullName = mFullName.getText().toString();
        final String personalNumber = mPersonalNumber.getText().toString();
        final String phone = mPhone.getText().toString();

        if (validateUserInput(email, password, fullName, personalNumber, phone)) {
            mNextButton.setText("");
            progressBar.setVisibility(View.VISIBLE);
            if (mRegisterType.equalsIgnoreCase(mPassenger)) {
                mType = User.TYPE_PASSENGER;
            }
            if (mRegisterType.equalsIgnoreCase(mDriver)) {
                mType = User.TYPE_DRIVER;
            }

            User userRegister = new User(null, null, mType, phone, personalNumber, fullName,
                    null, mSaveRadioButtonAnswer, email, password);

            Gson gson = new Gson();
            String loginArray = gson.toJson(userRegister);

            if (mRegisterType.equalsIgnoreCase(mDriver)) {
                //register as driver need to verify bank id
                Intent intent = new Intent(getApplicationContext(), CarDetailActivity.class);
                intent.putExtra(LOGIN_ARRAY, loginArray);
                startActivity(intent);
            }

            if (mRegisterType.equalsIgnoreCase(mPassenger)) {
                //register as passenger need to verify bank id
                Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
                intent.putExtra(LOGIN_ARRAY, loginArray);
                startActivity(intent);
            }

        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private boolean validateUserInput(String email, String password, @NonNull String fullName,
                                      String personalNumber, @NonNull String phone) {
        boolean filledInTextFields = mCheckEmailValidation && mCheckPasswordValidation
                && !fullName.isEmpty() && mCheckPersonalNumberValidation && !phone.isEmpty();
        validateEmail(email);
        validatePassword(password);
        validateFullName(fullName);
        validatePersonalNumber(personalNumber);
        validatePhone(phone);
        return filledInTextFields;
    }

    private void validatePhone(String phone){
        if(phone.isEmpty()){
            mPhone.setError(getResources().getString(R.string.please_enter_your_phone_number));
        }
    }

    private void validatePersonalNumber(String personalNumber){
        if (personalNumber.isEmpty()){
            mPersonalNumber.setError(getResources().getString(R.string.please_enter_your_personal_number));
            mCheckPersonalNumberValidation = false;
        } else if(personalNumber.length() == 10 && !ValidatePersonalNumberHelper.hasValidChecksum(
                personalNumber)){
            mPersonalNumber.setError("The personal number doesn't exist");
            mCheckPersonalNumberValidation = false;
        } else if(personalNumber.length() < 10) {
            mPersonalNumber.setError("You have to enter 10 characters for the personal number");
            mCheckPersonalNumberValidation = false;
        } else{
            mCheckPersonalNumberValidation = true;
        }
    }

    private void validateFullName(String fullName){
        if(fullName.isEmpty()){
            mFullName.setError(getResources().getString(R.string.please_enter_your_name));
        }
    }

    private void validatePassword(String password){
        if (password.isEmpty()) {
            mPassword.setError(getResources().getString(R.string.please_enter_a_password));
            mCheckPasswordValidation = false;
        } else if(password.length() < 6){
            mPassword.setError(getResources().getString(R.string.the_given_password_is_invalid));
            mCheckPasswordValidation = false;
        } else{
            mCheckPasswordValidation = true;
        }
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) {
            mEmail.setError(getResources().getString(R.string.please_enter_an_email));
            mCheckEmailValidation = false;
        }

        if(!email.isEmpty()) {
            mViewModel.validateEmail(email).observe(this, bEmail -> {
                mCheckEmailValidation = !bEmail;
                if (bEmail) {
                    mEmail.setError(getResources().getString(
                            R.string.email_address_is_already_in_use_by_another_account));
                }
            });
        }
    }

    private void checkPermissions(Activity activity) {
        PackageManager mPackageManager = activity.getPackageManager();
        int hasPermStorage = mPackageManager.checkPermission(Manifest.permission.CAMERA,
                activity.getPackageName());
        if (hasPermStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            }
        } else {
            openMediaSelector(SignUpActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 2 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Timber.d("permission not granted");
            }
            else {
                Timber.d("permission granted");
                openMediaSelector(SignUpActivity.this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openMediaSelector(Activity context){
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent gallIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        List<ResolveInfo> info = new ArrayList<>();
        List<Intent> yourIntentsList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(camIntent, 0);
        for (ResolveInfo res : listCam) {
            final Intent finalIntent = new Intent(camIntent);
            finalIntent.setComponent(new ComponentName(res.activityInfo.packageName,
                    res.activityInfo.name));
            yourIntentsList.add(finalIntent);
            info.add(res);
        }
        List<ResolveInfo> listGall = packageManager.queryIntentActivities(gallIntent, 0);
        for (ResolveInfo res : listGall) {
            final Intent finalIntent = new Intent(gallIntent);
            finalIntent.setComponent(new ComponentName(res.activityInfo.packageName,
                    res.activityInfo.name));
            yourIntentsList.add(finalIntent);
            info.add(res);
        }
        openDialog(context);
    }

    private static void openDialog(final Activity context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        dialog.setTitle(context.getResources().getString(R.string.select_image_source));
        dialog.setNeutralButton(context.getResources().getString(R.string.cancel), (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    Picasso.with(this)
                            .load(selectedImage).into(mImageViewProfile);
                }
             break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();

                    boolean isCamera = (selectedImage == null);
                    if (isCamera) {
                        Bundle extras = imageReturnedIntent.getExtras();
                        assert extras != null;
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        Picasso.with(this).load(MediaStore.Images.Media.insertImage(
                                getApplicationContext().getContentResolver(),
                                imageBitmap, "Title", null)).into(mImageViewProfile);
                    }
                    else {
                        Picasso.with(this).load(selectedImage).into(mImageViewProfile);
                    }
                }
                break;
        }
    }
}
