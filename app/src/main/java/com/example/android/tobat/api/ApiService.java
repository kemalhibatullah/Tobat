package com.example.android.tobat.api;

import com.example.android.tobat.model.Model;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    /*@GET("/daily.json?key=c8d3b272d03dc57c9c7f8cabe554271f")*/
    @GET("/jogja.json")
    Call<Model> getJadwal();
}
