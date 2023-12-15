package com.example.christmas_arrange;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaParser;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnSave;
    Button btnPlay;

    private static final int PICK_FILE_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSave = findViewById(R.id.btnSave);
        btnPlay = findViewById(R.id.btnPlay);

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
        MediaPlayer mp = MediaPlayer.create(this,R.raw.music_name);
        if(!mp.isPlaying()){
            mp.start();
            btnPlay.setText("停止");
        }else{
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
    public void onSelectButtonClick() {
        // ファイルを選択するためのIntentを作成
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*"); // MP3ファイルを選択できるように指定

        // ファイル選択のためのアクティビティを開始
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // ユーザーがファイルを選択した場合
            if (data != null) {
                Uri selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    String filePath = selectedFileUri.getPath();
                    // filePathを利用して必要な処理を実行
                    Toast.makeText(this, "選択されたファイルのパス: " + filePath, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}