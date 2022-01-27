package com.uuballgame.comicme;


import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static String USER_NAME = "guest";
    public static String USER_PASSWORD = "guestPass";

    public static final String GET_FILTERS_DATA_LIST_URL = "http://34.105.126.48/ComicMe/Api/getComicFiltersList.php";

    public static List<ComicFilter> COMIC_FILTERS_LIST = new ArrayList<>();
    public static List<ComicFilter> COMIC_FILTERS_HISTORICAL = new ArrayList<>();
    public static List<ComicSourceImage> COMIC_SOURCE_IMAGE_LIST = new ArrayList<>();
}
