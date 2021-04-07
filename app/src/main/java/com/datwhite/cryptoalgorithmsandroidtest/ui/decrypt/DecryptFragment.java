package com.datwhite.cryptoalgorithmsandroidtest.ui.decrypt;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.datwhite.cryptoalgorithmsandroidtest.R;
import com.datwhite.cryptoalgorithmsandroidtest.crypto.DES.DES;
import com.datwhite.cryptoalgorithmsandroidtest.crypto.GenerateKey;
import com.datwhite.cryptoalgorithmsandroidtest.crypto.Vigenere.Vigenere;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static android.app.Activity.RESULT_OK;


public class DecryptFragment extends Fragment {

    private static final Charset ISO_CHARSET = Charset.forName("ISO_8859_1");

    private DecryptViewModel decryptViewModel;

    private EditText inputTextDecrypt;
    private EditText inputKeyDecrypt;
    private EditText outputTextDecrypt;
    private Button decryptBtn;
    private Button copyBtnDec;
    private Button openFileBtnDec;
    private Button saveFileDecBtn;
    private RadioButton inputKeyBtnDec;
    private RadioButton copyKeyBtnDec;
    private RadioButton desBtn;
    private RadioButton vigenereBtn;

    private int mode = 0; //0 - DES, 1 - Виженер

    private View root;

    private LayoutInflater inflater;

    ClipboardManager clipboardManager;
    ClipData clipData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        decryptViewModel = new ViewModelProvider(this).get(DecryptViewModel.class);

        root = inflater.inflate(R.layout.fragment_decrypt, container, false);

        decryptBtn = root.findViewById(R.id.decryptBtn);
        copyBtnDec = root.findViewById(R.id.copyBtnDec);

        inputKeyBtnDec = root.findViewById(R.id.inputKeyBtnDec);
        copyKeyBtnDec = root.findViewById(R.id.copyKeyBtnDec);

        desBtn = root.findViewById(R.id.desBtn);
        vigenereBtn = root.findViewById(R.id.vigenereBtn);

        openFileBtnDec = root.findViewById(R.id.openFileBtnDec);
        saveFileDecBtn = root.findViewById(R.id.saveFileDecBtn);

        inputTextDecrypt = root.findViewById(R.id.inputTextDecrypt);
        inputKeyDecrypt = root.findViewById(R.id.inputKeyDecrypt);
        outputTextDecrypt = root.findViewById(R.id.outputTextDecrypt);

        //Кнопка "Ввести ключ"
        inputKeyBtnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputKeyDecrypt.setActivated(true);
                inputKeyDecrypt.setClickable(true);
                inputKeyDecrypt.setCursorVisible(true);
                inputKeyDecrypt.setFocusable(true);
                inputKeyDecrypt.setFocusableInTouchMode(true);
//                inputKeyEncrypt.setEnabled(true);

            }
        });
        //Кнопка "Скопировать ключ"
        copyKeyBtnDec.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                inputKeyDecrypt.setClickable(false);
                inputKeyDecrypt.setCursorVisible(false);
                inputKeyDecrypt.setFocusable(false);
                inputKeyDecrypt.setFocusableInTouchMode(false);
//                inputKeyEncrypt.setEnabled(false);
                String k = GenerateKey.getKey();
                inputKeyDecrypt.setText(k);
            }
        });

        //Кнопка "Расшифровать"
        decryptBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String enc_to_dec_text = inputTextDecrypt.getText().toString();
                String key_to_dec = inputKeyDecrypt.getText().toString();

                if (enc_to_dec_text.length() == 0 || key_to_dec.length() == 0) {
                    Toast.makeText(root.getContext(), "Сначала ведите текст и ключ", Toast.LENGTH_LONG).show();
                }
                try {
                    if (mode == 0) {
                        if (key_to_dec.length() != 8) {
                            Toast.makeText(root.getContext(), "Ключ должен быть длиной 8 символов", Toast.LENGTH_LONG).show();
                        } else {

                            byte[] decodedBytes = Base64.getDecoder().decode(enc_to_dec_text);
                            byte[] dec = DES.decrypt(decodedBytes, key_to_dec.getBytes());
                            String decrypted = new String(dec);

                            outputTextDecrypt.setText(decrypted);

                        }
                    } else if (mode == 1) {
                        String dec = Vigenere.decrypt(enc_to_dec_text, key_to_dec);

                        outputTextDecrypt.setText(dec);
                    }
                } catch (Exception e) {
                    Toast toast = Toast.makeText(root.getContext(), "Дешифровка невозможна ", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 200);
                    toast.show();
                }

            }
        });

        //Копнка "Сохранить"
        saveFileDecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputKeyDecrypt.length() == 0 || outputTextDecrypt.length() == 0) {
                    Toast.makeText(root.getContext(), "Сначала ведите текст и ключ", Toast.LENGTH_LONG).show();
                } else {
                    int permissionStatus = ContextCompat.checkSelfPermission(root.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                        System.out.println("ACCESS");
                    } else {
                        System.out.println("DON'T ACCESS");
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        System.out.println("THEN ACCESS");
                    }

                    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DES-test/");
                    path.mkdirs();
                    File file = new File(path, "decrypted.txt");

                    try {
                        path.mkdirs();

                        OutputStream os = new FileOutputStream(file);
                        String start = "ASU-LSTU";
                        String key = " KEY " + inputKeyDecrypt.getText().toString();
                        String enc = " CRYPT " + outputTextDecrypt.getText().toString();
                        String text = start + key + enc;
                        Log.i("TEXT", text);
                        os.write(text.getBytes());
                        os.flush();
                        os.close();

                        Toast.makeText(root.getContext(), "Файл сохранен", Toast.LENGTH_SHORT).show();
                        // Tell the media scanner about the new file so that it is
                        // immediately available to the user.
                        MediaScannerConnection.scanFile(root.getContext(),
                                new String[]{file.toString()}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.i("ExternalStorage", "Scanned " + path + ":");
                                        Log.i("ExternalStorage", "-> uri=" + uri);
                                    }
                                });
                    } catch (IOException e) {
                        // Unable to create file, likely because external storage is
                        // not currently mounted.
                        Log.w("ExternalStorage", "Error writing " + file, e);
                    }
                }
            }
        });

        //Кнопка "Открыть файл"
        openFileBtnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionStatus = ContextCompat.checkSelfPermission(root.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("ACCESS");
                    Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    photoPickerIntent.setType("text/plain");
                    startActivityForResult(photoPickerIntent, 1);
                } else {
                    System.out.println("DON'T ACCESS");
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    System.out.println("THEN ACCESS");
                    Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    photoPickerIntent.setType("text/plain");
                    startActivityForResult(photoPickerIntent, 1);
                }
            }
        });

        ClipboardManager clipboardManager = (ClipboardManager) root.getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        //Кнопка "Копировать"
        copyBtnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clipData = ClipData.newPlainText("text", outputTextDecrypt.getText().toString());
                clipboardManager.setPrimaryClip(clipData);

                Toast toast = Toast.makeText(root.getContext(), "Скопировано ", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 200);
                toast.show();
            }
        });

        //Кнопка режим "DES"
        desBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputKeyDecrypt.setActivated(true);
                inputKeyDecrypt.setClickable(true);
                inputKeyDecrypt.setCursorVisible(true);
                inputKeyDecrypt.setFocusable(true);
                inputKeyDecrypt.setFocusableInTouchMode(true);
//                inputKeyEncrypt.setEnabled(true);
                mode = 0;
            }
        });

        //Кнопка режим "Виженер"
        vigenereBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                inputKeyDecrypt.setClickable(false);
                inputKeyDecrypt.setCursorVisible(false);
                inputKeyDecrypt.setFocusable(false);
                inputKeyDecrypt.setFocusableInTouchMode(false);
//                inputKeyEncrypt.setEnabled(false);
                mode = 1;
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    Uri chosenImageUri = data.getData();

                    Cursor cursor = getActivity().getContentResolver().query(chosenImageUri, null, null, null, null);
                    cursor.moveToFirst();
                    String filePath = cursor.getString(0);
                    cursor.close();

                    System.out.println("PATH: " + filePath);

                    inputTextDecrypt = root.findViewById(R.id.inputTextDecrypt);
                    inputKeyDecrypt = root.findViewById(R.id.inputKeyDecrypt);

                    FileInputStream fin = null;
                    File file = new File(filePath);
                    // если файл не существует, выход из метода
                    if (!file.exists()) {
                        System.out.println("EXISTS");
                        Toast.makeText(root.getContext(), "EXISTS", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        fin = new FileInputStream(file);
                        byte[] bytes = new byte[fin.available()];
                        fin.read(bytes);
                        String text = new String(bytes);
//                        System.out.println("TEXT: " + text);
//                        Toast.makeText(root.getContext(), text, Toast.LENGTH_SHORT).show();
                        String[] words = text.split(" ");
                        Log.i("0", words[0]);
                        if (!words[0].equals("ASU-LSTU")) {
                            Toast.makeText(root.getContext(), "Неверный файл", Toast.LENGTH_SHORT).show();
                        } else {
                            inputTextDecrypt.setText(words[4]);
                            inputKeyDecrypt.setText(words[2]);
                        }
                    } catch (IOException ex) {

                        Toast.makeText(root.getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {

                        try {
                            if (fin != null)
                                fin.close();
                        } catch (IOException ex) {

                            Toast.makeText(root.getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }
        }
    }
}