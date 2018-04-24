package com.cybercom.passenger.network;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Base64;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainViewModel;
import com.cybercom.passenger.network.model.AuthRequest;
import com.cybercom.passenger.network.model.AuthResponse;
import com.cybercom.passenger.network.model.CollectRequest;
import com.cybercom.passenger.network.model.CollectResponse;
import com.cybercom.passenger.network.model.Requirement;
import com.cybercom.passenger.network.model.SignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import timber.log.Timber;

import static android.content.Context.WIFI_SERVICE;

//import java.util.Base64;
//import java.util.Base64;
//import org.apache.commons.codec.binary.Base64;

public class RetrofitHelper {

    private static final String TEST_BASE_URL = "https://appapi2.test.bankid.com/rp/v5/";
    public static final String PASSWORD = "qwerty123";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_COMPLETE = "complete";
    private static final String STATUS_FAILED = "failed";
    private static RetrofitHelper INSTANCE;
    private Context mContext;

    private BankIdService mBankIdService;
    private OkHttpClient mClient;


    public static RetrofitHelper getInstance(Context context, Context activityContext, MainViewModel mainViewModel) {
        if (INSTANCE == null) {
            INSTANCE = new RetrofitHelper(context.getApplicationContext(), activityContext);
        }
        return INSTANCE;
    }

    private RetrofitHelper(final Context appContext, final Context activityContext) {

        mContext = activityContext;
        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;

        try {

            InputStream inputStream;
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
//            fis = new FileInputStream(certificateFile);
            inputStream = appContext.getResources().openRawResource(R.raw.fptestcert2_20150818_102329);

            keyStore.load(inputStream, PASSWORD.toCharArray());

            Timber.i("keystore. aliases : %s", keyStore.aliases());
            Timber.i("keystore. type: %s", keyStore.getType());
            Timber.i("keystore. tostring : %s", keyStore.toString());
            Timber.i("keystore. : %s", keyStore.getProvider());
            Timber.i("keystore. : %s", keyStore.size());

//            Now that we have the KeyStore containing the client certificate, we can use it to build an SSLContext:

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, PASSWORD.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();


            // Truststore
            KeyStore localTrustStore  = KeyStore.getInstance("BKS");

            InputStream inStream  = appContext.getResources().openRawResource(R.raw.mytruststore);
            localTrustStore.load(inStream, "secret".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(localTrustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, tmf.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();

            mClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) (tmf.getTrustManagers())[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();

            mBankIdService = new Retrofit.Builder()
                    .baseUrl(TEST_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(mClient)
                    .build()
                    .create(BankIdService.class);

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String text = "I confirm the correctness of my personal information...";

        String base64signMessage = Base64.encodeToString("Test".getBytes(), Base64.DEFAULT);
        Timber.i("encodedBytes %s", base64signMessage);
        WifiManager wm = (WifiManager) ((Application)appContext).getSystemService(WIFI_SERVICE);
        assert wm != null;
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        Timber.i("ip: %s", ip);
        Toast.makeText(appContext, "Ip: "  + ip, Toast.LENGTH_LONG); //10.90.192.124

//        mBankIdService.signRequest(new SignRequest("178.21.87.53", base64signMessage)).enqueue(new Callback<AuthResponse>() {
//            @Override
//            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
//                                Timber.i("respo message: %s", response.message());
//                Timber.i("respo body: %s", response.body());
//                Timber.i("respo toString: %s", response.toString());
//                Timber.i("respo code: %s", response.code());
//
//                Timber.i("autostart: "  + response.body().getAutoStartToken());
////                AuthRequest testModel = gson.fromJson(response, AuthResponse.class);
//
//                startPollingCollects(response.body().getOrderRef());
//
//                Intent intent = new Intent();
//                intent.setPackage("com.bankid.bus");
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("bankid:///?autostarttoken=<" + response.body().getAutoStartToken() + ">&redirect=null "));
//                context.startActivity(intent);
//
//            }
//
//            @Override
//            public void onFailure(Call<AuthResponse> call, Throwable t) {
//
//            }
//        });

//        mBankIdService.authorizationRequest(new AuthRequest("178.21.87.53")).enqueue(new Callback<AuthResponse>() {
//        mBankIdService.authorizationRequest(new AuthRequest("10.90.192.124", new Requirement(true))).enqueue(new Callback<AuthResponse>() {
        mBankIdService.authorizationRequest(new AuthRequest("178.21.87.53", new Requirement(true))).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                Timber.i("respo message: %s", response.message());
                Timber.i("respo code: %s", response.code());
                Timber.i("autostart: "  + response.body().getAutoStartToken());

                startPollingCollects(response.body().getOrderRef());
                Intent intent = new Intent();
                intent.setPackage("com.bankid.bus");
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("bankid:///?autostarttoken=" + response.body().getAutoStartToken() + "&redirect=null"));
                appContext.startActivity(intent);
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {

            }
        });
    }


    public LiveData<AuthResponse> authRequest() {
        final MutableLiveData<AuthResponse> authResponseMutableLiveData = new MutableLiveData<>();

        mBankIdService.authorizationRequest(new AuthRequest("176.21.87.1", new Requirement(true))).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                Timber.i("respo message: %s", response.message());
                Timber.i("respo code: %s", response.code());
                Timber.i("autostart: "  + response.body().getAutoStartToken());

                authResponseMutableLiveData.setValue(response.body());


//                startPollingCollects(response.body().getOrderRef());
//                Intent intent = new Intent();
//                intent.setPackage("com.bankid.bus");
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("bankid:///?autostarttoken=" + response.body().getAutoStartToken() + "&redirect=null"));
//                appContext.startActivity(intent);
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {

            }
        });

        return authResponseMutableLiveData;
    }


        private void startPollingCollects(final String orderRef) {


        mBankIdService.collectRequest(new CollectRequest(orderRef)).enqueue(new Callback<CollectResponse>() {
            @Override
            public void onResponse(Call<CollectResponse> call, Response<CollectResponse> response) {
                Timber.i("response %s", response.body().getStatus());

                if (response.body().getStatus().equals(STATUS_PENDING)) {
                    new Handler(Looper.getMainLooper()).postDelayed((new Runnable() {
                        @Override
                        public void run() {
                            startPollingCollects(orderRef);
                        }
                    }), 2000);
                } else if (response.body().getStatus().equals(STATUS_COMPLETE)) {
                    //TODO: ...
                    // TODO: send

                    Timber.i("show auth ");
//                        AuthenticateDriverFragment dialogFragment = AuthenticateDriverFragment.newInstance(0, null);

//                        dialogFragment.show(((Activity)mContext).getFragmentManager(), "AUTH_DRIVER_FRAGMENT");



                } else if (response.body().getStatus().equals(STATUS_FAILED)) {

                    Timber.i("Failed: %s", response.message());
                }

            }

            @Override
            public void onFailure(Call<CollectResponse> call, Throwable t) {

            }
        });
    }

    public interface BankIdService {

        //https://appapi2.bankid.com/rp/v5
        @Headers("Content-Type: application/json")
        @POST("auth")
        Call<AuthResponse> authorizationRequest(@Body AuthRequest authRequest);

        @Headers("Content-Type: application/json")
        @POST("sign")
        Call<AuthResponse> signRequest(@Body SignRequest signRequest);

        @Headers("Content-Type: application/json")
        @POST("collect")
        Call<CollectResponse> collectRequest(@Body CollectRequest collectRequest);
//
//        @POST("api/1.3/user/add-last-viewed")
//        Call<String> setLastViewed(@Body LastViewed lastViewed);
//
//        @POST("api/1.3/reporting/gemius")
//        Call<JsonElement> reportGemiusStreamTracking(@Body GemiusStreamTrackReport streamTrackReport);
//
//        @GET("api/1.3/schedule/nownext/{channel}")
//        Call<JsonElement> checkCurrentLiveProgram(@Path("channel") String channel);
//
//        @GET("api/1.3/configuration/AndroidTvSettings")
//        Call<JsonElement> getAkamaiLicense();
//
//        @GET("api/1.3/programcard/{urn}")
//        Call<JsonElement> getProgramCard(@Path("urn") String urn);
//
//        @GET
//        Call<JsonElement> getStream(@Url String url);
    }


//    X509TrustManager trustManager;
//    SSLSocketFactory sslSocketFactory;
//    try {
//        trustManager = trustManagerForCertificates(trustedCertificatesInputStream());
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, new TrustManager[] { trustManager }, null);
//        sslSocketFactory = sslContext.getSocketFactory();
//    } catch (GeneralSecurityException e) {
//        throw new RuntimeException(e);
//    }
//
//    client = new OkHttpClient.Builder()
//            .sslSocketFactory(sslSocketFactory, trustManager)
//        .build();

//    private static SSLContext getSSLConfig(Context context) throws CertificateException, IOException,
//            KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
//
//        // Loading CAs from an InputStream
//        CertificateFactory cf = null;
//        cf = CertificateFactory.getInstance("X.509");
//
//        Certificate ca;
//        // I'm using Java7. If you used Java6 close it manually with finally.
//        try (InputStream cert = context.getResources().openRawResource(R.raw.mytruststore)) {
//            ca = cf.generateCertificate(cert);
//        }
//
//        // Creating a KeyStore containing our trusted CAs
//        String keyStoreType = KeyStore.getDefaultType();
//        KeyStore keyStore   = KeyStore.getInstance(keyStoreType);
//        keyStore.load(null, null);
//        keyStore.setCertificateEntry("ca", ca);
//
//        // Creating a TrustManager that trusts the CAs in our KeyStore.
//        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
//        tmf.init(keyStore);
//
//        // Creating an SSLSocketFactory that uses our TrustManager
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, tmf.getTrustManagers(), null);
//
//        return sslContext;
//    }
}
