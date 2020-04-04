package com.phamhuuhoang.newfeedapp.Api;

import com.phamhuuhoang.newfeedapp.models.News;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
//TODO: "https://newsapi.org/v2/"+"top-headlines?country=&apiKey="
    @GET("top-headlines")
    Call<News> getNews(
        @Query("country") String country,
        @Query("apiKey") String apiKey
    );

    @GET("everything")
    Call<News> getNewSearch(
            @Query("q") String keyword,
            @Query("language") String language,
            @Query("sortBy") String sortBy,
            @Query("apiKey") String apiKey

    );






}
