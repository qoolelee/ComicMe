package com.uuballgame.comicme;


import android.content.Context;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static String USER_NAME = "guest";
    public static String USER_PASSWORD = "guestPass";

    public static final String GET_FILTERS_DATA_LIST_URL = "http://34.105.126.48/ComicMe/Api/getComicFiltersList.php";

    public static List<ComicFilter> COMIC_FILTERS_LIST = new ArrayList<>();
    public static List<ComicFilter> COMIC_FILTERS_HISTORICAL = new ArrayList<>();
    public static List<ComicSourceImage> COMIC_SOURCE_IMAGE_LIST = new ArrayList<>();

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }
}
