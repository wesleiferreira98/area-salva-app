package org.ferreiratechlab.reasalva.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Clipboard.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    // Método para excluir um item do banco de dados com base no seu ID
    public void deleteItem(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Escreva a instrução SQL para excluir o item da tabela com base no ID
        String selection = DatabaseContract.ItemEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };
        // Exclua o item da tabela
        db.delete(DatabaseContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
        // Feche o banco de dados
        db.close();
    }
}
