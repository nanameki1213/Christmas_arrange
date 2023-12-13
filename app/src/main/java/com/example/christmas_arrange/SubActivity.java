package com.example.christmas_arrange;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SubActivity extends AppCompatActivity {

    private static final int PICK_FOLDER_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button selectFolderButton = findViewById(R.id.btnSave);
        selectFolderButton.setOnClickListener(view -> openFolderChooser());
    }

    private void openFolderChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_FOLDER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FOLDER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                // フォルダのパスを取得
                String folderPath = getFolderPath(treeUri);
                // 選択されたフォルダのパスを使用して処理を行う
            }
        }
    }

    private String getFolderPath(Uri treeUri) {
        String folderPath = null;
        if (DocumentsContract.isDocumentUri(this, treeUri)) {
            // ドキュメント URI からフォルダのパスを取得
            String documentId = DocumentsContract.getTreeDocumentId(treeUri);
            String[] parts = documentId.split(":");
            if (parts.length > 1) {
                String storageType = parts[0];
                String relativePath = parts[1];
                folderPath = "/" + storageType + "/" + relativePath;
            }
        }
        return folderPath;
    }
}