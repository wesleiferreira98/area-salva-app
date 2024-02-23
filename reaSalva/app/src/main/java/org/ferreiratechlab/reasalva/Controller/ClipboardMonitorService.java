package org.ferreiratechlab.reasalva.Controller;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import org.ferreiratechlab.reasalva.DataBase.DatabaseContract;
import org.ferreiratechlab.reasalva.DataBase.DatabaseHelper;
import org.ferreiratechlab.reasalva.MainActivity;
import org.ferreiratechlab.reasalva.R;
import org.ferreiratechlab.reasalva.Views.Wecome;

import java.util.Objects;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class ClipboardMonitorService extends Service {

    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "ClipboardMonitorChannel";

    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener clipChangedListener;

    private DatabaseHelper databaseHelper;

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Implement your clipboard monitoring logic here
            //startClipboardMonitoring();

        }
    }



    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        showNotification();


        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        databaseHelper = new DatabaseHelper(this);
        Toast.makeText(ClipboardMonitorService.this,"Serviço iniciado", Toast.LENGTH_SHORT).show();
        Log.d("Inicio", "Método onCreate() chamado.");


        // Start a background thread to perform clipboard monitoring
        HandlerThread thread = new HandlerThread("ClipboardMonitorThread");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);



        // Send a message to the handler to start clipboard monitoring
        serviceHandler.sendEmptyMessage(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Saiuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
        clipboardManager.removePrimaryClipChangedListener(clipChangedListener);
        removeNotification();

        // Quit the service thread
        serviceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startClipboardMonitoring() {
        Log.d("gerenciamento", "Método startClipboardMonitoring() chamado.");
        clipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {

            @Override
            public void onPrimaryClipChanged() {
                String textCopied = Objects.requireNonNull(clipboardManager.getPrimaryClip()).getItemAt(0).getText().toString();
                Toast.makeText(ClipboardMonitorService.this, "Texto copiado: " + textCopied, Toast.LENGTH_SHORT).show();
                System.out.println("Texto copiado: " + textCopied);
                Log.d("onPrimaryClipChanged() ", "Método onPrimaryClipChanged()  chamado.");

                saveTextToDatabase(textCopied);
            }
        };
        clipboardManager.addPrimaryClipChangedListener(clipChangedListener);
    }

    private void saveTextToDatabase(String text) {
        // Implemente a lógica para salvar o texto no banco de dados usando o DatabaseHelper
        Log.d("saveTextToDatabase(String text) ", "Método saveTextToDatabase(String text)  chamado.");
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ItemEntry.COLUMN_NAME_CONTENT, text);
        long newRowId = db.insert(DatabaseContract.ItemEntry.TABLE_NAME, null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "Texto salvo no banco de dados", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao salvar texto no banco de dados", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void showNotification() {
        // Verifica se a permissão para exibir notificações foi concedida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    // Solicita ao usuário que conceda permissão para exibir notificações
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);

                    Toast.makeText(this, "Por favor, conceda permissão para exibir notificações", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        // Se a permissão já estiver concedida ou se estiver em uma versão anterior do Android, mostra a notificação normalmente
        Intent notificationIntent = new Intent(this, Wecome.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true);

        startForeground(NOTIFICATION_ID, builder.build());

        // Verifica se a permissão para postar notificações está concedida
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Posta a notificação
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
        } else {
            // Caso a permissão não esteja concedida, solicita a permissão ao usuário
            // Você pode implementar isso chamando ActivityCompat.requestPermissions() aqui

        }
        }






    private void removeNotification() {
        stopForeground(false);
    }
}



