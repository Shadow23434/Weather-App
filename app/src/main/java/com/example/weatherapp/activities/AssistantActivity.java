package com.example.weatherapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
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
import com.google.ai.client.generativeai.Chat;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AssistantActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ArrayList<ChatModel> chatModelArrayList;
    private ChatAdapter chatAdapter;
    private final String ASSISTANT = "assistant";
    private final String USER = "user";
    private RecyclerView rvChatSection;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    public static final Integer RECORD_AUDIO_REQUEST_CODE = 1;
    private AppCompatImageView mic_btn;
    private TextView textget;
    private EditText editText;
    private LottieAnimationView mic_animation;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        textToSpeech = new TextToSpeech(this, this);
        textget = findViewById(R.id.textget);
        editText = findViewById(R.id.edit_user_mess);
        mic_btn = findViewById(R.id.mic_btn);
        mic_animation = findViewById(R.id.mic_animation);

        initRvChatSection();
        checkPermission();
        onSpeechRecognization();
        onEnterEditText();
        onClickSendMessage();
        onClickMicButton();
        animationMicButton();
        goHome();
    }

    private void animationMicButton() {
        mic_animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.stopListening();
                mic_animation.setVisibility(View.INVISIBLE);
                mic_btn.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onClickMicButton() {
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

    private void onSpeechRecognization() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
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
                    scrollChatSection();
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
    }

    private void initRvChatSection() {
        chatModelArrayList = new ArrayList<>();
        rvChatSection = findViewById(R.id.chat_section);
        chatAdapter = new ChatAdapter(chatModelArrayList, this);
        rvChatSection.setLayoutManager(new LinearLayoutManager(this));
        rvChatSection.setAdapter(chatAdapter);
    }

    private void onEnterEditText() {
        if (editText.getText() != null) {
            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                        chatModelArrayList.add(new ChatModel(editText.getText().toString(), USER));
                        runOnUiThread(() -> {
                            chatAdapter.notifyDataSetChanged();
                            scrollChatSection();
                        });
                        getResponse(editText.getText().toString());
                        editText.setText("");
                        return true;
                    }
                    return false;
                }
            });
        } else {
            Toast.makeText(this, "Please enter the message", Toast.LENGTH_SHORT).show();
        }
    }

    private void onClickSendMessage() {
        if (editText.getText() != null) {
            AppCompatImageView send_btn = (AppCompatImageView) findViewById(R.id.send_btn);
            send_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatModelArrayList.add(new ChatModel(editText.getText().toString(), USER));
                    chatAdapter.notifyDataSetChanged();
                    scrollChatSection();
                    getResponse(editText.getText().toString());
                    editText.setText("");
                }
            });
        } else {
            Toast.makeText(this, "Please enter the message", Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollChatSection() {
        new Handler().postDelayed(new Runnable() {
            int lastPos = rvChatSection.getAdapter().getItemCount() - 1;
            @Override
            public void run() {
                rvChatSection.smoothScrollToPosition(lastPos);
            }
        }, 500);
    }

    private void goHome() {
        AppCompatImageView back_btn = (AppCompatImageView) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AssistantActivity.this, MainActivity.class));
            }
        });
    }

    @NonNull
    private GenerativeModelFutures getModel() {
        SafetySetting safetySetting = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH);
        GenerationConfig.Builder builder = new GenerationConfig.Builder();
        builder.temperature = 0.9f;
        builder.topK = 16;
        builder.topP = 0.1f;
        GenerationConfig generationConfig = builder.build();
        RequestOptions requestOptions = new RequestOptions();
        Content content = new Content.Builder()
                .addText("You are a friendly and knowledgeable weather assistant. You provide accurate and concise weather forecasts based on user queries, using data for current and future conditions. Be specific, include temperature ranges, precipitation chances, wind speeds, and any weather alerts if applicable. Tailor your responses to the user's location and requested time period. Make your tone clear, helpful, and approachable. If users ask for advice, offer practical tips based on the weather conditions, such as clothing suggestions, travel precautions, or outdoor activity recommendations." +
                        ""
//                        String.format("Location user prompt: %s.", cityName) +
//                        String.format("Hourly weather data: %s.", hourlyWeatherData) +
//                        String.format("Daily weather data: %s.", dailyWeatherData)
                )
                .build();

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                BuildConfig.gemini_api,
                generationConfig,
                Collections.singletonList(safetySetting),
                requestOptions,
                null,
                null,
                content
        );
        return GenerativeModelFutures.from(gm);
    }

    private void getResponse(String message) {
        Log.e("GEMINI: ", "Connected!!!");
        Log.e("GEMINI: ", "User:" + message);

        GenerativeModelFutures model = getModel();
        Content content = new Content.Builder().addText(message).build();
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        Log.e("GEMINI:  ", "Fetching successful");
                        chatModelArrayList.add(new ChatModel(result.getText().toString(), ASSISTANT));
                        Log.e("onSuccess: ", result.getText().toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatAdapter.notifyDataSetChanged();
                            }
                        });
                        speak(result.getText().toString());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("GEMINI: ", "Fetching Fail!!!");
                        Log.e("Gemini: ", t.toString());
                        chatModelArrayList.add(new ChatModel("Something went wrong, please try again!", ASSISTANT));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                },
                executor);
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
