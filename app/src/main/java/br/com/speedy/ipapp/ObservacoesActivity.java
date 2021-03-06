package br.com.speedy.ipapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.speedy.ipapp.adapter.ObservacoesAdapter;
import br.com.speedy.ipapp.model.Observacoes;
import br.com.speedy.ipapp.util.HttpConnection;
import br.com.speedy.ipapp.util.SessionApp;
import br.com.speedy.ipapp.util.SharedPreferencesUtil;


public class ObservacoesActivity extends ActionBarActivity implements Runnable, SwipeRefreshLayout.OnRefreshListener {

    public static final int ATUALIZAR_LISTA = 1;
    public static final int ATUALIZAR_LISTA_SWIPE = 2;

    private SwipeRefreshLayout refreshLayout;

    private View obsStatus;

    private View obsLista;

    private View msgItensNaoEncontrados;

    private Thread threadObs;

    private ObservacoesAdapter adapter;

    private List<Observacoes> observacoes;

    private ListView listViewObservacoes;

    private String tipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observacoes);

        tipo = getIntent().getStringExtra("tipo");

        obsStatus = (View) findViewById(R.id.obs_status);
        msgItensNaoEncontrados = (View) findViewById(R.id.obs_msg_item_nao_encontrado);

        listViewObservacoes = (ListView) findViewById(android.R.id.list);

        initSwipeDownToRefresh(getWindow().getDecorView().getRootView());

        getSupportActionBar().setTitle("Observações da Compra");

        showProgress(true);
        threadObs = new Thread(this);
        threadObs.start();
    }

    public void getObservacoesJSON(String dados){
        observacoes = new ArrayList<Observacoes>();

        try{
            JSONObject object = new JSONObject(dados);

            try{
                JSONArray jObs = object.getJSONArray("observacoes");

                for (int i = 0; i < jObs.length() ; i++) {
                    Observacoes observacao = new Observacoes();
                    observacao.setData(jObs.getJSONObject(i).getString("data"));
                    observacao.setUsuario(jObs.getJSONObject(i).getString("usuario"));
                    observacao.setObservacao(jObs.getJSONObject(i).getString("observacao"));

                    observacoes.add(observacao);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void callServerObsCompra(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(ObservacoesActivity.this, "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(ObservacoesActivity.this, "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(ObservacoesActivity.this, "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws + "getObservacoesCompra", method, data);

        if (!resposta.isEmpty())
            getObservacoesJSON(resposta);
    }

    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            refreshLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            refreshLayout.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            refreshLayout.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });

            obsStatus.setVisibility(View.VISIBLE);
            obsStatus.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            obsStatus.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });
        } else {
            refreshLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            obsStatus.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {

                case ATUALIZAR_LISTA:

                    adapter = new ObservacoesAdapter(ObservacoesActivity.this, observacoes);

                    listViewObservacoes.setAdapter(adapter);

                    showProgress(false);

                    if (observacoes != null && observacoes.size() > 0) {
                        refreshLayout.setVisibility(View.VISIBLE);
                        msgItensNaoEncontrados.setVisibility(View.GONE);
                    }else{
                        refreshLayout.setVisibility(View.GONE);
                        msgItensNaoEncontrados.setVisibility(View.VISIBLE);
                    }

                    break;

                case ATUALIZAR_LISTA_SWIPE:

                    adapter = new ObservacoesAdapter(ObservacoesActivity.this, observacoes);

                    listViewObservacoes.setAdapter(adapter);

                    stopSwipeRefresh();

                    break;
                default:
                    break;
            }
        }
    };

    public void getObservacoes(){
        JSONObject object = new JSONObject();

        try {
            object.put("id", SessionApp.getCompra().getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        callServerObsCompra("post-json", object.toString());

        Message msg = new Message();
        msg.what = ATUALIZAR_LISTA;
        handler.sendMessage(msg);
    }

    public void initSwipeDownToRefresh(View view){
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorScheme(android.R.color.holo_blue_light,
                android.R.color.white, android.R.color.holo_blue_light,
                android.R.color.white);
    }

    @Override
    public void onRefresh() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                JSONObject object = new JSONObject();

                try {
                    object.put("id", SessionApp.getCompra().getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                callServerObsCompra("post-json", object.toString());

                Message msg = new Message();
                msg.what = ATUALIZAR_LISTA_SWIPE;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void stopSwipeRefresh() {
        refreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_observacoes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_close) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void run() {
        getObservacoes();
    }
}
