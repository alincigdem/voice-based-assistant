package com.example.voicebasedassistant;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.app.ProgressDialog.show;
import static android.widget.Toast.makeText;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.util.Log;



public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private EditText editText;
    private String stringURLEndPoint = "https://api.openai.com/v1/chat/completions";
    private String stringAPIKey = "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";
    private String stringOutput = "";
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    private FirebaseAuth auth;
    boolean isSpeechRecognitionActive;
    ArrayList<String> arrayList;
    public String isim;
    public String phoneNumber;
    private static final int REQUEST_CALL_PERMISSION = 1;
    public String kisi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.TextView);
        Button copyCodeButton = findViewById(R.id.buttonCopyCode);

        arrayList = new ArrayList<>();


        copyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyTextToClipboard(textView.getText().toString());
            }
        });


        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        textView = findViewById(R.id.TextView);
        editText = findViewById(R.id.EditText);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setSpeechRate((float) 0.8);

            }
        });


        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
            }


            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                String string = "";
                textView.setText("");
                if (matches != null) {
                    string = matches.get(0);
                    editText.setText(string);


                    //Spotify kelimesini içeren komutları kontrol et
                    if (string.toLowerCase(Locale.getDefault()).contains("spotify aç")) {
                        openSpotify();
                    } else if (string.toLowerCase(Locale.getDefault()).contains("harita aç")) {
                        openGoogleMaps();
                    } else if (string.toLowerCase(Locale.getDefault()).contains("google aç")) {
                        openGoogleSearch();
                    } else if (string.toLowerCase(Locale.getDefault()).contains("youtube aç")) {
                        openYouTube();
                    } else if (string.toLowerCase(Locale.getDefault()).contains("takvim aç")) {
                        openCalendar();
                    } else if (string.toLowerCase(Locale.getDefault()).contains("mesaj aç")) {
                        openMessaging();
                    } else if (string.toLowerCase(Locale.getDefault()).contains("whatsapp aç")) {
                        openWhatsApp();
                    } else if (string.toLowerCase(Locale.getDefault()).contains("play store aç")) {
                        openPlayStore();
                    } else if (string.toLowerCase(Locale.getDefault()).contains("mail aç")) {
                        openGmail();
                    }
                    else if(string.toLowerCase(Locale.getDefault()).contains("kişisini ara")) {
                        // Extract the contact name from the recognized speech
                        String contactName = extractContactName(string);
                        Log.d("ContactName", "Contact Name: " + contactName );
                        isim=contactName;
                        Log.d("ContactName", "isim: " + isim );
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                && checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS},
                                    1);
                        } else {
                            getcontact();

                        }
                       /* if (!TextUtils.isEmpty(isim)) {
                            // Now you have the contact name, you can perform the calling action

                        } else {
                            // If contact name is empty, handle it accordingly
                            textView.setText("Kişi adını anlayamadım. Lütfen tekrar deneyin.");
                        }*/
                    }
                    else {
                        ChatGPTModel(string);
                    }
                }

            }


            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });




    }

    private void getcontact() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                , null, null, null, null);


        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String mobile = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
          //  arrayList.add(name + "  " + mobile + "\n");

            isim = isim.toUpperCase();
            name = name.toUpperCase();

            String cumle = name;
            String arananKelime = isim;
            boolean kelimeVarMi = cumle.contains(arananKelime);

            if (kelimeVarMi)
            {
                Log.d("ContactName", " yeni name: " + mobile + "  " + name);
                Log.d("ContactName", "yeni isim: " + mobile + "  " + isim);
                phoneNumber="tel:" + mobile;
                kisi= isim;
            }



        }

        // İzin kontrolü
        if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // İzin varsa aramayı başlat
            makeCall(phoneNumber);
        } else {
            // İzin yoksa kullanıcıdan izin iste
            requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }



    }

    private void makeCall(String phoneNumber) {
        // Telefon uygulamasını başlatmak için bir Intent oluşturun
        Intent callIntent = new Intent(Intent.ACTION_CALL);

        // Uri ile telefon numarasını ayarlayın
        callIntent.setData(Uri.parse(phoneNumber));
        textView.setText(kisi + " Aranıyor...");
        textToSpeech.speak(kisi + "Aranıyor", TextToSpeech.QUEUE_FLUSH, null, null);

        // Intent'i başlatın
        startActivity(callIntent);
        editText.setText("");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1)
        {
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                getcontact();
            }
        }
    }

    private String extractContactName(String speech) {
        String keyword = "kişisini ara";
        int startIndex = speech.indexOf(keyword);
        if (startIndex != -1) {
            String contactName = speech.substring(0, startIndex).trim();
            return contactName;
        }
        return "";
    }


    // Spotify uygulamasını açan metot
    private void openSpotify() {
        textView.setText("Spotify Açılıyor ...");
        JSONObject jsonObject = new JSONObject();
        textToSpeech.speak("Spotify Açılıyor", TextToSpeech.QUEUE_FLUSH, null,null);
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.spotify.music");
        if (launchIntent != null) {
            startActivity(launchIntent);
            editText.setText("");
            return;
        } else {
            // Spotify yüklü değilse, kullanıcıya uygulamayı indirmesi için bir mesaj gösterilebilir.
            Toast.makeText(this, "Spotify uygulaması bulunamadı. Lütfen indirin.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGoogleMaps() {
        textView.setText("Google Haritaları Açılıyor ...");
        JSONObject jsonObject = new JSONObject();
        textToSpeech.speak("Google Haritaları Açılıyor", TextToSpeech.QUEUE_FLUSH, null, null);
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
        if (launchIntent != null) {
            startActivity(launchIntent);
            editText.setText("");
        } else {
            // Google Maps app not installed, provide a message to the user
            Toast.makeText(this, "Google Haritaları uygulaması bulunamadı. Lütfen indirin.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGoogleSearch() {
        textView.setText("Google Açılıyor ...");
        textToSpeech.speak("Google Açılıyor", TextToSpeech.QUEUE_FLUSH, null, null);

        String searchUrl = "https://www.google.com/";

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
        startActivity(browserIntent);
        editText.setText("");
    }

    private void openYouTube() {
        textView.setText("YouTube Açılıyor ...");
        JSONObject jsonObject = new JSONObject();
        textToSpeech.speak("YouTube Açılıyor", TextToSpeech.QUEUE_FLUSH, null, null);
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
        if (launchIntent != null) {
            startActivity(launchIntent);
            editText.setText("");
        } else {
            // Google Maps app not installed, provide a message to the user
            Toast.makeText(this, "Youtube uygulaması bulunamadı. Lütfen indirin.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCalendar() {
        textView.setText("Takvim Açılıyor ...");
        textToSpeech.speak("Takvim Açılıyor", TextToSpeech.QUEUE_FLUSH, null, null);

        Intent calendarIntent = new Intent(Intent.ACTION_VIEW);
        calendarIntent.setData(Uri.parse("content://com.android.calendar/time"));
        startActivity(calendarIntent);
        editText.setText("");
    }

    private void openMessaging() {
        textView.setText("Mesajlar Açılıyor ...");
        textToSpeech.speak("Mesajlar Açılıyor", TextToSpeech.QUEUE_FLUSH, null, null);

        Intent messagingIntent = new Intent(Intent.ACTION_MAIN);
        messagingIntent.addCategory(Intent.CATEGORY_APP_MESSAGING);
        startActivity(messagingIntent);
        editText.setText("");
    }

    private void openWhatsApp() {
        textView.setText("WhatsApp Açılıyor ...");
        textToSpeech.speak("WhatsApp Açılıyor", TextToSpeech.QUEUE_FLUSH, null, null);

        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        if (whatsappIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(whatsappIntent);
            editText.setText("");
        } else {
            Toast.makeText(this, "Whatsapp uygulaması bulunamadı. Lütfen indirin.\"", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPlayStore() {
        textView.setText("Play Store Açılıyor ...");
        textToSpeech.speak("Play Store Açılıyor", TextToSpeech.QUEUE_FLUSH, null, null);

        // Play Store sayfasının URL'si
        String playStoreUrl = "https://play.google.com/store/apps/details?id=com.android.vending";

        Uri uri = Uri.parse(playStoreUrl);
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, uri);

        if (playStoreIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(playStoreIntent);
            editText.setText("");
        } else {
            Toast.makeText(this, "Play Store uygulaması bulunamadı. Lütfen yükleyin.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGmail() {
        textView.setText("Gmail Açılıyor ...");
        textToSpeech.speak("Gmail Açılıyor", TextToSpeech.QUEUE_FLUSH, null, null);

        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");

        if (intent != null) {
            startActivity(intent);
        } else {
            String playStoreUrl = "https://play.google.com/store/apps/details?id=com.google.android.gm";
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl));

            if (playStoreIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(playStoreIntent);
                editText.setText("");
            } else {
                Toast.makeText(this, "Gmail uygulaması bulunamadı. Lütfen yükleyin.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);

        // Kopyalama işlemi tamamlandığında kullanıcıya bir Toast mesajı göster
        Toast.makeText(this, "Text copied ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.cikis_yap) {
                Toast.makeText(this, "You have clicked on logout", Toast.LENGTH_SHORT).show();

            auth.signOut();

                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
                finish(); // Bu, mevcut aktiviteyi kapatır.
                return true;
        }
                return super.onOptionsItemSelected(item);



    }

    @Override
    public void onBackPressed() {
        // Geri tuşuna basıldığında aynı aktivitede kalmak için bu metodu boş bırakabilirsiniz.
        //super.onBackPressed(); // Yorum satırına alındı.
    }

    @Override
    protected void onDestroy() {
        if (!isLogoutRequested()) {
            // Çıkış yapılmadıysa ve uygulama destroy ediliyorsa,
            // tekrar aynı aktiviteyi başlatın.
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        super.onDestroy();
    }


    private boolean isLogoutRequested() {
        return false; // Çıkış yapılmadı.
    }


    public void buttonAssist(View view) {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            return;
        }

        stringOutput = "";


        String userInput;
        if (TextUtils.isEmpty(editText.getText().toString())) {
            isSpeechRecognitionActive = true;
            speechRecognizer.startListening(intent);
            textView.setText("Dinliyorum...");

        } else {
            // Eğer editText doluysa, editText içindeki komutu al
            isSpeechRecognitionActive = false;
            userInput = editText.getText().toString();
            ChatGPTModel(userInput);
            editText.setText("");
            // getSentimentFromPython metodunu çağır ve kullanıcının girdisini sağla
            getSentimentFromPython(userInput);

        }

    }

    private void getSentimentFromPython(String sentence) {
        PythonInterface pythonInterface = new PythonInterface();
        String sentiment;

        try {
            // Python dosyasındaki yöntemi çağırarak cümlenin duygusunu al
            sentiment = pythonInterface.callPythonMethod(sentence);

            // Alınan duygu bilgisini TextView içinde göster
            textView.setText("Duygu: " + sentiment);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


   private void ChatGPTModel(String stringInput){

        textView.setText("Cevap Hazırlanıyor ...");
        JSONObject jsonObject = new JSONObject();
       textToSpeech.speak("Cevap Hazırlanıyor", TextToSpeech.QUEUE_FLUSH, null,null);


        try {
            jsonObject.put("model", "gpt-3.5-turbo");

            JSONArray jsonArrayMessage = new JSONArray();
            JSONObject jsonObjectMessage = new JSONObject();
            jsonObjectMessage.put("role", "user");
            jsonObjectMessage.put("content",  stringInput);
            jsonArrayMessage.put(jsonObjectMessage);

            jsonObject.put("messages", jsonArrayMessage);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                stringURLEndPoint, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String stringText = null;
                try {
                    stringText = response.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                stringOutput = stringOutput + stringText;
                textView.setText(stringOutput);
                textToSpeech.speak(stringOutput, TextToSpeech.QUEUE_FLUSH, null,null);

            }



        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Authorization", "Bearer " + stringAPIKey);
                mapHeader.put("Content-Type", "application/json");

                return mapHeader;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };

        int intTimeoutPeriod = 60000; // 60 seconds timeout duration defined
        RetryPolicy retryPolicy = new DefaultRetryPolicy(intTimeoutPeriod,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);


    }










}