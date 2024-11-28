package com.example.weatherapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.adapters.ChatAdapter;
import com.example.weatherapp.domains.models.ChatModel;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AssistantActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ArrayList<ChatModel> chatModelArrayList;
    private ChatAdapter chatAdapter;
    private final String ASSISTANT = "assistant";
    private final String USER = "user";
    private RecyclerView rvChatSection;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private AlertDialog recordDialog; // prompt user to enable record service
    private AppCompatImageView mic_btn;
    private TextView textget;
    private EditText editText;
    private LottieAnimationView mic_animation;
    private int count = 0;
    private GenerativeModelFutures AIModel;
    private String dailyData;
    private String hourlyData;
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");

        if (latitude != null && !latitude.isEmpty()
            && longitude != null && !longitude.isEmpty()) {
            initModel();
        }
        else {
            Log.e("Main Intent: ", "Latitude is null");
        }

        textToSpeech = new TextToSpeech(this, this);
        textget = findViewById(R.id.textget);
        editText = findViewById(R.id.edit_user_mess);
        mic_btn = findViewById(R.id.mic_btn);
        mic_animation = findViewById(R.id.mic_animation);

        initRvChatSection();
        checkPermission(); // if deny?
        onSpeechRecognition(); // check here
        onEnterEditText();
        onClickSendMessage();
        onClickMicButton(); //
        animationMicButton(); //
        goHome();
    }

    private void initModel() {
        fetchWeatherData(() -> {
            getModel();
        });
    }

    private void getModel() {
        SafetySetting safetySetting = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH);
        GenerationConfig.Builder builder = new GenerationConfig.Builder();
        builder.temperature = 0.9f;
        builder.topK = 16;
        builder.topP = 0.1f;
        GenerationConfig generationConfig = builder.build();
        RequestOptions requestOptions = new RequestOptions();
        Content content = new Content.Builder()
                .addText("You are a friendly and knowledgeable weather assistant. You provide a very short answer, but accurate and concise weather forecasts based on user queries, using data for current and future conditions. Be specific, include temperature ranges, precipitation chances, wind speeds, and any weather alerts if applicable. Tailor your responses to the user's location and requested time period. Make your tone clear, helpful, and approachable. If users ask for advice, offer practical tips based on the weather conditions, such as clothing suggestions, travel precautions, or outdoor activity recommendations.\n" +
                        String.format("Hourly weather data: %s.", hourlyData) +
                        String.format("Daily weather data: %s.", dailyData)
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
        AIModel = GenerativeModelFutures.from(gm);
    }

    private void getResponse(String message) {
        Log.e("GEMINI: ", "Connected!!!");
        Log.e("GEMINI: ", "User:" + message);

        Content content = new Content.Builder().addText(message).build();
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = AIModel.generateContent(content);
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

    private void fetchWeatherData(Runnable onComplete) {
        String hourlyUrl = String.format("https://api.weatherbit.io/v2.0/forecast/hourly?&lat=%s&lon=%s&key=%s&hours=12", latitude, longitude, BuildConfig.weather_api);
        Log.e("Fetch weather data: ", hourlyUrl);
        String dailyUrl = String.format("https://api.weatherbit.io/v2.0/forecast/daily?&lat=%s&lon=%s&key=%s", latitude, longitude, BuildConfig.weather_api);
        Log.e("Fetch weather data: ", dailyUrl);

        OkHttpClient client = new OkHttpClient();
        Request hourlyRequest = new Request.Builder().url(hourlyUrl).build();
        Request dailyRequest = new Request.Builder().url(dailyUrl).build();

        new Thread(() -> {
            // hourly
            try {
                Response hourlyResponse = client.newCall(hourlyRequest).execute();
                if (hourlyResponse.isSuccessful() && hourlyResponse.body() != null) {
                    hourlyData = hourlyResponse.body().string();
                    Log.e("Hourly data: ", hourlyData);
                }
            } catch (Exception e) {
                Log.e("Fetch hourly data: ", "Error");
                e.printStackTrace();
            }

            // daily
            try {
                Response dailyResponse = client.newCall(dailyRequest).execute();
                if (dailyResponse.isSuccessful() && dailyResponse.body() != null) {
                    dailyData = dailyResponse.body().string();
                    Log.e("Daily data: ", dailyData);
                }
            } catch (Exception e) {
                Log.e("Fetch daily data: ", "Error");
                e.printStackTrace();
            }
            onComplete.run();
        }).start();
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
                            Log.e("speechRecognizer", "OK");
                            speechRecognizer.startListening(speechIntent);
                            Log.e("speechRecognizer", "Start listening");
                            mic_btn.setVisibility(View.INVISIBLE);
                            mic_animation.setVisibility(View.VISIBLE);
                        }

                        if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                            speechRecognizer.startListening(speechIntent);
                            Log.e("speechRecognizer", "Start listening");
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

    private void onSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
            return;
        } else {
          Log.e("onSpeedRecognition: ", "Speech recognition is available");
        }

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
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "Audio recording error";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "Client-side error";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "Insufficient permissions";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "Network error";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "Network timeout";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "No recognition result matched";
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "RecognitionService busy";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "Server error";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "No speech input";
                            break;
                        default:
                            message = "Speech recognition error";
                            break;
                    }
                    Log.e("SpeechRecognizer Error", message);
                    Toast.makeText(AssistantActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResults(Bundle results) {
                    if (results != null) {
                        ArrayList<String> arrayList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                            textget.setText(arrayList.get(0));
                            chatModelArrayList.add(new ChatModel(arrayList.get(0), USER));
                            runOnUiThread(() -> chatAdapter.notifyDataSetChanged());
                            scrollChatSection();
                            Log.e("onResults:", "textget: " + textget.getText().toString());
                            getResponse(textget.getText().toString());
                    }
                    else {
                        Log.e("onResult:", "results is null");
                    }
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
                        hideKeyboard();
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
                    hideKeyboard();
                }
            });
        } else {
            Toast.makeText(this, "Please enter the message", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
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

    @SuppressLint("ObsoleteSdkInt")
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestRecordPermission();
        }
        else {
            Log.e("Check permission : ", "PERMISSION GRANTED");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length>0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("onRequestPermissionsResult", "Record permission granted");
            }
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(AssistantActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    // user selected deny
                    showRecordPermissionDialog();
                }
                else {
                    // user selected "don't ask again" and restart
                    showRecordServiceDialog();
                }
            }
        }
    }

    private void requestRecordPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);
    }

    private void showRecordServiceDialog() {
        ConstraintLayout recordPermissionLayout = findViewById(R.id.record_service_layout);
        View view = LayoutInflater.from(AssistantActivity.this).inflate(R.layout.record_service_dialog, recordPermissionLayout);
        Button openSettingsBtn = view.findViewById(R.id.open_settings_btn);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(AssistantActivity.this);
        builder.setView(view);
        recordDialog = builder.create();

        openSettingsBtn.findViewById(R.id.open_settings_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 102);
            }
        });

        cancelBtn.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordDialog.dismiss();
                Toast.makeText(AssistantActivity.this, "Permission is necessary for the app to work", Toast.LENGTH_SHORT).show();
                finishAffinity(); // Closes all activities and exits the app
                System.exit(0);
            }
        });

        if (recordDialog.getWindow() != null) {
            recordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        recordDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                if (recordDialog != null) recordDialog.dismiss();
                Toast.makeText(AssistantActivity.this, "Record permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showRecordPermissionDialog() {
        ConstraintLayout recordPermissionLayout = findViewById(R.id.record_permission_layout);
        View view = LayoutInflater.from(AssistantActivity.this).inflate(R.layout.record_permission_dialog, recordPermissionLayout);
        Button grantPermissionBtn = view.findViewById(R.id.grant_permission_btn);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(AssistantActivity.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        grantPermissionBtn.findViewById(R.id.grant_permission_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                requestRecordPermission();
            }
        });

        cancelBtn.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Toast.makeText(AssistantActivity.this, "Permission is necessary for the app to work. Microphone button is disable.", Toast.LENGTH_SHORT).show();
//                finishAffinity(); // Closes all activities and exits the app
//                System.exit(0);
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
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
        Log.e("onDestroy: ", "Destroy");
        speechRecognizer.destroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
