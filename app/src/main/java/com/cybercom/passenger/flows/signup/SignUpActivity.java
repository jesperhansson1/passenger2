package com.cybercom.passenger.flows.signup;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.utils.ToastHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.os.Build.VERSION_CODES.M;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    SignUpViewModel mViewModel;
    RadioButton mRadioButtonMale, mRadioButtonFemale;
    Button mNextButton;
    EditText mPassword, mEmail, mFullName, mPersonalNumber, mPhone;
    String mSaveRadioButtonAnswer;
    Boolean mFilledInTextFields = false;
    private static final String GENDER_MALE = "Male";
    private static final String GENDER_FEMALE = "Female";
    ImageView mImageViewProfile;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.signup_title);

        mViewModel = ViewModelProviders.of(this).get(SignUpViewModel.class);

        mPassword = findViewById(R.id.edittext_signup_password);
        mEmail = findViewById(R.id.edittext_signup_email);
        mFullName = findViewById(R.id.edittext_signup_fullName);
        mPersonalNumber = findViewById(R.id.edittext_signup_personalNumer);
        mPhone = findViewById(R.id.edittext_signup_phone);

        mRadioButtonMale = findViewById(R.id.radiobutton_signup_maleRadioButton);
        mRadioButtonMale.setOnClickListener(this);
        mRadioButtonFemale = findViewById(R.id.radiobutton_signup_femaleRadioButton);
        mRadioButtonFemale.setOnClickListener(this);
        mNextButton = findViewById(R.id.button_signup_next);
        mNextButton.setOnClickListener(this);

        mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorWhite));
        mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorBlue));

        mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_white);
        mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_blue);

        mSaveRadioButtonAnswer = GENDER_MALE;
        mImageViewProfile = findViewById(R.id.imageview_signup_profile);
        mImageViewProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.radiobutton_signup_maleRadioButton:
                // Do something
                mSaveRadioButtonAnswer = GENDER_MALE;
                mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_white);

                mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_blue);
                break;

            case R.id.radiobutton_signup_femaleRadioButton:
                // Do something
                mSaveRadioButtonAnswer = GENDER_FEMALE;
                mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_white);

                mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_blue);
                break;

            case R.id.button_signup_next:
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                final String fullName = mFullName.getText().toString();
                final String personalNumber = mPersonalNumber.getText().toString();
                final String phone = mPhone.getText().toString();

                if(checkTextFields(email, password, fullName, personalNumber, phone)){
                    mViewModel.createUserWithEmailAndPassword(email, password, this).observe(this, new Observer<FirebaseUser>() {
                        @Override
                        public void onChanged(@Nullable FirebaseUser user) {
                            if(user != null){
                                mViewModel.createUser(user.getUid(), new User(user.getUid(), FirebaseInstanceId.getInstance().getToken(),
                                        User.TYPE_PASSENGER, phone, personalNumber, fullName, null, mSaveRadioButtonAnswer));
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else{
                                ToastHelper.makeToast(getResources().getString(R.string.toast_could_not_create_user), SignUpActivity.this).show();
                            }
                        }
                    });
                }
                break;

            case R.id.imageview_signup_profile:
                checkpermissions(SignUpActivity.this);
                break;
        }
    }

    public Boolean checkTextFields(String email, String password, String fullName, String personalNumber, String phone){
        if(!email.isEmpty() && !password.isEmpty() && !fullName.isEmpty() && !personalNumber.isEmpty() && !phone.isEmpty()){
            mFilledInTextFields = true;
        } else{
            mFilledInTextFields = false;
        }
        if(email.isEmpty()){
            mEmail.setError("Please enter an email");
        }
        if(password.isEmpty()){
            mPassword.setError("Please enter a password");
        }
        if(fullName.isEmpty()){
            mFullName.setError("Please enter your name");
        }
        if(personalNumber.isEmpty()){
            mPersonalNumber.setError("Please enter your personal number");
        }
        if(phone.isEmpty()){
            mPhone.setError("Please enter your phone number");
        }
        return mFilledInTextFields;
    }



    public void checkpermissions(Activity activity) {
        PackageManager mPackageManager = activity.getPackageManager();
        int hasPermStorage = mPackageManager.checkPermission(Manifest.permission.CAMERA, activity.getPackageName());
        if (hasPermStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            }
        } else {
            openMediaSelector(SignUpActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Timber.d("permission not granted");
            }
            else
            {
                Timber.d("permission granted");
                openMediaSelector(SignUpActivity.this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void openMediaSelector(Activity context){
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//("android.media.action.IMAGE_CAPTURE");
        Intent gallIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        List<ResolveInfo> info=new ArrayList<ResolveInfo>();
        List<Intent> yourIntentsList = new ArrayList<Intent>();
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(camIntent, 0);
        for (ResolveInfo res : listCam) {
            final Intent finalIntent = new Intent(camIntent);
            finalIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            yourIntentsList.add(finalIntent);
            info.add(res);
        }
        List<ResolveInfo> listGall = packageManager.queryIntentActivities(gallIntent, 0);
        for (ResolveInfo res : listGall) {
            final Intent finalIntent = new Intent(gallIntent);
            finalIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            yourIntentsList.add(finalIntent);
            info.add(res);
        }
        openDialog(context,yourIntentsList,info);
    }

    private static void openDialog(final Activity context, final List<Intent> intents,
                                   List<ResolveInfo> activitiesInfo) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        dialog.setTitle(context.getResources().getString(R.string.select_image_source));
        final AlertDialog.Builder builder = dialog.setAdapter(buildAdapter(context, activitiesInfo),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = intents.get(id);
                        context.startActivityForResult(intent, 1);
                    }
                });

        dialog.setNeutralButton(context.getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    mImageViewProfile.setImageURI(selectedImage);
                }
             break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    mImageViewProfile.setImageURI(selectedImage);
                    Picasso.with(this)
                            .load(selectedImage).into(mImageViewProfile);
                    boolean isCamera = (imageReturnedIntent == null || imageReturnedIntent.getData() == null);
                    if(isCamera)
                    {
                        Bundle extras = imageReturnedIntent.getExtras();
                        assert extras != null;
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        Picasso.with(this)
                                .load(MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),
                                        imageBitmap, "Title", null)).into(mImageViewProfile);
                    }
                }
                 break;
        }
    }

    private static ArrayAdapter<ResolveInfo> buildAdapter(final Context context, final List<ResolveInfo> activitiesInfo) {
        return new ArrayAdapter<ResolveInfo>(context, R.layout.image_picker,R.id.textview_imagepicker_title,activitiesInfo){
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ResolveInfo res=activitiesInfo.get(position);
                ImageView image= view.findViewById(R.id.imageview_imagepicker_icon);
                image.setImageDrawable(res.loadIcon(context.getPackageManager()));
                TextView textview= view.findViewById(R.id.textview_imagepicker_title);
                textview.setText(res.loadLabel(context.getPackageManager()).toString());
                return view;
            }
        };
    }
}
