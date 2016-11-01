package br.com.speedy.ipapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.speedy.ipapp.adapter.CompraAdapter;
import br.com.speedy.ipapp.model.Barco;
import br.com.speedy.ipapp.model.Compra;
import br.com.speedy.ipapp.model.Fornecedor;
import br.com.speedy.ipapp.model.Lote;
import br.com.speedy.ipapp.model.Peixe;
import br.com.speedy.ipapp.util.HttpConnection;
import br.com.speedy.ipapp.util.SessionApp;
import br.com.speedy.ipapp.util.SharedPreferencesUtil;


public class ComprasSalvasActivity extends ActionBarActivity implements Runnable{

    public static final int ATUALIZAR_LISTA = 1;

    public static final int BAIXAR_LOTES = 2;

    private ListView listViewCompras;

    private List<Compra> compras;

    private CompraAdapter adapter;

    private Thread threadCompras;

    private View comprasStatus;

    private View comprasLista;

    private Compra compra;

    private List<Lote> lotes;

    private ProgressDialog progressDialog;

    private String resposta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compras_salvas);

        comprasStatus = findViewById(R.id.compras_status);

        comprasLista = findViewById(R.id.compras_lista);

        showProgress(true);
        threadCompras = new Thread(this);
        threadCompras.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_compras_salvas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            comprasStatus.setVisibility(View.VISIBLE);
            comprasStatus.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            comprasStatus.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            comprasLista.setVisibility(View.VISIBLE);
            comprasLista.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            comprasLista.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            comprasStatus.setVisibility(show ? View.VISIBLE : View.GONE);
            comprasLista.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void getCompras(){

        callServer("get-json", "");

        Message msg = new Message();
        msg.what = ATUALIZAR_LISTA;
        handler.sendMessage(msg);
    }

    public void getPeixesJSON(String data){
        compras = new ArrayList<Compra>();

        try {
            JSONArray ja = new JSONArray(data);

            for (int i = 0; i < ja.length() ; i++) {

                Compra compra = new Compra();
                compra.setId(ja.getJSONObject(i).getLong("id"));
                compra.setDataCompra(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ja.getJSONObject(i).getString("dataCompraString")));
                compra.setCodigo(ja.getJSONObject(i).getString("codigo"));
                compra.setObservacao("");
                compra.setStatusCompra(ja.getJSONObject(i).getString("statusCompra"));
                //compra.setPause(ja.getJSONObject(i).getBoolean("pause"));
                //compra.setStatus(ja.getJSONObject(i).getBoolean("status"));
                compra.setLotes(new ArrayList<Lote>());
                compra.setValorTotal(new BigDecimal(ja.getJSONObject(i).getDouble("valorTotal")));

                JSONObject jsonFornec = ja.getJSONObject(i).getJSONObject("fornecedor");
                JSONObject jsonBarco = ja.getJSONObject(i).getJSONObject("barco");

                Fornecedor fornecedor = new Fornecedor();
                fornecedor.setId(jsonFornec.getLong("id"));
                fornecedor.setNome(jsonFornec.getString("nome"));

                Barco barco = new Barco();
                barco.setId(jsonBarco.getLong("id"));
                barco.setNome(jsonBarco.getString("nome"));

                compra.setFornecedor(fornecedor);
                compra.setBarco(barco);

                compras.add(compra);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getLotesJSON(String data){

        lotes = new ArrayList<Lote>();

        try{

            JSONArray ja = new JSONArray(data);

            for (int i = 0; i < ja.length() ; i++) {

                Lote lote = new Lote();
                Peixe p = new Peixe();

                try {

                    JSONObject loteJson = ja.getJSONObject(i);
                    JSONObject peixeJson = loteJson.getJSONObject("peixe");
                    p.setId(peixeJson.getLong("id"));
                    p.setDescricao(peixeJson.getString("descricao"));

                    lote.setId(loteJson.getLong("id"));
                    lote.setPeixe(p);
                    lote.setCompra(new Compra());
                    lote.setCompra(compra);
                    lote.setFornecedor(new Fornecedor());
                    lote.setFornecedor(compra.getFornecedor());
                    lote.setPeso(new BigDecimal(loteJson.getDouble("peso")));
                    lote.setQtdCaixas(loteJson.getInt("qtdCaixas"));
                    lote.setValor(new BigDecimal(loteJson.getDouble("valor")));
                    lote.setValorUnitarioPeixe(new BigDecimal(loteJson.getDouble("valorUnitarioPeixe")));
                    //lote.setSequencia(loteJson.getInt("sequencia"));
                    lote.setDescontokg(loteJson.has("descontokg") && !loteJson.get("descontokg").equals(null) ? new BigDecimal(loteJson.getDouble("descontokg")) : BigDecimal.ZERO);
                    lote.setDesconto(loteJson.has("desconto") && !loteJson.get("desconto").equals(null) ? new BigDecimal(loteJson.getDouble("desconto")) : BigDecimal.ZERO);
                    lote.setAcrescimo(loteJson.has("acrescimo") && !loteJson.get("acrescimo").equals(null) ? new BigDecimal(loteJson.getDouble("acrescimo")) : BigDecimal.ZERO);
                    lote.setPesoCacapa(new BigDecimal(loteJson.getDouble("pesoCacapa")));
                    lote.setPesoLiquido(lote.getPeso().subtract(lote.getPesoCacapa().multiply(new BigDecimal(lote.getQtdCaixas()))));

                    lotes.add(lote);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            SessionApp.setLotes(lotes);

            BigDecimal totalPeso = BigDecimal.ZERO;
            BigDecimal totalPesoLiquido = BigDecimal.ZERO;
            Integer totalCaixas = 0;

            for(Lote lote : lotes){
                totalPeso = totalPeso.add(lote.getPeso());
                totalPesoLiquido = totalPesoLiquido.add(lote.getPesoLiquido());
                totalCaixas = totalCaixas + lote.getQtdCaixas();
            }

            SessionApp.setTotalPesoBruto(totalPeso);
            SessionApp.setTotalPesoLiquido(totalPesoLiquido);
            SessionApp.setTotalCaixas(totalCaixas);

        }catch (Exception e){
            e.printStackTrace();
        }

        Message msg = new Message();
        msg.what = BAIXAR_LOTES;
        handler.sendMessage(msg);
    }

    private void callServer(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(ComprasSalvasActivity.this, "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(ComprasSalvasActivity.this, "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(ComprasSalvasActivity.this, "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws + "getComprasSalvas", method, data);

        if (!resposta.isEmpty())
            getPeixesJSON(resposta);
    }

    private void callServerLotes(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(ComprasSalvasActivity.this, "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(ComprasSalvasActivity.this, "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(ComprasSalvasActivity.this, "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws + "getLotesCompra", method, data);

        if (!resposta.isEmpty())
            getLotesJSON(resposta);
    }

    @Override
    public void run() {
        getCompras();
    }

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {

                case ATUALIZAR_LISTA:

                    adapter = new CompraAdapter(ComprasSalvasActivity.this, compras);

                    setListAdapter(adapter);

                    showProgress(false);

                    break;

                case BAIXAR_LOTES:

                    if (progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    finish();

                    break;
                default:
                    break;
            }
        }
    };

    public void getLotes(){

        progressDialog = ProgressDialog.show(ComprasSalvasActivity.this, "", "Carregando, aguarde.", false, false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                callServerLotes("post-json", compra.getId().toString());
            }
        }).start();
    }

    public void showDialogOpcoesCompra(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(ComprasSalvasActivity.this);

        builder.setTitle("Opções");

        String[] itens = {"Selecionar a compra", "Visualizar observações da compra"};

        builder.setItems(itens, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showDialogSelecionarCompra();
                        break;
                    case 1:
                        SessionApp.setCompra(compra);
                        Intent i = new Intent(ComprasSalvasActivity.this, ObservacoesActivity.class);
                        startActivity(i);
                        break;
                }
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showDialogSelecionarCompra(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(ComprasSalvasActivity.this);

        builder.setTitle("Seleção de compra");

        builder.setMessage("Confirma a seleção da compra " + compra.getCodigo() + "?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SessionApp.setCompra(compra);
                SessionApp.setFornecedor(compra.getFornecedor());
                SessionApp.setBarco(compra.getBarco());
                getLotes();
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {

        compra = (Compra) this.getListAdapter().getItem(position);

        showDialogOpcoesCompra();
    }

    public ListView getListView() {

        if (listViewCompras == null)
            listViewCompras = (ListView) findViewById(android.R.id.list);

        listViewCompras.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                onListItemClick((ListView) arg0, arg1, arg2, arg3);
            }

        });

        return listViewCompras;
    }

    public void setListAdapter (ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    protected ListAdapter getListAdapter() {
        ListAdapter adapter = getListView().getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter)adapter).getWrappedAdapter();
        } else {
            return adapter;
        }
    }

}
