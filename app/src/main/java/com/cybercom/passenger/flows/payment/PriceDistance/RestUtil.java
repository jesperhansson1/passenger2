package com.cybercom.passenger.flows.payment.PriceDistance;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static com.cybercom.passenger.model.ConstantValues.GOOGLE_API_BASE_URL;

public class RestUtil {
    private static RestUtil self;
    private Retrofit mRetrofit;

    private RestUtil() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(GOOGLE_API_BASE_URL)
                        .addConverterFactory(JacksonConverterFactory.create());
        mRetrofit = builder.client(httpClient).build();
    }

    public static RestUtil getInstance() {
        if (self == null) {
            synchronized(RestUtil.class) {
                if (self == null) {
                    self = new RestUtil();
                }
            }
        }
        return self;
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

}
