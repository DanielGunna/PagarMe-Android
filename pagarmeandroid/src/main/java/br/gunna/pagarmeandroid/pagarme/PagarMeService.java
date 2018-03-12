package br.gunna.pagarmeandroid.pagarme;


/**
 * Created by Daniel on 11/05/17.
 */


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class PagarMeService {
    private static final String META_DATA_KEY = "me.pagar.EncryptionKey";
    private Gson mGson;
    private OkHttpClient mOkHttpClient;
    private HttpLoggingInterceptor mLogInterceptor;
    private Interceptor mResponseInterceptor;
    private static PagarMeService sInstance;
    private PagarMeApi mServiceInstance;

    private PagarMeService() {
        initAppHostingService();
    }

    private PagarMeApi getmServiceInstance() {
        return mServiceInstance;
    }

    static PagarMeApi getServiceInstance() {
        if (sInstance == null) {
            sInstance = new PagarMeService();
        }
        return sInstance.getmServiceInstance();
    }

    private void initAppHostingService() {
        if (mServiceInstance == null) {
            initInterceptors();
            initOkHttp();
            initGson();
            initRetrofitService();
        }
    }

    private void initGson() {
        mGson = new GsonBuilder()
                .setLenient()
                .create();
    }

    private void initInterceptors() {
        mLogInterceptor = new HttpLoggingInterceptor();
        mLogInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        mResponseInterceptor = new Interceptor() {

            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response response = chain.proceed(chain.request());
                Log.w("Retrofit@Response", response.body().string());
                return response;
            }
        };
    }

    private void initOkHttp() {
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(mLogInterceptor)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    private void initRetrofitService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.PagarMe.API_URL)
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .client(mOkHttpClient)
                .build();
        mServiceInstance = retrofit.create(PagarMeApi.class);
    }

}
