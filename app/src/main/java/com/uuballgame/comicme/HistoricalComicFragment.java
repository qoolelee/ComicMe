package com.uuballgame.comicme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoricalComicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoricalComicFragment extends Fragment {
    HistoricalFiltersAdapter filterAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoricalComicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoricalComicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoricalComicFragment newInstance(String param1, String param2) {
        HistoricalComicFragment fragment = new HistoricalComicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historical_comic, container, false);

        // find recycler view in fragment
        RecyclerView recyclerView = view.findViewById(R.id.comic_filter_historical);

        // add divider
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        // prepare data for adapter
        List<ComicFilter> historicalFilters = Constants.COMIC_FILTERS_HISTORICAL;
        // Create the View holder adapter
        filterAdapter = new HistoricalFiltersAdapter(historicalFilters);
        // attach adapter to recycler view
        recyclerView.setAdapter(filterAdapter);
        // set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // get filter list from shared preferences
        getHistoricalFilters(filterAdapter);
    }

    private void getHistoricalFilters(HistoricalFiltersAdapter filterAdapter) {
        // read back str from shared preferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getResources().getString(R.string.comic_me_app), Context.MODE_PRIVATE);
        String str = sharedPref.getString(getResources().getString(R.string.historical_filters), null);

        // decode Json
        if(str != null){
            Gson gson = new Gson();
            Type typeListOfComicFilter = new TypeToken<List<ComicFilter>>(){}.getType();
            Constants.COMIC_FILTERS_HISTORICAL = gson.fromJson(str, typeListOfComicFilter);

            // notify filter_datas changed
            filterAdapter.comicFilters.clear();
            filterAdapter.comicFilters.addAll(Constants.COMIC_FILTERS_HISTORICAL);
            filterAdapter.notifyDataSetChanged();
        }
    }
}