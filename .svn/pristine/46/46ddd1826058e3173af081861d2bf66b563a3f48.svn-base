package br.com.speedy.ipapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.speedy.ipapp.model.Fornecedor;

public class FornecedorAdapter extends BaseAdapter {

	private List<Fornecedor> fornecedores;

	private Fornecedor fornecedor;

    private LayoutInflater mInflater;

	public FornecedorAdapter() {
		// TODO Auto-generated constructor stub
	}

	public FornecedorAdapter(Context context, List<Fornecedor> fornecedores){
		this.fornecedores = fornecedores;
		
		mInflater = (LayoutInflater)
	            context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}
	
	private class ViewHolder{
		TextView txtNome;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fornecedores.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return fornecedores.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return fornecedores.indexOf(getItem(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder = null;
		
		if (convertView == null) {
			convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
			holder = new ViewHolder();
			holder.txtNome = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		fornecedor = (Fornecedor) getItem(position);

		holder.txtNome.setText(fornecedor.getNome());

		return convertView;
	}

}
