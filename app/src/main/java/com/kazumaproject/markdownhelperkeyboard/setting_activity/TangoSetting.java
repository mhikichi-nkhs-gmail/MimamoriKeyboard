package com.kazumaproject.markdownhelperkeyboard.setting_activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kazumaproject.markdownhelperkeyboard.R;

public class TangoSetting extends AppCompatActivity {

    private MyOpenHelper2 helper;

    String kbn = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add2);


        //DB作成
        helper = new MyOpenHelper2(getApplicationContext());

        //データを受け取る
        Intent intent = getIntent();
        String KBN = intent.getStringExtra("KBN");

        Button button = findViewById(R.id.toAddActivity2);
        View view = findViewById(R.id.tvName);

        if(KBN.length() != 0) {
            //参照
            kbn = KBN;

            System.out.println("あいうえお");

            //既存データ参照
            readDate(KBN);

        }else{
            //新規登録
            kbn = "はい";

            //ボタンテキスト変更
            button.setText("はい");



        }
    }

    //データを参照する
    public void readDate(String read)
    {
        SQLiteDatabase db = helper.getReadableDatabase();

        EditText hiragana = findViewById(R.id.editText);
        EditText katakana = findViewById(R.id.editText2);
        EditText kanji = findViewById(R.id.editText3);

        Cursor cursor = db.query(
                "tango" ,
                new String[]{"hiragana","katakana","kanji"},
                "_ID = ?",
                new String[]{read},
                null,null,null


        );
        cursor.moveToFirst();

        for(int i = 0;i < cursor.getCount(); i++){
            hiragana.setText(cursor.getString(0));
            katakana.setText(cursor.getString(1));
            kanji.setText(cursor.getString(2));


        }

        cursor.close();

    }

    //データを保存する
    public void saveData(View view) {
        SQLiteDatabase db = helper.getWritableDatabase();

        EditText saveHiragana = findViewById(R.id.et1);
        EditText saveKatakana = findViewById(R.id.editTextText8);
        EditText saveKanji = findViewById(R.id.aaa);

        String sHiragana = saveHiragana.getText().toString();
        String sKatakana = saveKatakana.getText().toString();
        String sKanji = saveKanji.getText().toString();

        ContentValues values = new ContentValues();
        values.put("hiragana",sHiragana);
        values.put("katakana",sKatakana);
        values.put("kanji",sKanji);

        //ボタンが登録の場合
        if(kbn=="はい"){
            if(sHiragana.length()!=0) {

                db.insert("tango",null,values);

                System.out.println("単語が登録されました。");
            }else{

                System.out.println("戻るが押されました。");
            }
            //ボタンが更新の場合
        }else{
            if(sHiragana.length() !=0) {
                //更新
                UPDate(kbn);
                //トースト表示
                System.out.println("更新されました。");
            }else{
                //トースト表示
                System.out.println("更新されませんでした。");

            }
        }

    }

    //データ更新
    public void UPDate(String read){
        SQLiteDatabase db = helper.getReadableDatabase();

        EditText updataHiragana = findViewById(R.id.et1);
        EditText updataKatakana = findViewById(R.id.editTextText8);
        EditText updataKanji = findViewById(R.id.aaa);

        String uHiragana = updataHiragana.getText().toString();
        String uKatakana = updataKatakana.getText().toString();
        String uKanji = updataKanji.getText().toString();

        ContentValues upvalue = new ContentValues();
        upvalue.put("hiragana",uHiragana);
        upvalue.put("katakana",uKatakana);
        upvalue.put("kanji",uKanji);

        db.update("tango",upvalue,"_id=?",new String[]{read});

    }


    public void onClose(View view) {
        finish(); //画面を閉じる
    }

}
