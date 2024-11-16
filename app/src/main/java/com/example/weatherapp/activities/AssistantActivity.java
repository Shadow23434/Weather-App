package com.example.weatherapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.chat.ChatAdapter;
import com.example.weatherapp.chat.ChatModel;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AssistantActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ArrayList<ChatModel> chatModelArrayList;
    private ChatAdapter chatAdapter;
    private final String ASSISTANT = "assistant";
    private final String USER = "user";
    AppCompatImageView mic_btn;
    private RecyclerView chat_section;
    TextToSpeech textToSpeech;
    SpeechRecognizer speechRecognizer;
    public static final Integer RECORD_AUDIO_REQUEST_CODE = 1;
    TextView textget;
    EditText editText;
    int count = 0;
    LottieAnimationView mic_animation;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        textget = findViewById(R.id.textget);
        editText = findViewById(R.id.edit_user_mess);

        // Go back home
        AppCompatImageView back_btn = (AppCompatImageView) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AssistantActivity.this, MainActivity.class));
            }
        });
        
        chatModelArrayList = new ArrayList<>();
        chat_section= findViewById(R.id.chat_section);
        chatAdapter = new ChatAdapter(chatModelArrayList, this);
        chat_section.setLayoutManager(new LinearLayoutManager(this));
        chat_section.setAdapter(chatAdapter);

        mic_btn = findViewById(R.id.mic_btn);
        mic_animation = findViewById(R.id.mic_animation);

        textToSpeech = new TextToSpeech(this, this);

        // Send message
        AppCompatImageView send_btn = (AppCompatImageView) findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatModelArrayList.add(new ChatModel(editText.getText().toString(), USER));
                chatAdapter.notifyDataSetChanged();
                getResponse(editText.getText().toString());
            }
        });

        checkPermission();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        if (speechRecognizer != null) {
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {
                    mic_animation.setVisibility(View.VISIBLE);
                    mic_btn.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {
                    mic_animation.setVisibility(View.INVISIBLE);
                    mic_btn.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(int error) {

                }

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> arrayList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    textget.setText(arrayList.get(0));
                    chatModelArrayList.add(new ChatModel(arrayList.get(0), USER));
                    chatAdapter.notifyDataSetChanged();
                    Log.e("onResults:", textget.getText().toString());
                    getResponse(textget.getText().toString());
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        } else {
            Log.e("SpeechRecognizer: ", "SpeechRecognizer is null!");
        }

        mic_animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.stopListening();
                mic_animation.setVisibility(View.INVISIBLE);
                mic_btn.setVisibility(View.VISIBLE);
            }
        });

        mic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                if (count == 0) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                    if (null != activeNetwork) {
                        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                            speechRecognizer.startListening(speechIntent);
                            mic_btn.setVisibility(View.INVISIBLE);
                            mic_animation.setVisibility(View.VISIBLE);
                        }

                        if (activeNetwork.getType() == connectivityManager.TYPE_MOBILE) {
                            speechRecognizer.startListening(speechIntent);
                            mic_btn.setVisibility(View.INVISIBLE);
                            mic_animation.setVisibility(View.VISIBLE);
                        }
                    } else {
                            Toast.makeText(AssistantActivity.this, "Turn on the internet and try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @SuppressLint("ObsoleteSdkInt")
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}
                    ,RECORD_AUDIO_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.length>0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private void getResponse(String message) {
//        String url = "https://generativelanguage.googleapis.com/v1beta/get?bid=178934&key=7aGUBUKKCj5ztv76&uid=[]&msg=" + message;
//        String Base_URL = "https://generativelanguage.googleapis.com/v1beta";
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(Base_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
//        Call<MsgModel> call = retrofitAPI.getMessage(url);
//
//        call.enqueue(new Callback<MsgModel>() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
//                if (response.isSuccessful()) {
//                    MsgModel model = response.body();
//                    chatModelArrayList.add(new ChatModel(model.getCnt().toString(), ASSISTANT));
//                    chatAdapter.notifyDataSetChanged();
//                    speak(model.getCnt().toString());
//                }
//            }
//            @Override
//            public void onFailure(Call<MsgModel> call, Throwable t) {
//                chatModelArrayList.add(new ChatModel("Something went wrong, please try again!", ASSISTANT));
//                chatAdapter.notifyDataSetChanged();
//            }
//        });
//    }

    private void getResponse(String message) {
        Log.e("GEMINI: ", "Connected!!!");
        Log.e("GEMINI: ", message);
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.gemini_api);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder().addText(message).build();
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        Log.e("GEMINI:  ", "fetching success");
                        chatModelArrayList.add(new ChatModel(result.getText().toString(), ASSISTANT));
                        Log.e("onSuccess: ", result.getText().toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update your RecyclerView or any UI component here
                                chatAdapter.notifyDataSetChanged();
                            }
                        });
                        speak(result.getText().toString());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("GEMINI: ", "Fetching Error!!");
                        chatModelArrayList.add(new ChatModel("Something went wrong, please try again!", ASSISTANT));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update your RecyclerView or any UI component here
                                chatAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                },
                executor);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result  = textToSpeech.setLanguage(Locale.getDefault());
            textToSpeech.setPitch(1);
            textToSpeech.setSpeechRate(1);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("textToSpeech: ", "Language are not supported");
            }
            else {
                speak("");
            }
        }
        else {
            Log.e("TextToSpeech: ", "Initialization failed");
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void speak(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
