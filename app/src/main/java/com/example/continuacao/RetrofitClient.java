package com.example.continuacao;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // IMPORTANTE: use o seu link com / no final
    private static final String BASE_URL = "https://api-estoque.whyguiih.workers.dev/";
    private static Retrofit retrofit = null;

    public static ApiService getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}