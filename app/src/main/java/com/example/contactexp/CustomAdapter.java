package com.example.contactexp;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.provider.BaseColumns._ID;

/**
 *
 */
//public class CustomAdapter extends CursorRecyclerViewAdapter<CustomAdapter.ViewHolder> {
//
//    private Context context;
//
//    public CustomAdapter(Context context, Cursor cursor) {
//        super(context, cursor);
//        this.context = context;
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        public TextView mTextView;
//
//        public ViewHolder(View view) {
//            super(view);
//            mTextView = view.findViewById(R.id.title);
//        }
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.contacts_list_item, parent, false);
//        ViewHolder vh = new ViewHolder(itemView);
//
//        return vh;
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
//        viewHolder.mTextView.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
//        viewHolder.mTextView.setTag(cursor);
//        viewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Cursor curs = (Cursor) view.getTag();
//                String[] columnNames = curs.getColumnNames();
//                String message = "";
//
//                String contactID = curs.getString(curs.getColumnIndex(_ID));
//
//                for (String columnName : columnNames) {
//                    if (curs.getType(curs.getColumnIndex(columnName)) != FIELD_TYPE_NULL && curs.getType(curs.getColumnIndex(columnName)) != FIELD_TYPE_BLOB) {
//                        String value = curs.getString(curs.getColumnIndex(columnName));
//                        message += columnName + " = " + value + "\n";
//
//                    }
//                }
//                Log.e("Full Message:-", message);
//
//                ((MainActivity) context).displayRawContact(contactID);
//            }
//        });
//    }
//}


//public class CustomAdapter extends BaseAdapter {
//
//    private Cursor cursorGlobal;
//    private Context context;
//
//    public CustomAdapter(Context context, Cursor cursorGlobal) {
//        this.cursorGlobal = cursorGlobal;
//        this.context = context;
//    }
//
//    @Override
//    public int getCount() {
//        return cursorGlobal.getCount();
//    }
//
//    @Override
//    public Object getItem(int i) {
//        return null;
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return i;
//    }
//
//    @Override
//    public View getView(int i, View view, ViewGroup viewGroup) {
//
//        ViewHolder viewHolder;
//
//        if (view == null) {
//            viewHolder = new ViewHolder();
//            view = LayoutInflater.from(context).inflate(R.layout.contacts_list_item, viewGroup, false);
//            viewHolder.tvName = view.findViewById(R.id.title);
//
//            view.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) view.getTag();
//        }
//
//        cursorGlobal.moveToPosition(i);
//        viewHolder.tvName.setText(cursorGlobal.getString(cursorGlobal.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
////        viewHolder.tvName.setTag(i);
//        viewHolder.tvName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String[] columnNames = cursorGlobal.getColumnNames();
//                String message = "";
//
//                String contactID = cursorGlobal.getString(cursorGlobal.getColumnIndex(_ID));
//
//                for (String columnName : columnNames) {
//                    if (cursorGlobal.getType(cursorGlobal.getColumnIndex(columnName)) != FIELD_TYPE_NULL && cursorGlobal.getType(cursorGlobal.getColumnIndex(columnName)) != FIELD_TYPE_BLOB) {
//                        String value = cursorGlobal.getString(cursorGlobal.getColumnIndex(columnName));
//                        message += columnName + " = " + value + "\n";
//
//                    }
//                }
//                Log.e("Full Message:-", message);
//
//                ((MainActivity) context).displayRawContact(contactID);
//
//
//            }
//        });
//
//        return view;
//    }
//
//    class ViewHolder {
//        TextView tvName;
//    }
//
//}

public class CustomAdapter extends CursorAdapter {

    private Cursor cursorGlobal;
    private Context context;

    public CustomAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.cursorGlobal = cursor;
        this.context = context;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.contacts_list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.title);
        Button btnDetail = (Button) view.findViewById(R.id.detail);
        Button btnEdit = (Button) view.findViewById(R.id.edit);
        textView.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
        btnDetail.setTag(cursor.getPosition());
        btnEdit.setTag(cursor.getPosition());
        textView.setTag(cursor.getPosition());

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = (int) v.getTag();

                Cursor tagCursor = cursorGlobal;
                tagCursor.moveToPosition(pos);

                String[] columnNames = tagCursor.getColumnNames();
                String message = "";

                String contactID = tagCursor.getString(tagCursor.getColumnIndex(_ID));
//                String contactID = (String) v.getTag();

                for (String columnName : columnNames) {
                    if (tagCursor.getType(tagCursor.getColumnIndex(columnName)) != FIELD_TYPE_NULL && tagCursor.getType(tagCursor.getColumnIndex(columnName)) != FIELD_TYPE_BLOB) {
                        String value = tagCursor.getString(tagCursor.getColumnIndex(columnName));
                        message += columnName + " = " + value + "\n";

                    }
                }
                Log.e("Full Message:-", message);

//                ((MainActivity)context).getAllRawIdsWithAccountType(tagCursor);
                ((MainActivity)context).addK2MContactInfo(contactID);
//                ((MainActivity) context).joinIntoExistingContact(Long.parseLong(contactID), 5552l);
//                ((MainActivity)context).displayRawContact(contactID);
//                ((MainActivity)context).displayRawContact("5556");

            }
        });
        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = (int) v.getTag();

                Cursor tagCursor = cursorGlobal;
                tagCursor.moveToPosition(pos);

                String[] columnNames = tagCursor.getColumnNames();
                String message = "";

                String contactID = tagCursor.getString(tagCursor.getColumnIndex(_ID));
//                String contactID = (String) v.getTag();

                for (String columnName : columnNames) {
                    if (tagCursor.getType(tagCursor.getColumnIndex(columnName)) != FIELD_TYPE_NULL && tagCursor.getType(tagCursor.getColumnIndex(columnName)) != FIELD_TYPE_BLOB) {
                        String value = tagCursor.getString(tagCursor.getColumnIndex(columnName));
                        message += columnName + " = " + value + "\n";

                    }
                }
                Log.e("Full Message:-", message);

//                ((MainActivity)context).getAllRawIdsWithAccountType(tagCursor);
//                ((MainActivity)context).addK2MContactInfo(contactID);
//                ((MainActivity) context).joinIntoExistingContact(Long.parseLong(contactID), 5552l);
                ((MainActivity)context).displayRawContact(contactID);
//                ((MainActivity)context).displayRawContact("5556");

            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = (int) v.getTag();

                Cursor tagCursor = cursorGlobal;
                tagCursor.moveToPosition(pos);

                String[] columnNames = tagCursor.getColumnNames();
                String message = "";

                String contactID = tagCursor.getString(tagCursor.getColumnIndex(_ID));
                String lookup = tagCursor.getString(tagCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
//                String contactID = (String) v.getTag();

//                for (String columnName : columnNames) {
//                    if (tagCursor.getType(tagCursor.getColumnIndex(columnName)) != FIELD_TYPE_NULL && tagCursor.getType(tagCursor.getColumnIndex(columnName)) != FIELD_TYPE_BLOB) {
//                        String value = tagCursor.getString(tagCursor.getColumnIndex(columnName));
//                        message += columnName + " = " + value + "\n";
//
//                    }
//                }
//                Log.e("Full Message:-", message);

//                ((MainActivity)context).openForEdit(contactID,lookup);
                ((MainActivity)context).addK2MContactInfo(contactID);
            }
        });
    }


}
