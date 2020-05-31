package com.absolutezero.memelo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    static ImageView imageView;
    static ProgressBar progressBar;
    static Intent aboutIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar= findViewById(R.id.progressBar2);
        imageView= findViewById(R.id.imageView);
        aboutIntent= new Intent(getApplicationContext(),AboutActivity.class);

        refreshImage(getCurrentFocus());
    }

    public void refreshImage(View view){
        //Checking if Device is connected to Internet or not
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            Toast.makeText(this, "No Internet Connection Found :(", Toast.LENGTH_LONG).show();
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            imageView.setClickable(false);
            DownloadJSON downloadImage = new DownloadJSON();
            String result = "";
            try {
                result = downloadImage.execute("https://meme-api.herokuapp.com/gimme").get();
                Log.i("API hit Result", result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuAbout: {
                startActivity(aboutIntent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}

class DownloadJSON extends AsyncTask<String,Void,String>{

    @Override
    protected String doInBackground(String... strings) {

        String jsonData="";

        try {
            URL url= new URL(strings[0]);
            HttpURLConnection urlConnection= (HttpURLConnection) url.openConnection();
            InputStream inputStream= urlConnection.getInputStream();
            int data=inputStream.read();

            while(data!=-1){
                jsonData+=(char)data;
                data=inputStream.read();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        try {
            JSONObject apiJson= new JSONObject(s);
            String imageURL= apiJson.getString("url");
            Log.i("i",imageURL);

            //Calling DownloadImage at the end of getting image url
            DownloadImage downloadImage= new DownloadImage();

            downloadImage.execute(imageURL);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class DownloadImage extends  AsyncTask<String, Void, Bitmap>{

    @Override
    protected Bitmap doInBackground(String... strings) {
        URL imageUrl;
        Bitmap img=null;
        try {
            imageUrl= new URL(strings[0]);

            HttpURLConnection urlConnection= (HttpURLConnection) imageUrl.openConnection();
            InputStream inputStream= urlConnection.getInputStream();

            img= BitmapFactory.decodeStream(inputStream);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return img;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        MainActivity.imageView.setClickable(true);
        MainActivity.progressBar.setVisibility(View.INVISIBLE);
        MainActivity.imageView.setImageBitmap(bitmap);
    }
}