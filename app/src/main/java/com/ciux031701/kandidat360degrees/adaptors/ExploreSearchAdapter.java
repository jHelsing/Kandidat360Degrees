package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by boking on 2017-03-21.
 */

public class ExploreSearchAdapter extends BaseAdapter {

    public List<String> resultList;
    public Context context;
    ArrayList<String> arraylist;
    public LayoutInflater inflater;

    public ExploreSearchAdapter(List<String> apps, Context context) {
        super();
        this.resultList = apps;
        this.context = context;
        arraylist = new ArrayList<String>();
        arraylist.addAll(resultList);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear(){
        resultList.clear();
        arraylist.clear();
    }

    public String getItemWithoutCountry(int i){
        String fullText = (String)getItem(i);
        String[] parts = fullText.split("\\,");
        return parts[0];
    }

    public void insert(String s){
        resultList.add(s);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int i) {
        return resultList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    static class SearchHolder{
        TextView searchText;
    }

    @Override
    public View getView(int position, View convertView,ViewGroup parent) {
        SearchHolder searchHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.map_search_item, parent, false);
            searchHolder = new SearchHolder();
            searchHolder.searchText = (TextView) convertView.findViewById(R.id.row_result_text);
            convertView.setTag(searchHolder);
        } else {
            searchHolder = (SearchHolder) convertView.getTag();
        }

        searchHolder.searchText.setText(resultList.get(position) + "");
        return convertView;
    }

    //We should use this if we decide to temporarily store results which would make it look faster
    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());

        resultList.clear();
        if (charText.length() == 0) {
            resultList.addAll(arraylist);

        } else {
            for (String text : arraylist) {
                if (charText.length() != 0 && text.toLowerCase(Locale.getDefault()).contains(charText)) {
                    resultList.add(text);
                }
            }
        }
        notifyDataSetChanged();
    }
}
