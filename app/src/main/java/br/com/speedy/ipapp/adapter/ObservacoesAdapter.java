package br.com.speedy.ipapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.speedy.ipapp.R;
import br.com.speedy.ipapp.model.Observacoes;

public class ObservacoesAdapter extends BaseAdapter {

    private List<Observacoes> observacoes;

    private Observacoes observacao;

    private LayoutInflater mInflater;

    public ObservacoesAdapter() {
    }

    public ObservacoesAdapter(Context context, List<Observacoes> observacoes){
        this.observacoes = observacoes;

        mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    private class ViewHolder{
        TextView txtId;
        TextView txtObservacao;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return observacoes.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return observacoes.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return observacoes.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_observacoes, null);
            holder = new ViewHolder();
            holder.txtId = (TextView) convertView.findViewById(R.id.txtIOId);
            holder.txtObservacao = (TextView) convertView.findViewById(R.id.txtIOObservacao);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        observacao = (Observacoes) getItem(position);

        String id = (position + 1) + "";

        holder.txtId.setText(id);
        holder.txtObservacao.setText(observacao.getUsuario() + " em " + observacao.getData() + ": " + observacao.getObservacao());

        return convertView;
    }

}
