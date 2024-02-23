package org.ferreiratechlab.reasalva;

import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.ferreiratechlab.reasalva.Controller.ClipboardMonitorService;
import org.ferreiratechlab.reasalva.Controller.ItemAdapter;
import org.ferreiratechlab.reasalva.DataBase.DatabaseContract;
import org.ferreiratechlab.reasalva.DataBase.DatabaseHelper;
import org.ferreiratechlab.reasalva.Models.ClipboardItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ClipboardManager clipboardManager;
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private DatabaseHelper databaseHelper;
    private CardView cardView;
    private TextView infoTextView;
    private Toolbar toolbar;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private NavigationView navigationView;

    private boolean flag = false;
    // Defina os IDs de menu como constantes de classe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        cardView = findViewById(R.id.cardView);
        infoTextView = findViewById(R.id.infoTextView);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        databaseHelper = new DatabaseHelper(this);



       startService(new Intent(this, ClipboardMonitorService.class));




        FloatingActionButton fabClearHistory = findViewById(R.id.floatingActionButton);
        FloatingActionButton SycHistory = findViewById(R.id.floatingActionButton2);

        fabClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crie um diálogo de confirmação para perguntar ao usuário se deseja salvar o texto
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Deseja Limpar o histórico da área de transferencia?");
                builder.setPositiveButton("Limpar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Salve o texto no banco de dados
                        clearHistory();
                        Toast.makeText(MainActivity.this, "Histórico limpo", Toast.LENGTH_SHORT).show();
                        // Atualize a lista na RecyclerView
                        updateList();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                    }
                });
                builder.show();
                // Implemente a lógica para limpar o histórico do banco de dados

            }
        });
        SycHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verifiyHistory();
            }
        });


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(cardView);
        recyclerView.setAdapter(adapter);
        updateList();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                verifiyHistory();
                flag=true;
            }
        }, 1000);

        setSupportActionBar(toolbar);



        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Verifique qual item do menu foi selecionado

                int id = item.getItemId();
                if (id == R.id.menu_about){
                    showMessageDialog("Área Salva", "Desenvolvido por FerreiraTechLab");
                }
                else if (id == R.id.menu_settings) {
                    showMessageDialog("Alerta", "Evite inserir informações sensíveis, como senhas.");

                }else if(id == R.id.menu_instrucoes){
                    showMessageDialogIntrucoes();

                }else if(id == R.id.menu_exit){
                    finish();
                }

                // Feche a gaveta de navegação após selecionar um item do menu
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    private void showMessageDialogIntrucoes() {
        // Obtenha o ícone do seu recurso drawable
        Drawable iconSync = getResources().getDrawable(R.drawable.ic_popup_sync);
        Drawable iconTrash = getResources().getDrawable(R.drawable.baseline_auto_delete_24);

        TextView textView = new TextView(MainActivity.this);

        textView.setPadding(18, 18, 18, 18);
        textView.setTextSize(18);

        // Defina o tamanho máximo para os ícones
        int maxSize = (int) (textView.getLineHeight() *  1.2); // Ajuste o fator de escala conforme necessário

        // Redimensione os ícones para se encaixarem no tamanho máximo
        iconSync.setBounds(0, 0, maxSize, maxSize);
        iconTrash.setBounds(0, 0, maxSize, maxSize);



        // Crie um SpannableStringBuilder para adicionar o ícone ao texto
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append("1. Clique em ").append(" ", new ImageSpan(iconSync), 0).append(" para adicionar os textos\n\n")
                .append("2. Clique em ").append(" ", new ImageSpan(iconTrash), 0).append(" para apagar todos os texto\n\n")
                .append("3. Clique rápido em um item para copiá-lo.\n\n")
                .append("4. Clique e segure para excluir um item.");
        textView.setText(builder);

        // Adicione o texto ao builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Instruções");
        alertDialogBuilder.setView(textView);
        alertDialogBuilder.setPositiveButton("OK", null);
        // Crie o AlertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // Mostre o AlertDialog
        alertDialog.show();
    }


    // Método auxiliar para exibir um diálogo com uma mensagem
    private void showMessageDialog(String title, String message) {
        // Crie um AlertDialog.Builder com o título e a mensagem fornecidos
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        // Exiba o diálogo
        builder.show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        finish();

    }

    @Override
    protected void onResume(){
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!flag){
                    verifiyHistory();
                }else{
                    flag = false;
                }

            }
        }, 1000);

    }

    private void updateList() {
        // Obtém uma instância do banco de dados usando o DatabaseHelper
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Define as colunas que deseja recuperar da tabela
        String[] projection = {
                DatabaseContract.ItemEntry._ID,
                DatabaseContract.ItemEntry.COLUMN_NAME_CONTENT
        };

        // Realiza a consulta ao banco de dados
        Cursor cursor = db.query(
                DatabaseContract.ItemEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        // Cria uma lista para armazenar os itens do banco de dados
        List<ClipboardItem> itemList = new ArrayList<>();

        // Verifica se há itens na tabela
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Lê o conteúdo de cada item do banco de dados
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.ItemEntry._ID));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ItemEntry.COLUMN_NAME_CONTENT));

                // Cria um objeto ClipboardItem e adiciona à lista de itens
                ClipboardItem item = new ClipboardItem(id, content);
                itemList.add(item);
            } while (cursor.moveToNext());

            // Fecha o cursor após ler todos os itens
            cursor.close();
        }

        // Define os itens na lista do adaptador
        adapter.setItems(itemList);
        updateCardVisibility();
    }



    private void clearHistory() {
        // Obtém uma instância do banco de dados usando o DatabaseHelper
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Exclui todos os registros da tabela
        int rowsDeleted = db.delete(DatabaseContract.ItemEntry.TABLE_NAME, null, null);

        // Verifica se os registros foram excluídos com sucesso
        if (rowsDeleted > 0) {
            Toast.makeText(MainActivity.this, "Histórico limpo", Toast.LENGTH_SHORT).show();
            // Atualiza a lista após limpar o histórico
            updateList();
        } else {
            Toast.makeText(MainActivity.this, "Erro ao limpar o histórico", Toast.LENGTH_SHORT).show();
        }
    }
    private void startClipboardMonitorService() {
        Intent serviceIntent = new Intent(this, ClipboardMonitorService.class);
        startService(serviceIntent);
    }

    private void showSaveTextDialog(final CharSequence text) {
        // Crie um diálogo de confirmação para perguntar ao usuário se deseja salvar o texto
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Deseja salvar o texto da área de transferência?");
        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Salve o texto no banco de dados
                saveTextToDatabase(text.toString());
                // Atualize a lista na RecyclerView
                updateList();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // O usuário optou por não salvar o texto, não é necessário fazer nada
            }
        });
        builder.show();
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

    private void verifiyHistory(){
        // Verifica se há texto na área de transferência ao iniciar o aplicativo
        if ((clipboardManager.hasPrimaryClip())) {
            // Obtém o texto da área de transferência
            CharSequence text = clipboardManager.getPrimaryClip().getItemAt(0).getText();
            // Mostra um diálogo perguntando ao usuário se deseja salvar o texto
            showSaveTextDialog(text);
        }else{

        }

    }

    // Método para verificar e atualizar a visibilidade do CardView com base na lista de itens
    public void updateCardVisibility() {
        //List<ClipboardItem> itemList = adapter.getItems();
        if (adapter.getItems().isEmpty()) {
            // Se a lista estiver vazia, torna o CardView visível e define o texto apropriado
            cardView.setVisibility(View.VISIBLE);
            // Obtenha o ícone do seu recurso drawable
            Drawable icon = getResources().getDrawable(R.drawable.ic_popup_sync);

            // Defina o nível de intrínseco para o ícone (isso é importante para que ele apareça corretamente)
            int size = (int) (infoTextView.getLineHeight() * 1.2);
            icon.setBounds(0, 0, size, size);

            // Crie um SpannableStringBuilder para adicionar o ícone ao texto
            SpannableStringBuilder builder = new SpannableStringBuilder();

            builder.append("Clique em ").append(" ", new ImageSpan(icon), 0).append(" para adicionar os textos");


            // Defina o texto composto no TextView
            infoTextView.setText(builder);
        } else {
            // Se houver itens na lista, torna o CardView invisível
            cardView.setVisibility(View.GONE);
        }
    }
}
