package br.com.speedy.ipapp.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.speedy.ipapp.R;
import br.com.speedy.ipapp.adapter.FornecedorBarcoAdapter;
import br.com.speedy.ipapp.model.Barco;
import br.com.speedy.ipapp.model.Compra;
import br.com.speedy.ipapp.model.Fornecedor;
import br.com.speedy.ipapp.util.DialogUtil;
import br.com.speedy.ipapp.util.HttpConnection;
import br.com.speedy.ipapp.util.SessionApp;
import br.com.speedy.ipapp.util.SharedPreferencesUtil;

public class FornecedorFragment extends Fragment implements Runnable, SwipeRefreshLayout.OnRefreshListener {

    public static final int ATUALIZAR_LISTA = 1;
    public static final int ATUALIZAR_LISTA_SWIPE = 2;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<Fornecedor> fornecedores;

    private View fornecedorStatus;

    private View fornecedorLista;

    private View msgItensNaoEncontrados;

    private FornecedorBarcoAdapter adapter;

    private Thread threadFornecedores;

    private SwipeRefreshLayout refreshLayout;

    private ExpandableListView eListView;

    private Button btPesquisar;

    private EditText edtFiltroFornecedor;

    // TODO: Rename and change types of parameters
    public static FornecedorFragment newInstance(int position) {
        FornecedorFragment fragment = new FornecedorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FornecedorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fornecedor_list, container, false);

        eListView = (ExpandableListView) view.findViewById(R.id.eListFornecedor);

        eListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Barco b = fornecedores.get(groupPosition).getBarcos().get(childPosition);
                showDialogSelecionarFornecedor(b);
                return true;
            }
        });

        fornecedorLista = view.findViewById(R.id.fornecedor_lista);

        fornecedorStatus = view.findViewById(R.id.fornecedor_status);

        msgItensNaoEncontrados = view.findViewById(R.id.ff_msg_item_nao_encontrado);

        edtFiltroFornecedor = (EditText) view.findViewById(R.id.edtFFFiltroFornecedor);

        btPesquisar = (Button) view.findViewById(R.id.btFFPesquisar);

        btPesquisar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edtFiltroFornecedor.getText() == null || edtFiltroFornecedor.getText().toString().isEmpty()){
                    DialogUtil.showDialogAdvertencia(getActivity(), "Preencha o campo de pesquisa.");
                }else {
                    showProgress(true);
                    threadFornecedores = new Thread(FornecedorFragment.this);
                    threadFornecedores.start();
                }
            }
        });

        initSwipeDownToRefresh(view);

        return view;
    }

    public void getFornecedores(){

        JSONObject object = new JSONObject();
        try {
            object.put("nome", edtFiltroFornecedor.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        callServer("post-json", object.toString());

        Message msg = new Message();
        msg.what = ATUALIZAR_LISTA;
        handler.sendMessage(msg);
    }

    public void getFornecedoresJSON(String data){
        fornecedores = new ArrayList<Fornecedor>();

        try {
            JSONArray ja = new JSONArray(data);

            for (int i = 0; i < ja.length() ; i++) {
                Fornecedor p = new Fornecedor();
                p.setId(ja.getJSONObject(i).getLong("id"));
                p.setNome(ja.getJSONObject(i).getString("nome"));
                p.setCpf(ja.getJSONObject(i).getString("cpf"));
                p.setCnpj(ja.getJSONObject(i).getString("cnpj"));

                p.setBarcos(new ArrayList<Barco>());

                JSONArray jaB = ja.getJSONObject(i).getJSONArray("barcos");

                for (int j = 0; j < jaB.length() ; j++) {
                    Barco barco = new Barco();
                    barco.setId(jaB.getJSONObject(j).getLong("id"));
                    barco.setNome(jaB.getJSONObject(j).getString("nome"));
                    barco.setFornecedor(p);

                    p.getBarcos().add(barco);
                }


                fornecedores.add(p);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void callServer(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(getActivity(), "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(getActivity(), "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(getActivity(), "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws +"getFornecedoresFiltro", method, data);

        if (!resposta.isEmpty())
            getFornecedoresJSON(resposta);
    }

    @Override
    public void run() {

        getFornecedores();
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

            fornecedorStatus.setVisibility(View.VISIBLE);
            fornecedorStatus.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fornecedorStatus.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

        } else {
            refreshLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            fornecedorStatus.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {

                case ATUALIZAR_LISTA:

                    adapter = new FornecedorBarcoAdapter(getActivity(), fornecedores) ;

                    setListAdapter(adapter);

                    for (int i = 0; i < adapter.getGroupCount(); i++) {
                        eListView.expandGroup(i);
                    }

                    if (isAdded())
                        showProgress(false);

                    if (fornecedores != null && fornecedores.size() > 0) {
                        refreshLayout.setVisibility(View.VISIBLE);
                        msgItensNaoEncontrados.setVisibility(View.GONE);
                    }else{
                        refreshLayout.setVisibility(View.GONE);
                        msgItensNaoEncontrados.setVisibility(View.VISIBLE);
                    }

                    break;
                case ATUALIZAR_LISTA_SWIPE:

                    adapter = new FornecedorBarcoAdapter(getActivity(), fornecedores) ;

                    setListAdapter(adapter);

                    for (int i = 0; i < adapter.getGroupCount(); i++) {
                        eListView.expandGroup(i);
                    }

                    stopSwipeRefresh();

                    break;
                default:
                    break;
            }
        }
    };

    public void setListAdapter (ExpandableListAdapter adapter) {

        eListView.setAdapter(adapter);
    }

    public void showDialogSelecionarFornecedor(final Barco barco){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Salvar compra");

        builder.setMessage("Deseja selecionar o barco " + barco.getNome() + " do fornecedor " + barco.getFornecedor().getNome() + "?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SessionApp.setFornecedor(barco.getFornecedor());
                SessionApp.setBarco(barco);
                if (SessionApp.getCompra() == null) {
                    SessionApp.setCompra(new Compra());
                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmssmm");
                    String codigo = sdf.format(new Date());
                    SessionApp.getCompra().setCodigo(codigo);
                }
                DialogUtil.showDialogInformacao(getActivity(), "Fornecedor selecionado com sucesso.");
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
                    object.put("nome", edtFiltroFornecedor.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                callServer("post-json", object.toString());

                Message msg = new Message();
                msg.what = ATUALIZAR_LISTA_SWIPE;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void stopSwipeRefresh() {
        refreshLayout.setRefreshing(false);
    }
}
