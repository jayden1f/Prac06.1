package curtin.edu.prac06;


import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private ProgressBar progressBar;
    private TextView textArea;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textArea = findViewById(R.id.textArea);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        Button downloadBtn = findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new MyTask().execute();
            }
        });
    }

    private class MyTask extends AsyncTask<Void, Void, String>
    {
        private String result;
        @Override
        protected String doInBackground(Void... params)
        {

            String urlString = Uri.parse("https://10.0.0.12:8000/testwebservice/rest")
                    .buildUpon()
                    .appendQueryParameter("method", "thedata.getit")
                    .appendQueryParameter("api_key", "01189998819991197253")
                    .appendQueryParameter("format", "json")
                    .build().toString();

            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpsURLConnection conn = null;
            try {
                conn = (HttpsURLConnection) url.openConnection();
                DownloadUtils.addCertificate(MainActivity.this, conn);
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
            try
            {
                try {
                    if(conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                    {
                        throw new IllegalArgumentException("connection issues");
                    }

                    InputStream is = conn.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    byte[] buffer = new byte[1024];
                    int bytesRead = is.read(buffer);
                    int progress = 0;
                    while(bytesRead > 0)
                    {
                        baos.write(buffer, 0, bytesRead);
                        bytesRead = is.read(buffer);
                        progress += bytesRead;
                        animateProgressBar(progress);
                    }
                    baos.close();
                    result = new String(baos.toByteArray());
                    Log.d("testinf result", result);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;


            }
            finally
            {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result)
        {
            String output = "";
            try
            {
                JSONObject jBase = new JSONObject(result);

                String[] jNames = new String[3];
                String[] jStrength = new String[3];
                String[] jRelationship = new String[3];
                JSONArray jList = jBase.getJSONArray("factions");

                for(int i = 0; i < jList.length(); i++)
                {
                    JSONObject curObject = jList.getJSONObject(i);
                    jNames[i] = curObject.getString("name");
                    jStrength[i] = curObject.getString("strength");
                    jRelationship[i] = curObject.getString("relationship");
                }

                for(int i = 0; i < jList.length(); i++)
                {
                    output = output + jNames[i]+": " + jStrength[i]+", " + jRelationship[i] +"\n";
                }
            }
            catch(JSONException e) {e.printStackTrace();}

            textArea.setText(output);
        }

        private void setProgressBar(int max)
        {
            runOnUiThread(new Runnable(){
                @Override
                public void run()
                {
                    progressBar.setMin(0);
                    progressBar.setMax(max);
                }
            });
        }

        private void animateProgressBar(int value)
        {
            runOnUiThread(new Runnable(){
                @Override
                public void run()
                {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(value);
                }
            });
        }
    }

    // ...
}

