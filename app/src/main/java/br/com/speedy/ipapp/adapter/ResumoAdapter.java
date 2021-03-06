package br.com.speedy.ipapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import br.com.speedy.ipapp.R;
import br.com.speedy.ipapp.util.ItemResumo;
import br.com.speedy.ipapp.util.SessionApp;

/**
 * Created by Henrique Araújo on 2015-03-06.
 */
public class ResumoAdapter extends BaseExpandableListAdapter implements SectionIndexer {

    private ItemResumo itemResumo;

    private List<ItemResumo> itensResumo;

    private Context context;

    private String[] sections;// = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "w", "x", "y", "z"};

    HashMap<String, Integer> mapIndex;

    public ResumoAdapter(Context context, List<ItemResumo> itensResumo){
        this.context = context;
        this.itensResumo = itensResumo;

        mapIndex = new LinkedHashMap<String, Integer>();

        for (int i = 0; i < itensResumo.size(); i++){
            String nomePeixe = itensResumo.get(i).getPeixe().getDescricao();
            String l = nomePeixe.substring(0, 1);
            l = l.toUpperCase();

            mapIndex.put(l, i);
        }

        Set<String> sectionLetters = mapIndex.keySet();

        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);
    }

    @Override
    public int getGroupCount() {

        return itensResumo.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itensResumo.get(groupPosition).getLotes().size();
    }

    @Override
    public Object getGroup(int groupPosition) {

        return itensResumo.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itensResumo.get(groupPosition).getLotes().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        //return itensResumo.indexOf(getGroup(groupPosition));
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        //return itensResumo.get(groupPosition).getLotes().indexOf(getChild(groupPosition,childPosition));
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int i) {
        return mapIndex.get(sections[i]);
    }

    @Override
    public int getSectionForPosition(int i) {
        return 0;
    }

    private class ViewHolderGroup{
        TextView txtDescricao;
    }

    private class ViewHolderChild{
        TextView txtId;
        TextView txtPeixe;
        TextView txtPesoLiquido;
        TextView txtPesoBruto;
        TextView txtQtdeCaixas;
        TextView txtDesconto;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ViewHolderGroup holder = null;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
            holder = new ViewHolderGroup();
            holder.txtDescricao = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolderGroup) convertView.getTag();
        }

        itemResumo = (ItemResumo) getGroup(groupPosition);
        int gp = groupPosition + 1;
        holder.txtDescricao.setText(gp + " - " + itemResumo.getPeixe().getDescricao() + " (" + getChildrenCount(groupPosition) + ")");

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ViewHolderChild holder = null;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item_lote, null);
            holder = new ViewHolderChild();
            holder.txtId = (TextView) convertView.findViewById(R.id.txtId);
            holder.txtPesoLiquido = (TextView) convertView.findViewById(R.id.txtLotePesoLiquido);
            holder.txtPesoBruto = (TextView) convertView.findViewById(R.id.txtLotePesoBruto);
            holder.txtQtdeCaixas = (TextView) convertView.findViewById(R.id.txtLoteQtdeCaixas);
            holder.txtDesconto = (TextView) convertView.findViewById(R.id.txtDesconto);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolderChild) convertView.getTag();
        }

        try {
            itemResumo = (ItemResumo) getGroup(groupPosition);

            int gp = groupPosition + 1;
            int cp = childPosition + 1;
            String id = gp + "." + cp;
            //holder.txtId.setText(itemResumo.getLotes().get(childPosition).getSequencia() + "");
            holder.txtId.setText(id);
            holder.txtPesoLiquido.setText(itemResumo.getLotes().get(childPosition).getPesoLiquido().toString() + "kg");
            holder.txtPesoBruto.setText(itemResumo.getLotes().get(childPosition).getPeso().toString() + "kg");
            holder.txtQtdeCaixas.setText(itemResumo.getLotes().get(childPosition).getQtdCaixas().toString());

            if (itemResumo.getLotes().get(childPosition).getDescontokg() != null && itemResumo.getLotes().get(childPosition).getDescontokg().compareTo(BigDecimal.ZERO) == 1)
                holder.txtDesconto.setText(" - " + itemResumo.getLotes().get(childPosition).getDescontokg().toString() + "kg");
            else
                holder.txtDesconto.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }
}
