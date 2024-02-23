package org.ferreiratechlab.reasalva.Controller;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.ferreiratechlab.reasalva.DataBase.DatabaseHelper;
import org.ferreiratechlab.reasalva.Models.ClipboardItem;
import org.ferreiratechlab.reasalva.R;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<ClipboardItem> items = new ArrayList<>();
    private Context context;

    private CardView cardView;



    public void setItems(List<ClipboardItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        ClipboardItem item = items.get(position);
        holder.bind(item);
    }

    public ItemAdapter(CardView cardView) {
        this.cardView = cardView;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewItem;
        private ItemAdapter adapter;

        public ItemViewHolder(View itemView, ItemAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            textViewItem = itemView.findViewById(R.id.textViewItem);

            // Adiciona um OnClickListener para copiar o item para a área de transferência
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardItem item = adapter.items.get(getAdapterPosition());
                    ClipboardManager clipboardManager = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Item", item.getContent());
                    clipboardManager.setPrimaryClip(clip);
                    Toast.makeText(v.getContext(), "Item copiado para a área de transferência", Toast.LENGTH_SHORT).show();
                }
            });

            // Adiciona um OnLongClickListener para mostrar um diálogo de exclusão
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Excluir item")
                            .setMessage("Tem certeza de que deseja excluir este item?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    int position = getAdapterPosition();
                                    if (position != RecyclerView.NO_POSITION) {
                                        adapter.deleteItem(position);
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                    return true;
                }
            });
        }

        public void bind(ClipboardItem item) {
            textViewItem.setText(item.getContent());
        }
    }

    public void deleteItem(int position) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        try{
            ClipboardItem deletedItem = items.get(position);
            items.remove(position);
            notifyItemRemoved(position);
            long itemId = deletedItem.getId();

            databaseHelper.deleteItem(itemId);

            // Verifica se a lista está vazia após a exclusão do item
            if (items.isEmpty()) {
                // Se estiver vazia, mostra o CardView com o texto centralizado
                cardView.setVisibility(View.VISIBLE);
            }


        }catch (IndexOutOfBoundsException e){
            System.out.println(position);
        }

    }

    public List<ClipboardItem> getItems() {
        return items;
    }




}

