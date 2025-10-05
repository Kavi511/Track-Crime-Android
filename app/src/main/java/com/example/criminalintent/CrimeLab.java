package com.example.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.criminalintent.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
        generateSampleCrimes();
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeDataWrapper dataWrapper = queryCrimes(null, null);

        try {
            dataWrapper.moveToFirst();
            while (!dataWrapper.isAfterLast()) {
                crimes.add(dataWrapper.getCrime());
                dataWrapper.moveToNext();
            }
        } finally {
            dataWrapper.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeDataWrapper dataWrapper = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[] { id.toString() });

        try {
            if (dataWrapper.getCount() == 0) {
                return null;
            }

            dataWrapper.moveToFirst();
            return dataWrapper.getCrime();
        } finally {
            dataWrapper.close();
        }
    }

    public void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    public void deleteCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        mDatabase.delete(CrimeTable.NAME,
                CrimeTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private void generateSampleCrimes() {
        // Check if we already have crimes in the database
        CrimeDataWrapper dataWrapper = queryCrimes(null, null);
        int count = dataWrapper.getCount();
        dataWrapper.close();

        if (count > 0) {
            return; // Already populated
        }

        String[] crimeTypes = {
                "Armed Robbery", "Burglary", "Assault", "Fraud", "Drug Trafficking",
                "Vandalism", "Theft", "Embezzlement", "Forgery", "Money Laundering",
                "Cyber Crime", "Identity Theft", "Credit Card Fraud", "Insurance Fraud",
                "Tax Evasion", "Bribery", "Extortion", "Kidnapping", "Arson", "Homicide",
                "Car Theft", "Shoplifting", "Pickpocketing", "Counterfeiting", "Smuggling",
                "Racketeering", "Perjury", "Obstruction of Justice", "Escape", "Contempt"
        };

        String[] locations = {
                "Downtown", "Suburbs", "Shopping Mall", "Bank", "Gas Station",
                "Convenience Store", "Office Building", "Residential Area", "Park",
                "Highway", "Airport", "Train Station", "Hotel", "Restaurant", "Bar",
                "University", "Hospital", "Church", "Library", "Museum", "Theater",
                "Stadium", "Warehouse", "Factory", "Construction Site", "Beach"
        };

        String[] suspects = {
                "John Doe", "Jane Smith", "Mike Johnson", "Sarah Wilson", "David Brown",
                "Lisa Davis", "Tom Miller", "Amy Garcia", "Chris Rodriguez", "Emma Martinez",
                "Alex Thompson", "Jordan Lee", "Casey White", "Taylor Anderson", "Riley Taylor",
                "Morgan Jackson", "Quinn Martin", "Parker Lee", "Blake Hall", "Avery Young"
        };

        for (int i = 0; i < 100; i++) {
            Crime crime = new Crime();
            crime.setTitle(crimeTypes[i % crimeTypes.length] + " - " + locations[i % locations.length]);
            crime.setDate(new java.util.Date(
                    System.currentTimeMillis() - (long) (Math.random() * 365 * 24 * 60 * 60 * 1000)));
            crime.setSolved(Math.random() > 0.6); // 40% solved
            crime.setSuspect(suspects[i % suspects.length]);
            addCrime(crime);
        }
    }

    private CrimeDataWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor dataResult = mDatabase.query(
                CrimeTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new CrimeDataWrapper(dataResult);
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }

    public File getPhotoFile(Crime crime) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }
}
