package com.example.criminalintent;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mSuspectButton;
    private Button mReportButton;
    private Button mCallButton;
    private ImageView mPhotoView;
    private Button mPhotoButton;
    private File mPhotoFile;
    private static final String ARG_CRIME_ID = "crime_id";
    private static final int REQUEST_SUSPECT = 0;
    private static final int REQUEST_PHOTO = 1;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        if (mCrime == null) {
            mCrime = new Crime();
            // Add the new crime to CrimeLab
            CrimeLab.get(getActivity()).addCrime(mCrime);
        }

        // Ensure suspect field is initialized
        if (mCrime.getSuspect() == null) {
            mCrime.setSuspect("");
        }

        // Ensure title field is initialized
        if (mCrime.getTitle() == null) {
            mCrime.setTitle("");
        }

        // Ensure date field is initialized
        if (mCrime.getDate() == null) {
            mCrime.setDate(new Date());
        }

        // Initialize photo file
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle() != null ? mCrime.getTitle() : "");
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mCrime != null) {
                    mCrime.setTitle(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This space intentionally left blank
            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(v1 -> {
            if (mCrime != null && mCrime.getDate() != null) {
                DatePickerDialog dialog = new DatePickerDialog(
                        getContext(),
                        (view, year, month, dayOfMonth) -> {
                            mCrime.setDate(new Date(year - 1900, month, dayOfMonth));
                            updateDate();
                        },
                        mCrime.getDate().getYear() + 1900,
                        mCrime.getDate().getMonth(),
                        mCrime.getDate().getDate());
                dialog.show();
            }
        });

        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mCrime != null) {
                mCrime.setSolved(isChecked);
            }
        });

        // Handle back button click
        Button backButton = v.findViewById(R.id.back_button);
        backButton.setOnClickListener(v1 -> {
            // This will trigger the back navigation
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(v1 -> {
            if (mCrime != null) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_SUSPECT);
            }
        });

        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(v1 -> {
            if (mCrime != null) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        mCallButton = v.findViewById(R.id.crime_call);
        mCallButton.setOnClickListener(v1 -> {
            if (mCrime != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:555-0123"));
                startActivity(intent);
            }
        });

        mPhotoView = v.findViewById(R.id.crime_photo);
        mPhotoButton = v.findViewById(R.id.crime_camera);

        mPhotoButton.setOnClickListener(v1 -> {
            // Try multiple approaches to ensure camera opens
            boolean cameraOpened = false;

            // Approach 1: Standard camera intent
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(intent);
                cameraOpened = true;
            } catch (Exception e) {
                // Approach 2: Try without checking resolveActivity
                try {
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                    cameraOpened = true;
                } catch (Exception e2) {
                    // Approach 3: Use our comprehensive fallback method
                    openCameraForPhoto();
                }
            }
        });

        mPhotoView.setOnClickListener(v1 -> {
            if (mPhotoFile != null && mPhotoFile.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri photoUri = FileProvider.getUriForFile(getActivity(),
                        "com.example.criminalintent.fileprovider", mPhotoFile);
                intent.setDataAndType(photoUri, "image/jpeg");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        });

        updatePhotoView();

        updateSuspectButtonText();

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCrime != null) {
            CrimeLab.get(getActivity()).updateCrime(mCrime);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK || mCrime == null) {
            return;
        }

        if (requestCode == REQUEST_SUSPECT && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                // Query the contact's display name
                String[] queryFields = new String[] { ContactsContract.Contacts.DISPLAY_NAME };
                Cursor contactData = getActivity().getContentResolver()
                        .query(contactUri, queryFields, null, null, null);

                try {
                    if (contactData != null && contactData.getCount() > 0) {
                        contactData.moveToFirst();
                        String suspect = contactData.getString(0);
                        mCrime.setSuspect(suspect);
                        CrimeLab.get(getActivity()).updateCrime(mCrime);
                        updateSuspectButtonText();
                    }
                } finally {
                    if (contactData != null) {
                        contactData.close();
                    }
                }
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    private void updateSuspectButtonText() {
        if (mCrime != null && mCrime.getSuspect() != null && !mCrime.getSuspect().isEmpty()) {
            mSuspectButton.setText(mCrime.getSuspect());
        } else {
            mSuspectButton.setText(R.string.crime_suspect_text);
        }
    }

    private String getCrimeReport() {
        if (mCrime == null) {
            return "";
        }

        String solvedString = mCrime.isSolved() ? getString(R.string.crime_report_solved)
                : getString(R.string.crime_report_unsolved);
        String dateFormat = "";
        if (mCrime.getDate() != null) {
            dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(mCrime.getDate());
        }
        String suspect = mCrime.getSuspect() != null ? mCrime.getSuspect()
                : getString(R.string.crime_report_no_suspect);
        String title = mCrime.getTitle() != null ? mCrime.getTitle() : "";

        String report = getString(R.string.crime_report, title, dateFormat, solvedString, suspect);
        return report;
    }

    private void updateDate() {
        if (mCrime != null && mCrime.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            mDateButton.setText(dateFormat.format(mCrime.getDate()));
        }
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private void openCameraForPhoto() {
        try {
            // Method 1: Try the most common camera intent
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(cameraIntent);
        } catch (Exception e1) {
            try {
                // Method 2: Try still image camera intent
                Intent stillCameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(stillCameraIntent);
            } catch (Exception e2) {
                try {
                    // Method 3: Try generic image capture
                    Intent genericIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivity(genericIntent);
                } catch (Exception e3) {
                    try {
                        // Method 4: Try opening camera app directly by package name
                        Intent directCameraIntent = getActivity().getPackageManager()
                                .getLaunchIntentForPackage("com.android.camera");
                        if (directCameraIntent != null) {
                            startActivity(directCameraIntent);
                        } else {
                            // Method 5: Try common camera package names
                            String[] cameraPackages = {
                                    "com.android.camera2",
                                    "com.google.android.GoogleCamera",
                                    "com.samsung.camera",
                                    "com.oneplus.camera",
                                    "com.huawei.camera",
                                    "com.xiaomi.camera"
                            };

                            boolean cameraFound = false;
                            for (String packageName : cameraPackages) {
                                Intent packageIntent = getActivity().getPackageManager()
                                        .getLaunchIntentForPackage(packageName);
                                if (packageIntent != null) {
                                    startActivity(packageIntent);
                                    cameraFound = true;
                                    break;
                                }
                            }

                            if (!cameraFound) {
                                android.widget.Toast
                                        .makeText(getActivity(), "Camera app not found. Please install a camera app.",
                                                android.widget.Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    } catch (Exception e4) {
                        android.widget.Toast.makeText(getActivity(),
                                "Unable to open camera. Please check if camera app is installed.",
                                android.widget.Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

}
