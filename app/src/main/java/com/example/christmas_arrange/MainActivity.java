package com.example.christmas_arrange;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FOLDER_REQUEST_CODE = 2;
    private static final int PERMISSION_WRITE_EX_STR = 1;

    private Handler handler = new Handler();
    Button btnSave;
    Button btnPlay;
    MediaPlayer mp;
    String filePath;
    Uri file_uri;
    String url_str;
    byte[] original_mpeg;
    byte[] generated_mpeg;
    private static final int PICK_FILE_REQUEST_CODE = 1;

    @Override

    protected void onCreate(Bundle instanceState1) {
        super.onCreate(instanceState1);
        setContentView(R.layout.activity_main);

        btnSave = findViewById(R.id.btnSave);
        btnPlay = findViewById(R.id.btnPlay);
        mp = null;

        original_mpeg = null;

        url_str = "http://10.0.2.2:8000/";

        if(Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS
                },PERMISSION_WRITE_EX_STR);
            }
        }

    }

    // 追加ボタン
    public void onGenerateButtonClick(View view) {
        try {
            InputStream is = getContentResolver().openInputStream(file_uri);
            original_mpeg = new byte[is.available()];
            String readBytes = String.format(Locale.US, "read bytes = %d", is.read(original_mpeg));
            Log.e(TAG, readBytes);
            is.close();
        } catch (IOException e) {
            showToast(e.getMessage());
            e.printStackTrace();
        }
        showToast("opened file");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int statusCode = postAPI();
                String readBytes = String.format(Locale.US, "status code = %d", statusCode);
                Log.e(TAG, readBytes);
            }
        });
        thread.start();

        // その後定期的にGETをして処理の進み具合を取得し，処理完了してサーバからアレンジ済みのmp3データを受け取る．

        // mp3を受け取ったら再生と保存ボタンを有効にする．
        if(generated_mpeg != null) {
            btnPlay.setEnabled(true);
            btnPlay.setText("再生");
            mp = null;
            btnSave.setEnabled(true);
        }
    }

    public int postAPI() {
        HttpURLConnection con = null;
        OutputStream outputStream = null;
        int statusCode = 0;
        try {
            URL url = new URL(url_str);
            con = (HttpURLConnection) url.openConnection();

            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.addRequestProperty("User-Agent", "Android");
            con.addRequestProperty("Accept-Language", Locale.getDefault().toString());
            con.addRequestProperty("Content-Type", "audio/mpeg");
            con.setRequestMethod("POST");

            con.setDoInput(true);
            con.setDoOutput(true);

            con.connect();

            outputStream = con.getOutputStream();
            outputStream.write(original_mpeg);

            statusCode = con.getResponseCode();

            if (statusCode == 200) {
                InputStream is = con.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;

                try {
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }

                    // InputStreamとByteArrayOutputStreamを閉じる
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                        baos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                generated_mpeg = baos.toByteArray();

                String readBytes = String.format(Locale.US, "read bytes = %d", generated_mpeg.length);
                Log.e(TAG, readBytes);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return statusCode;
    }

    // 保存ボタン
    public void onSaveButtonClick(View view) {
        // 保存先のファイルパスの入力を促すダイアログ

        // 保存先のファイルパスの入力を促すダイアログ

        // ダイアログを表示する
        new AlertDialog.Builder(this)
                .setTitle("Christmas Arrange App")
                .setMessage("保存先のパスを設定してください")
                .setPositiveButton("OK", (dialog, which) -> {
                    // OKボタン押下時に実行したい処理を記述
                    openFolderChooser();
                })
                .setNegativeButton("キャンセル", (dialog, which) -> {
                    // キャンセルボタン押下時に実行したい処理を記述
                })
                .create()
                .show();

        mp.release();

    }

    public void onPlayButtonClick(View view) throws IOException {
        if (mp == null) {
            Context context = view.getContext();
            Path path = null;
            try {
                path = Files.createTempFile("temp", ".mp3");
                OutputStream out = Files.newOutputStream(path);
                out.write(generated_mpeg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Files.deleteIfExists(path);
            }
            mp = new MediaPlayer();
            try {
                showToast(path.toString());
                mp.setDataSource(path.toString());
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!mp.isPlaying()) {
            mp.start();
            btnPlay.setText("停止");
        } else {
            try {
                //再生を停止
                mp.stop();
                mp.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            btnPlay.setText("再生");
        }

    }

    public void onSelectButtonClick(View view) {
        // ファイル選択用のIntentを作成
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/mpeg"); // mp3ファイルを対象とする
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // ファイル選択ダイアログを表示
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    // ファイル選択ダイアログで選択された結果を受け取る
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // 選択されたファイルのURIを取得
            Uri selectedFileUri = data.getData();

            // URIからファイルパスを取得
            filePath = getFilePathFromUri(selectedFileUri);
            file_uri = selectedFileUri;

            // 取得したファイルパスがnullでない場合、mp3ファイルであるかを確認
            if (filePath != null && filePath.toLowerCase().endsWith(".mp3")) {
                // mp3ファイルの場合、ファイルパスを表示
                showToast("Selected MP3 File: " + filePath);
            } else {
                showToast("Please select an MP3 file.");
            }
        } else if (requestCode == PICK_FOLDER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                String selectedFolderPath = getDocumentPath(treeUri);
                handleSelectedFolder(selectedFolderPath);
            }
        }
    }

    private String getFilePathFromUri(Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
            // DocumentProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String originalId;
                try {
                    originalId = DocumentsContract.getDocumentId(uri);
                } catch (IllegalArgumentException e) {
                    showToast("Invalid URI");
                    return null;
                }

                String[] split = originalId.split(":");
                int splitIndex = split.length - 1;
                String id = split[splitIndex];
                showToast(id);

                return id;
//
//                // 文字列からハッシュを生成
//                String originalString = id;
//                int hashCode = originalString.hashCode();
//
//                // hashCodeが負の場合、正の数に変換
//                long uniqueId = (long) hashCode & 0xffffffffL;

//                try {
//                    final Uri contentUri = ContentUris.withAppendedId(
//                            Uri.parse("content://downloads/public_downloads"),
//                            uniqueId
//                    );
//                    return getDataColumn(this, contentUri, null, null);
//                } catch (NumberFormatException e) {
//                    showToast("Invalid ID format");
//                    return null;
//                }
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(this, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // MediaStore (and general)
            return getDataColumn(this, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File
            return uri.getPath();
        }

        return null;
    }
    private void openFolderChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_FOLDER_REQUEST_CODE);
    }

    private String getDocumentPath(Uri treeUri) {
        String documentId = DocumentsContract.getTreeDocumentId(treeUri);
        String[] split = documentId.split(":");
        if (split.length >= 2) {
            String type = split[0];
            if ("primary".equalsIgnoreCase(type)) {
                return "/" + split[1];
            }
        }
        return null;
    }

    private void handleSelectedFolder(String folderPath) {
        // 選択されたフォルダのパスを使用して処理を行う
        // 例: パスをログに表示
        System.out.println("Selected Folder Path: " + folderPath);
    }

    // ファイルパスを取得するヘルパーメソッド
    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                showToast("get String from Cursor Object");
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    // DocumentProviderが外部ストレージのドキュメントか確認するヘルパーメソッド
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    // DocumentProviderがダウンロードのドキュメントか確認するヘルパーメソッド
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    // DocumentProviderがメディアのドキュメントか確認するヘルパーメソッド
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    // トーストメッセージを表示するヘルパーメソッド
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
