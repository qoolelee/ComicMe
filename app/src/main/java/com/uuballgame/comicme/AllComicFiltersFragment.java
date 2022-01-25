package com.uuballgame.comicme;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllComicFiltersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllComicFiltersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllComicFiltersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllComicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllComicFiltersFragment newInstance(String param1, String param2) {
        AllComicFiltersFragment fragment = new AllComicFiltersFragment();
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
        View view = inflater.inflate(R.layout.fragment_all_comic_filter, container, false);

        // find recycler view in fragment
        RecyclerView recyclerView = view.findViewById(R.id.comic_filter_list);

        // add divider
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        // prepare data for adapter
        List<ComicFilter> comicFilters = Constants.COMIC_FILTERS_LIST;
        // Create the View holder adapter
        AllComicFiltersAdapter filterAdapter = new AllComicFiltersAdapter(comicFilters);
        // attach adapter to recycler view
        recyclerView.setAdapter(filterAdapter);
        // set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // get filter list from server
        getUpdatedFilters(view, filterAdapter);

        return view;
    }

    private void getUpdatedFilters(View view, AllComicFiltersAdapter filterAdapter) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Constants.GET_FILTERS_DATA_LIST_URL;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // stop progress bar
                        ProgressBar progressBar = view.findViewById(R.id.progressBar1);
                        progressBar.setVisibility(View.GONE);

                        // update lists
                        updateAllComicsFilters(response, filterAdapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // stop progress bar
                ProgressBar progressBar = view.findViewById(R.id.progressBar1);
                progressBar.setVisibility(View.GONE);

                // alert server connecting error
                Alert(getResources().getString(R.string.alert_filter_list_server_down), view, filterAdapter);
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("UserName", "guest");
                map.put("UserPassword", "guestpass");
                return map;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void updateAllComicsFilters(String response, AllComicFiltersAdapter filterAdapter) {
        Gson gson = new Gson();
        Type typeListOfComicFilter = new TypeToken<List<ComicFilter>>(){}.getType();
        Constants.COMIC_FILTERS_LIST = gson.fromJson(response, typeListOfComicFilter);

        // notify filter_datas changed
        filterAdapter.comicFilters.clear();
        filterAdapter.comicFilters.addAll(Constants.COMIC_FILTERS_LIST);
        filterAdapter.notifyDataSetChanged();
    }

    private void Alert(String alertMessage, View view, AllComicFiltersAdapter filterAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(alertMessage)
                .setTitle(R.string.error)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getUpdatedFilters(view, filterAdapter);
                    }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });

        builder.create().show();
    }
}