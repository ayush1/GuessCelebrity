package guesscelebrity.example.com.guesscelebrity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Pattern pattern;
    private Matcher matcher;

    ArrayList<String> listCelebNames = new ArrayList<>();
    ArrayList<String> listCelebImages = new ArrayList<>();
    ArrayList<String> options = new ArrayList<>();
    int randIndex;
    Bitmap celebImage;

    ImageView imageViewCeleb;
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String result = null;

        imageViewCeleb = (ImageView) findViewById(R.id.iv_celebrity);
        button0 = (Button) findViewById(R.id.btn_0);
        button1 = (Button) findViewById(R.id.btn_1);
        button2 = (Button) findViewById(R.id.btn_2);
        button3 = (Button) findViewById(R.id.btn_3);

        try {
            result = new DownloadTask(this).execute("http://www.posh24.com/celebrities").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        String[] strings = result.split("<p class=\"link\">Top 100 celebs</p>");
        String[] celebData = strings[1].split("<div class=\"sidebarContainer\">");

        getCelebrityImage(celebData[0]);
        getCelebrityName(celebData[0]);

        createNewQuestion();
    }

    public void createNewQuestion(){
        Random random = new Random();
        int question = random.nextInt(listCelebNames.size());

        try {
            celebImage = new DownloadImageTask().execute(listCelebImages.get(question)).get();
            imageViewCeleb.setImageBitmap(celebImage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        randIndex = random.nextInt(4);
        int randomCeleb;
        for(int i=0; i<4; i++){
            if(i == randIndex){
                options.add(listCelebNames.get(question));
            }else{
                randomCeleb = random.nextInt(listCelebNames.size());
                while(randomCeleb == question){
                    randomCeleb = random.nextInt(listCelebNames.size());
                }
                options.add(listCelebNames.get(randomCeleb));
            }
        }

        button0.setText(options.get(0));
        button1.setText(options.get(1));
        button2.setText(options.get(2));
        button3.setText(options.get(3));

        options.clear();
    }

    public void checkAnswer(View view){
        if(view.getTag().toString().equalsIgnoreCase(String.valueOf(randIndex))){
            Toast.makeText(this, "Correct Answer", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Wrong Answer", Toast.LENGTH_SHORT).show();
        }
        createNewQuestion();
    }

    private void getCelebrityName(String result) {
        pattern = Pattern.compile("alt=\"(.*?)\"");
        matcher = pattern.matcher(result);

        while(matcher.find()){
            String celebName = matcher.group(1);
            listCelebNames.add(celebName);
        }
    }

    private void getCelebrityImage(String result) {
        pattern = Pattern.compile("src=\"(.*?)\"");
        matcher = pattern.matcher(result);

        while(matcher.find()){
            String celebImage = matcher.group(1);
            listCelebImages.add(celebImage);
        }
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        Context context;
        public DownloadTask(Context applicationContext) {
            this.context =  applicationContext;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder stringBuilder = new StringBuilder();

                while(bufferedReader.readLine() != null){
                    stringBuilder.append(bufferedReader.readLine()).append('\n');
                }

                return stringBuilder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }
    }
}
