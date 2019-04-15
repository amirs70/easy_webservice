package ir.itroid.easyhttpweb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;


public class WebService extends AsyncTask<String, String, String> {
    private static final String DBG = "DBUG";
    public static String ADDR;
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private String serviceURL;
    private String result;
    private String method = "POST";
    private String path = "";
    private HashMap<String, String> params;
    private String query = null;
    private String url;
    private HashMap<String, String> headers = null;
    private OnWebServiceActionListener onResult;
    private boolean log;
    private String[] resultKeys = new String[]{"result"};

    public WebService(Context context) {
        serviceURL = WebService.ADDR;
        WebService.context = context;
    }

    public WebService(Context context, String url) {
        serviceURL = url;
        WebService.context = context;
    }

    public WebService setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
        return this;
    }

    public WebService setPath(String path) {
        this.path = path;
        return this;
    }

    public WebService addPath(String path) {
        this.path += "/" + path;
        return this;
    }

    public WebService setQuery(String query) {
        this.query = query;
        return this;
    }

    public WebService setMethod(String method) {
        this.method = method;
        return this;
    }

    public WebService setAction(String action) {
        return addParams("action", action);
    }

    public WebService addParams(String key, String val) {
        if (val == null) return this;
        if (this.params != null) {
            this.params.put(key, val);
        } else {
            this.params = new HashMap<>();
            this.params.put(key, val);
        }
        return this;
    }

    public WebService setParams(HashMap<String, String> params) {
        this.params = params;
        return this;
    }

    public WebService addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    private HttpsURLConnection addAllHeaders(HttpsURLConnection connection) {
        if (this.headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return connection;
    }

    private HttpURLConnection addAllHeaders(HttpURLConnection connection) {
        if (this.headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return connection;
    }

    public String getExceptionString(Exception e) {
        return "ERROR: " +
                e.getMessage() +
                " at " + e.getStackTrace()[0].getLineNumber() +
                " in " + e.getStackTrace()[0].getClassName() +
                " (" + e.getStackTrace()[0].getMethodName() + ")";
    }

    private String httpsSendRequest() {
        result = "";
        //HttpsTrustManager.allowAllSSL();
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            SSLEngine engine = sslContext.createSSLEngine();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            this.url = serviceURL + path + "/";
            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection = addAllHeaders(connection);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            if (this.query == null) this.query = getQuery(params);
            writer.write(this.query);
            writer.flush();
            writer.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                result = getStringFromOutputString(connection.getInputStream());
            }
        } catch (IOException e) {
            Log.i("DBUG", "IOException1");
            Log.i("DBUG", getExceptionString(e));
        }
        return result;
    }


    private String httpSendRequest() {
        result = "";
        try {
            this.url = serviceURL + path + "/";
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection = addAllHeaders(connection);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            if (this.query == null) this.query = getQuery(params);
            writer.write(this.query);
            writer.flush();
            writer.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                result = getStringFromOutputString(connection.getInputStream());
            }
        } catch (IOException e) {
            Log.i("DBUG", "IOException11");
        }
        return result;
    }

    private String sendRequest() {
        if (serviceURL.contains("http")) {
            return httpSendRequest();
        } else if (serviceURL.contains("https")) {
            return httpsSendRequest();
        }
        return null;
    }

    private String getStringFromOutputString(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            Log.i("DBUG", "IOException3");
        }
        return result.toString();
    }

    private String getQuery(HashMap<String, String> params) {
        if (params == null || params.size() < 1) return "";
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            try {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException | NullPointerException e) {
                Log.i("DBUG", "IOException4");
            }
        }
        return result.toString();
    }

    public WebService setOnResultListener(OnWebServiceActionListener onResult) {
        this.onResult = onResult;
        return this;
    }

    public WebService withLog(boolean withLog) {
        log = withLog;
        return this;
    }

    public WebService setResultKeys(String... keys) {
        resultKeys = keys;
        return this;
    }

    public WebService withLog() {
        return this.withLog(true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (onResult != null) onResult.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        return sendRequest();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (log) Log.i(DBG, s);
        if (onResult != null) onResult.onResult(s);
        try {

            JSONObject res = new JSONObject(s);

            if (res.has("stat") && res.getInt("stat") > 0) {

                JSONObject results = new JSONObject();

                int ln = resultKeys.length;
                int finalResult = 0;

                if (ln > 0) {
                    for (String rk : resultKeys) {
                        if (res.has(rk)) {
                            results.put(rk, res.get(rk));
                            finalResult++;
                        }

                    }
                }

                if (finalResult > 0) {
                    onResult.onSuccess(results);
                } else {
                    onResult.onError(res.toString());
                }
            } else {
                onResult.onError(res.toString());
            }

        } catch (JSONException e) {
            onResult.onError(s);
            Log.i(DBG, getExceptionString(e));
        }
    }

    public interface OnWebServiceActionListener {

        void onPreExecute();

        default void onError(String result) {

        }

        default void onSuccess(JSONObject result) {

        }

        default void onResult(String result) {

        }
    }

}
