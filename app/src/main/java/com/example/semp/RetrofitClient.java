package com.example.semp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // IMPORTANTE: use o seu link com / no final
    private static final String BASE_URL = "https://api-estoque.whyguiih.workers.dev/";

    // volatile garante que múltiplos threads lidem com a variável corretamente
    private static volatile Retrofit retrofit = null;

    public static ApiService getApi() {
        if (retrofit == null) {
            // Thread-safe Singleton (evita criação de múltiplas instâncias)
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit.create(ApiService.class);
    }
}