package com.example.christmas_arrange;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FOLDER_REQUEST_CODE = 2;

    Button btnSave;
    Button btnPlay;
    MediaPlayer mp;
    String filePath;
    byte[] generated_mpeg;
    private static final int PICK_FILE_REQUEST_CODE = 1;

    @Override

    protected void onCreate(Bundle instanceState1) {
        super.onCreate(instanceState1);
        setContentView(R.layout.activity_main);

        btnSave = findViewById(R.id.btnSave);
        btnPlay = findViewById(R.id.btnPlay);
        mp = null;

    }

    // 追加ボタン
    public void onGenerateButtonClick(View view) {
        OutputStream outputStream;
        // 入力ダイアログから得たファイルパスからmp3データを取り出し，ボディとして付けてサーバにPOSTする．
        showToast("filepath: " + filePath);
        byte[] data = null;
        try {
            showToast("File input stream");

            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            data = new byte[(int)file.length()];
            fileInputStream.read(data);

            showToast("Open file: " + filePath);
            fileInputStream.close();
        } catch (IOException e) {
            showToast(e.getMessage());
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        try {
            URL url = new URL("http://localhost:8000");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");

            con.setDoInput(true);
            con.setDoOutput(true);

            con.connect();
            showToast("start Connection");

            outputStream = con.getOutputStream();
            outputStream.write(data);

            int statusCode = con.getResponseCode();

            if(statusCode == 200) {
                showToast("OK");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // その後定期的にGETをして処理の進み具合を取得し，処理完了してサーバからアレンジ済みのmp3データを受け取る．

        // mp3を受け取ったら再生と保存ボタンを有効にする．
        btnPlay.setEnabled(true);
        btnSave.setEnabled(true);
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

    }

    public void onPlayButtonClick(View view) {
        if (mp == null) {
            mp = MediaPlayer.create(this, R.raw.music_name);
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
                String id = originalId;
                if (split.length > 1) {
                    id = split[1];
                }

                try {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            Long.parseLong(id)
                    );
                    return getDataColumn(this, contentUri, null, null);
                } catch (NumberFormatException e) {
                    showToast("Invalid ID format");
                    return null;
                }
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

        showToast("return Null");

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
