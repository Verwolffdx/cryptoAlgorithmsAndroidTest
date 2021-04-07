package com.datwhite.cryptoalgorithmsandroidtest.ui.encrypt;

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


public class EncryptFragment extends Fragment {

    private static final Charset ISO_CHARSET = Charset.forName("ISO_8859_1");

    private EncryptViewModel encryptViewModel;

    private EditText inputTextEncrypt;
    private EditText inputKeyEncrypt;
    private EditText outputTextEncrypt;
    private Button encryptBtn;
    private Button copyBtnEnc;
    private Button saveFileBtn;
    private Button openFileBtnEnc;
    private RadioButton inputKeyBtn;
    private RadioButton generateKeyBtn;
    private RadioButton desBtn;
    private RadioButton vigenereBtn;

    private int mode = 0; //0 - DES, 1 - Виженер

    private View root;

    ClipboardManager clipboardManager;
    ClipData clipData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        encryptViewModel =
                new ViewModelProvider(this).get(EncryptViewModel.class);
        root = inflater.inflate(R.layout.fragment_encrypt, container, false);

        encryptBtn = root.findViewById(R.id.encryptBtn);
        copyBtnEnc = root.findViewById(R.id.copyBtnEnc);

        desBtn = root.findViewById(R.id.desBtn);
        vigenereBtn = root.findViewById(R.id.vigenereBtn);

        openFileBtnEnc = root.findViewById(R.id.openFileBtnEnc);
        saveFileBtn = root.findViewById(R.id.saveFileBtn);

        inputKeyBtn = root.findViewById(R.id.inputKeyBtn);
        generateKeyBtn = root.findViewById(R.id.generateKeyBtn);

        inputTextEncrypt = root.findViewById(R.id.inputTextEncrypt);
        inputKeyEncrypt = root.findViewById(R.id.inputKeyEncrypt);
        outputTextEncrypt = root.findViewById(R.id.outputTextEncrypt);

        //Кнопка "Ввести ключ"
        inputKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputKeyEncrypt.setActivated(true);
                inputKeyEncrypt.setClickable(true);
                inputKeyEncrypt.setCursorVisible(true);
                inputKeyEncrypt.setFocusable(true);
                inputKeyEncrypt.setFocusableInTouchMode(true);
//                inputKeyEncrypt.setEnabled(true);

            }
        });

        //Кнопка "Сгенерировать ключ"
        generateKeyBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                inputKeyEncrypt.setClickable(false);
                inputKeyEncrypt.setCursorVisible(false);
                inputKeyEncrypt.setFocusable(false);
                inputKeyEncrypt.setFocusableInTouchMode(false);
//                inputKeyEncrypt.setEnabled(false);

                try {
                    String k = GenerateKey.generate(8);
                    inputKeyEncrypt.setText(k);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });

        //Кнопка "Зашифровать"
        encryptBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String text = inputTextEncrypt.getText().toString();
                String key = inputKeyEncrypt.getText().toString();

                if (text.length() == 0 || key.length() == 0) {
                    Toast.makeText(root.getContext(), "Сначала ведите текст и ключ", Toast.LENGTH_LONG).show();
                }

                try {
                    if (mode == 0) {
                        if (key.length() != 8) {
                            Toast.makeText(root.getContext(), "Ключ должен быть длиной 8 символов", Toast.LENGTH_LONG).show();
                        } else {
                            byte[] enc = DES.encrypt(text.getBytes(), key.getBytes());
                            String encodedString = Base64.getEncoder().encodeToString(enc);

                            GenerateKey.setKey(key);
                            outputTextEncrypt.setText(encodedString);
                        }
                    } else if (mode == 1) {
                        String enc = Vigenere.encrypt(text, key);

                        GenerateKey.setKey(key);
                        outputTextEncrypt.setText(enc);
                    }
                } catch (Exception e) {
                    Toast toast = Toast.makeText(root.getContext(), "Дешифровка невозможна ", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 200);
                    toast.show();
                }

            }
        });

        //Кнопка "Сохранить"
        saveFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputKeyEncrypt.length() == 0 || outputTextEncrypt.length() == 0) {
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
                    File file = new File(path, "encrypted.txt");

                    try {
                        path.mkdirs();

                        OutputStream os = new FileOutputStream(file);
                        String start = "ASU-LSTU";
                        String key = " KEY " + inputKeyEncrypt.getText().toString();
                        String enc = " CRYPT " + outputTextEncrypt.getText().toString();
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
        openFileBtnEnc.setOnClickListener(new View.OnClickListener() {
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


        //Копировать
        ClipboardManager clipboardManager = (ClipboardManager) root.getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        copyBtnEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clipData = ClipData.newPlainText("text", outputTextEncrypt.getText().toString());
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
                inputKeyEncrypt.setActivated(true);
                inputKeyEncrypt.setClickable(true);
                inputKeyEncrypt.setCursorVisible(true);
                inputKeyEncrypt.setFocusable(true);
                inputKeyEncrypt.setFocusableInTouchMode(true);
//                inputKeyEncrypt.setEnabled(true);
                mode = 0;
            }
        });

        //Кнопка режим "Виженер"
        vigenereBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                inputKeyEncrypt.setClickable(false);
                inputKeyEncrypt.setCursorVisible(false);
                inputKeyEncrypt.setFocusable(false);
                inputKeyEncrypt.setFocusableInTouchMode(false);
//                inputKeyEncrypt.setEnabled(false);
                mode = 1;
            }
        });

        return root;
    }

    private File getExternalPath(String FILE_NAME) {
        return new File(getActivity().getExternalFilesDir(null), FILE_NAME);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    Uri chosenImageUri = data.getData();

//                    chosenImageUri.getEncodedPath();

                    Cursor cursor = getActivity().getContentResolver().query(chosenImageUri, null, null, null, null);
                    cursor.moveToFirst();
                    String filePath = cursor.getString(0);
                    cursor.close();

                    System.out.println("PATH: " + filePath);

                    inputTextEncrypt = root.findViewById(R.id.inputTextEncrypt);

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
                            inputTextEncrypt.setText(words[4]);
                            inputKeyEncrypt.setText(words[2]);
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