package br.com.speedy.ipapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.speedy.ipapp.R;
import br.com.speedy.ipapp.model.Fornecedor;
import br.com.speedy.ipapp.util.ItemResumo;

/**
 * Created by Henrique Araújo on 2015-03-06.
 */
public class FornecedorBarcoAdapter extends BaseExpandableListAdapter {

    private Fornecedor fornecedor;

    private List<Fornecedor> fornecedores;

    private Context context;

    public FornecedorBarcoAdapter(Context context, List<Fornecedor> fornecedores){
        this.context = context;
        this.fornecedores = fornecedores;
    }

    @Override
    public int getGroupCount() {

        return fornecedores.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return fornecedores.get(groupPosition).getBarcos().size();
    }

    @Override
    public Object getGroup(int groupPosition) {

        return fornecedores.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return fornecedores.get(groupPosition).getBarcos().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private class ViewHolderGroup{
        TextView txtNomeFornecedor;
    }

    private class ViewHolderChild{
        TextView txtId;
        TextView txtNomeBarco;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ViewHolderGroup holder = null;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
            holder = new ViewHolderGroup();
            holder.txtNomeFornecedor = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolderGroup) convertView.getTag();
        }

        fornecedor = (Fornecedor) getGroup(groupPosition);

        holder.txtNomeFornecedor.setText(fornecedor.getNome() + " - " + getChildrenCount(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ViewHolderChild holder = null;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item_fornecedor_barco, null);
            holder = new ViewHolderChild();
            holder.txtId = (TextView) convertView.findViewById(R.id.txtId);
            holder.txtNomeBarco = (TextView) convertView.findViewById(R.id.txtNomeBarco);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolderChild) convertView.getTag();
        }

        fornecedor = (Fornecedor) getGroup(groupPosition);

        int id = childPosition + 1;
        holder.txtId.setText(id + "");
        holder.txtNomeBarco.setText(fornecedor.getBarcos().get(childPosition).getNome());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }
}
