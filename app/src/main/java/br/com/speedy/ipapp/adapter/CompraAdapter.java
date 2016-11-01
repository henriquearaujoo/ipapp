package br.com.speedy.ipapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import br.com.speedy.ipapp.R;
import br.com.speedy.ipapp.model.Compra;
import br.com.speedy.ipapp.model.Fornecedor;

public class CompraAdapter extends BaseAdapter {

	private List<Compra> compras;

	private Compra compra;

    private LayoutInflater mInflater;

	public CompraAdapter() {
		// TODO Auto-generated constructor stub
	}

	public CompraAdapter(Context context, List<Compra> compras){
		this.compras = compras;
		
		mInflater = (LayoutInflater)
	            context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}
	
	private class ViewHolder{
		TextView txtCodigo;
        TextView txtFornecedor;
        TextView txtDataCompra;
		View llCompraSalva;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return compras.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return compras.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return compras.indexOf(getItem(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder = null;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_compra, null);
			holder = new ViewHolder();
			holder.txtCodigo = (TextView) convertView.findViewById(R.id.txtCompraCodigo);
            holder.txtFornecedor = (TextView) convertView.findViewById(R.id.txtCompraFornecedor);
            holder.txtDataCompra = (TextView) convertView.findViewById(R.id.txtCompraData);
			holder.llCompraSalva = (View) convertView.findViewById(R.id.llCompraSalva);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		compra = (Compra) getItem(position);

		holder.txtCodigo.setText(compra.getCodigo());
        holder.txtDataCompra.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(compra.getDataCompra()));
        holder.txtFornecedor.setText(compra.getFornecedor().getNome());

		if (compra.getStatusCompra().equals("RETORNADO_INICIO")) {
			holder.llCompraSalva.setBackgroundColor(Color.YELLOW);
		}else {
			holder.llCompraSalva.setBackgroundColor(Color.WHITE);
		}

		return convertView;
	}

}
