package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Context menu is handled through the adapter

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();

        // Show a brief message when returning from crime detail
        if (getActivity() != null) {
            Toast.makeText(getActivity(), "Welcome back to the crime list!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        // Show first crime in landscape mode if detail container exists
        if (getActivity().findViewById(R.id.detail_fragment_container) != null && !crimes.isEmpty()) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            Fragment detailFragment = CrimeFragment.newInstance(crimes.get(0).getId());
            fm.beginTransaction()
                    .replace(R.id.detail_fragment_container, detailFragment)
                    .commit();
        }
    }

    /*
     * @Override
     * public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     * super.onCreateOptionsMenu(menu, inflater);
     * inflater.inflate(R.menu.fragment_crime_list, menu);
     * }
     *
     * @Override
     * public boolean onOptionsItemSelected(MenuItem item) {
     * if (item.getItemId() == R.id.new_crime) {
     * Crime crime = new Crime();
     * CrimeLab.get().addCrime(crime);
     * Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
     * startActivity(intent);
     * return true;
     * }
     * return super.onOptionsItemSelected(item);
     * }
     */

    private class CrimeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;
        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedCheckBox = itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(crime.getTitle());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            mDateTextView.setText(dateFormat.format(crime.getDate()));

            // Remove previous listener to avoid multiple callbacks
            mSolvedCheckBox.setOnCheckedChangeListener(null);
            mSolvedCheckBox.setChecked(crime.isSolved());

            // Set up checkbox listener to update the crime when checked/unchecked
            mSolvedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mCrime.setSolved(isChecked);
                CrimeLab.get(getActivity()).updateCrime(mCrime);
            });
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(), mCrime.getTitle() + " clicked!", Toast.LENGTH_SHORT).show();

            // Check if we're in landscape mode with detail container
            if (getActivity().findViewById(R.id.detail_fragment_container) != null) {
                // Landscape mode - replace detail fragment
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment detailFragment = CrimeFragment.newInstance(mCrime.getId());
                fm.beginTransaction()
                        .replace(R.id.detail_fragment_container, detailFragment)
                        .commit();
            } else {
                // Portrait mode - start CrimePagerActivity
                Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
                startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            // Show delete confirmation dialog
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.delete_crime_title))
                    .setMessage(getString(R.string.delete_crime_message, mCrime.getTitle()))
                    .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                        CrimeLab.get(getActivity()).deleteCrime(mCrime);
                        // Notify the adapter that data has changed
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                        Toast.makeText(getActivity(), getString(R.string.crime_deleted), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();

            return true; // Consume the long click
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        public Crime getCrimeAtPosition(int position) {
            return mCrimes.get(position);
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }
}