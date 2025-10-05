package com.example.criminalintent;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.criminalintent.CrimeDbSchema.CrimeTable;

import java.util.Date;
import java.util.UUID;

public class CrimeDbSchema {
    public static final class CrimeTable {
        public static final String NAME = "crimes";
        
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
        }
    }
}
