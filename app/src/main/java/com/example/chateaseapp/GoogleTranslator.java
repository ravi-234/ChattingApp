package com.example.chateaseapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chateaseapp.databinding.ActivityGoogleTranslatorBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class GoogleTranslator extends AppCompatActivity {
    private final int req_code=105;
   ActivityGoogleTranslatorBinding binding;

    String[] fromLanguages = {"From", "English", "Chinese", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Gujrati","Urdu"};
    String[] toLanguages = {"To", "English", "Chinese", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Gujrati","Urdu"};
    ///private static final int Request_Permission_Code = 1; instead used req_code=105
    int languageCode, fromLanguageCode, toLanguageCode = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoogleTranslatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        //Sourcelanguage from dropdown view work start->
        binding.fromspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

         //For dropdown view in from and to langauge
        ArrayAdapter fromAdapter=new ArrayAdapter(this, R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fromspinner.setAdapter(fromAdapter);
        //Sourcelanguage from dropdown view work->End

        //Destinationlanguage from dropdown view work start->
        binding.tospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode=getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter toadapter=new ArrayAdapter(this, R.layout.spinner_item,toLanguages);
        toadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.tospinner.setAdapter(toadapter);
        //Destinationlanguage from dropdown view work->End

        binding.translatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.translatedtext.setText("");
                if(binding.editsourcetext.toString().isEmpty())
                {
                    binding.editsourcetext.setError("Please enter some text");

                }
                else if(fromLanguageCode==0)
                {
                    Toast.makeText(getApplicationContext(), "Please choose source language", Toast.LENGTH_SHORT).show();

                }
                else if(toLanguageCode==0)
                {
                    Toast.makeText(getApplicationContext(), "Please choose destination language", Toast.LENGTH_SHORT).show();

                }
                else
                {
                   translatetext(fromLanguageCode,toLanguageCode,binding.editsourcetext.getText().toString());

                }

            }
        });


        binding.translatormic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Need to Speak");
                try{
                    startActivityForResult(intent,req_code);

                }
                catch (ActivityNotFoundException e)
                {
                    Toast.makeText(getApplicationContext(),"Sorry,Your device not supporting",Toast.LENGTH_LONG).show();
                }

            }
        });

        binding.translateleftarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent2);
            }
        });





    }

    public void translatetext(int fromLanguageCode,int toLanguageCode,String sourcetext)
    {
        binding.translatedtext.setText("Downloading Model...");
        FirebaseTranslatorOptions options=new FirebaseTranslatorOptions.Builder().setSourceLanguage(fromLanguageCode).setTargetLanguage(toLanguageCode).build();
        FirebaseTranslator translator= FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions=new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                binding.translatedtext.setText("Translating...");
                translator.translate(sourcetext).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.translatedtext.setText(s);
                        Intent resultIntent=new Intent();//Translated message sending back to chatactivity messagebox
                        resultIntent.putExtra("translatedmsg",s);
                        setResult(RESULT_OK,resultIntent);
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.translatedtext.setText("Failed to translate");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
             binding.translatedtext.setText("Failed to download the model"+e.getMessage());
            }
        });



    }
//"To", "English", "Chinese", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Gujrati","Urdu"
    public int getLanguageCode(String language) {
        int languageCode=0;
        switch (language)
        {
            case "English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;
           case "Chinese":
                languageCode= FirebaseTranslateLanguage.ZH;
                break;
            case "Arabic":
                languageCode= FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode= FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languageCode= FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                languageCode= FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode= FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode= FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                languageCode= FirebaseTranslateLanguage.HI;
                break;
            case "Gujrati":
                languageCode= FirebaseTranslateLanguage.GU;
                break;

            case "Urdu":
                languageCode= FirebaseTranslateLanguage.UR;
                break;
            default: languageCode=0;


        }
        return languageCode;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case req_code:
                if(resultCode==RESULT_OK&&data!=null)
                {
                    ArrayList result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    binding.editsourcetext.setText((String)result.get(0));
                }
                break;

        }
    }
}