package com.example.christmas_arrange;

import android.content.Context;
import android.content.ContentUris;
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

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btnSave;
    Button btnPlay;
    MediaPlayer mp;
    private static final int PICK_FILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSave = findViewById(R.id.btnSave);
        btnPlay = findViewById(R.id.btnPlay);
        mp = null;

    }

    // 追加ボタン
    public void onGenerateButtonClick(View view) {
        // 入力ダイアログから得たファイルパスからmp3データを取り出し，ボディとして付けてサーバにPOSTする．
        // その後定期的にGETをして処理の進み具合を取得し，処理完了してサーバからアレンジ済みのmp3データを受け取る．

        // mp3を受け取ったら再生と保存ボタンを有効にする．
        btnPlay.setEnabled(true);
        btnSave.setEnabled(true);
    }

    // 保存ボタン
    public void onSaveButtonClick(View view) {
        // 保存先のファイルパスの入力を促すダイアログ
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
            String filePath = getFilePathFromUri(selectedFileUri);

            // 取得したファイルパスがnullでない場合、mp3ファイルであるかを確認
            if (filePath != null && filePath.toLowerCase().endsWith(".mp3")) {
                // mp3ファイルの場合、ファイルパスを表示
                showToast("Selected MP3 File: " + filePath);
            } else {
                showToast("Please select an MP3 file.");
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
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = Uri.parse("content://downloads/public_downloads/" + id);
                return getDataColumn(this, contentUri, null, null);
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
