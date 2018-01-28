package com.example.imgrecognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.os.Handler;
import java.io.IOException;
public class MainActivity extends Activity {

    public static Bitmap[] bitmaps;
    String names[] = {"AR","贵金属优惠券","圈圈","畅途网","手环","苹果","QQ","优酷"};
    byte[] data = new byte[100000];
    private void DownloadImage() throws IOException {
        String paths[] = {"http://123.57.35.196:9090/CJH/freela/resources/static/topic/comp/505276602/site_1513050111080.png",
                "http://123.57.35.196:9090/CJH/freela/resources/static/topic/comp/505276602/site_1513753759953.jpg",
                "http://123.57.35.196:9090/CJH/freela/resources/static/topic/comp/91643311/site_1504586068018.jpg",
                "http://123.57.35.196:9090/CJH/freela/resources/static/topic/comp/100136/site_1504586187714.JPG",
                "http://123.57.35.196:9090/CJH/freela/resources/static/topic/comp/505276602/site_1513753569434.jpg",
                "https://gss1.bdstatic.com/-vo3dSag_xI4khGkpoWK1HF6hhy/baike/c0%3Dbaike72%2C5%2C5%2C72%2C24/sign=de71d708dec451dae2fb04b9d7943903/730e0cf3d7ca7bcbcfe28879bd096b63f724a8cf.jpg",
                "https://gss3.bdstatic.com/-Po3dSag_xI4khGkpoWK1HF6hhy/baike/c0%3Dbaike92%2C5%2C5%2C92%2C30/sign=1aa9f99b2b3fb80e18dc698557b8444b/34fae6cd7b899e5114f3cfbe48a7d933c8950d1e.jpg",
                "https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/c0%3Dbaike180%2C5%2C5%2C180%2C60/sign=ea020a0a5a3d26973ade000f3492d99e/359b033b5bb5c9ea5f203423de39b6003bf3b31a.jpg"};
        bitmaps = new Bitmap[paths.length];
        byte[] data = new byte[50000];

        for (int i = 0; i < paths.length; i++) {
            data = HttpImgService.getImage(paths[i]);
            bitmaps[i] = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
    }

    private final static int IMG_RECOGNITION = 0;
    private ImageView iv;

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.iv);
        new Thread(networkTask).start();
        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            //startActivityForResult 的主要作用就是它可以回传数据，假设我们有两个页面，首先进入第一个页面，里面有一个按钮，
            // 用于进入下一个页面，当进入下一个页面时，进行设置操作，并在其finish()动作或者back动作后，将设置的值回传给第一个页面，
            // 从而第一个页面来显示所得到的值。这个有一点像回调方法，就是在第二个页面finish()动作或者back动作后，
            // 会回调第一个页面的onActivityResult()方法
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImgRecognitionActivity.class);
                startActivityForResult(intent, IMG_RECOGNITION);
            }
        });
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("mylog", "请求结果为-->" + val);
        }
    };
    Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            try {
                DownloadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", "请求结果");
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };
    // 回调方法，从第二个页面回来的时候会执行onActivityResult方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMG_RECOGNITION) {
            int data2 = data.getIntExtra("data", -1);
            // setImageResource是显示的resource（即res文件夹里的）图片，根据索引显示的，所以如果从网上下载的图片不保存到
            //res里的话，就不能利用setImageResource显示了
            Toast.makeText(this, "看到的是" + names[data2], Toast.LENGTH_LONG).show();
            iv.setImageBitmap(bitmaps[data2]);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
