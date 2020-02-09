package com.wei.notify01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Switch maxPrioritySwitch;
    private NotificationManager manager;

    // 通知頻道分類代碼
    private String channelIdBasic = "com.wei.notify01.notify.channel.basic";
    private String channelIdOther = "com.wei.notify01.notify.channel.other";

    private Bitmap bigPicture;

    // 通知物件編號
    private static int downloadId = 10;
    private static final int BASIC_ID = 0;
    private static final int BIG_PICTURE_ID = 1;
    private static final int BIG_TEXT_ID = 2;
    private static final int INBOX_ID =3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processViews();

        // 取得 NotificationManager 物件
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        // 建立與設定 Notification Channel
        createChannel(channelIdBasic, "Basic", "Basic notify channel");
        createChannel(channelIdOther, "Other", "Other notify channel");
    }

    // <summary>
    //  建立與設定 Notify channel
    //  加入裝置版本判斷
    //  設定 channel 不支援 API 26 以下的版本
    // </summary>
    // <param name='id'> channel ID </summary>
    // <param name='name'> channel name </summary>
    // <param name='description'> channel description </param>
    private void createChannel(String id, CharSequence name, String description) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        // 建立 channel 物件，參數依序為 channel 代碼，名稱、等級
        NotificationChannel channel = new NotificationChannel(id, name,
                NotificationManager.IMPORTANCE_DEFAULT);
        // 設定 channel 的說明
        channel.setDescription(description);
        // 設定 channel 物件給 NotificationManager
        manager.createNotificationChannel(channel);
    }

    // <summary> 視覺元件初始化 </summary>
    private void processViews() {
        maxPrioritySwitch = findViewById(R.id.maxPrioritySwitch);
        // 建立大型圖片用的 Bitmap 物件
        bigPicture = BitmapFactory.decodeResource(getResources(),
                R.drawable.notify_big_picture);
    }

    public void basicSend(View view) {
        // 建立 Notification Build 物件
        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(this, channelIdBasic);

        // 設定圖示、時間、標題、訊息、額外資訊
        builder.setSmallIcon(R.drawable.ic_android_white_48dp)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Basic Notification")
                .setContentText("Demo for basic notification Hello world")
                .setContentInfo("1")
                .setColor(Color.BLUE);

        // 設定分類目錄
        setCategory(builder, "recommendation");

        if (maxPrioritySwitch.isChecked()) {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
            builder.setVibrate(new long[0]);
        }

        // 建立通知物件
        Notification notification = builder.build();
        // 發出通知
        manager.notify(BASIC_ID, notification);
        Toast.makeText(this, "Please check notification status",
                Toast.LENGTH_SHORT).show();
    }

    // <summary>
    //  設定通知分類目錄
    //  不支援 API level 21 以下的版本
    //  加入版本判斷
    // </summary>
    // <param name='builder'> notification builder </param>
    // <param name='category'> notification category </param>
    private void setCategory(NotificationCompat.Builder builder, String category) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.setCategory(category);
    }

    public void basicCancel(View view) {
        manager.cancel(BASIC_ID);
        Toast.makeText(this, "Notification is cleared", Toast.LENGTH_LONG).show();
    }

    // <summary> </summary>
    public void setPreference(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);

        switch (view.getId()) {
            case R.id.btnBasicPref:
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelIdBasic);
                break;
            case R.id.btnOtherPreference:
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelIdOther);
                break;
        }

        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    public void progressSend(View view) {
        // 建立 Notification Builder 物件
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, channelIdOther);

        // 設定 Builder 細節
        builder.setSmallIcon(R.drawable.ic_file_download_white_48dp)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Download picture" + downloadId + ".jpg")
                .setContentText("Download in progress......")
                .setColor(Color.BLUE);

        new Thread() {
            // 設定編號
            private int id = downloadId++;

            @Override
            public void run() {
                int incr;

                for (incr = 0; incr<= 10; incr++) {

                    // 設定進度
                    // 參數最大值、且前進度、是否顯示進度
                    builder.setProgress(10, incr, false);

                    // 更新進度、呼叫 manager 物件的 notify(id, notification) 進行更新
                    manager.notify(id, builder.build());

                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 設定下載完成通知內容
                builder.setContentText("Download complete!")
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                // 用同一 id 更新通知
                manager.notify(id, builder.build());
            }
        }.start();
    }

    public void bigPictureSend(View view) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, channelIdOther);

        // 設定圖示、時間、標題、顏色
        builder.setSmallIcon(R.drawable.ic_insert_photo_white_48dp)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Big picture notification")
                .setColor(Color.BLUE);

        // 建立大型圖片樣式物件
        NotificationCompat.BigPictureStyle bigPictureStyle =
                new NotificationCompat.BigPictureStyle();
        // 設定圖片與介紹
        bigPictureStyle.bigPicture(bigPicture)
                .setSummaryText("The Flower");
        builder.setStyle(bigPictureStyle);

        manager.notify(BIG_PICTURE_ID, builder.build());
        Toast.makeText(this, "Picture Notification", Toast.LENGTH_SHORT).show();
    }

    public void bigTextSend(View view) {
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelIdOther);

        builder.setSmallIcon(R.drawable.ic_description_white_48dp)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Bit Text Notification")
                .setColor(Color.BLUE);

        // 建立大型文字樣式物件
        NotificationCompat.BigTextStyle bigTextStyle
                = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(getString(R.string.big_text))
                .setSummaryText("About Notification");

        builder.setStyle(bigTextStyle);
        manager.notify(BIG_TEXT_ID, builder.build());
    }

    public void inboxSend(View view) {
        final NotificationCompat.Builder builder
                = new NotificationCompat.Builder(this, channelIdOther);

        builder.setSmallIcon(R.drawable.ic_view_list_white_48dp)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Inbox Notification")
                .setColor(Color.BLUE);

        // 建立列表樣式
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        // 加入三行列表
        inboxStyle.addLine("You got a message from Ali");
        inboxStyle.addLine("You got a message from Lili");
        inboxStyle.addLine("You got a message from Tam");

        // 設定列表摘要
        inboxStyle.setSummaryText("Total 3 messages");
        // 設定列表樣式
        builder.setStyle(inboxStyle);

        manager.notify(INBOX_ID, builder.build());
        Toast.makeText(this, "Inbox Notification", Toast.LENGTH_SHORT).show();
    }
}
