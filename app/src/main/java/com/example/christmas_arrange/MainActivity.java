package com.example.christmas_arrange;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FOLDER_REQUEST_CODE = 2;

    Button btnSave;
    Button btnPlay;

    @Override

    protected void onCreate(Bundle instanceState1) {
        super.onCreate(instanceState1);
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

        // 保存先のファイルパスの入力を促すダイアログ

        // ダイアログを表示する
        new AlertDialog.Builder(this)
                .setTitle("Christmas Arrange App")
                .setMessage("保存先のパスを設定してください")
                .setPositiveButton("OK", (dialog, which) -> {
                    // OKボタン押下時に実行したい処理を記述
                })
                .setNegativeButton("キャンセル", (dialog, which) -> {
                    // キャンセルボタン押下時に実行したい処理を記述
                })
                .create()
                .show();

    }
    public void onPlayButtonClick(View view) {

    }

}