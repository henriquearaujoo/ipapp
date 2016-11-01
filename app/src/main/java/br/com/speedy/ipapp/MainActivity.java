package br.com.speedy.ipapp;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import br.com.speedy.ipapp.fragment.FornecedorFragment;
import br.com.speedy.ipapp.fragment.PeixeFragment;
import br.com.speedy.ipapp.fragment.ResumoFragment;
import br.com.speedy.ipapp.model.Compra;
import br.com.speedy.ipapp.model.Configuracoes;
import br.com.speedy.ipapp.model.Lote;
import br.com.speedy.ipapp.util.HttpConnection;
import br.com.speedy.ipapp.util.SessionApp;
import br.com.speedy.ipapp.util.SharedPreferencesUtil;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener{

    public static final int SHOW_DIALOG_SALVAR_COMPRA = 1;
    public static final int SHOW_DIALOG_FINALIZAR_COMPRA = 2;
    public static final int SHOW_DIALOG_ENVIAR_IMPRESSAO = 3;
    public static final int DESCARTE_COMPRA = 4;
    public static final int ATUALIZAR_INCONSISTENCIAS = 5;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private ProgressDialog progressDialog;

    private Compra compra;

    private String resposta;

    private Fragment fragmentResumo;

    private Button btBuscar, btDescartar, btSalvarCompra, btFinalizarCompra;

    private EditText edtObservacao;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showDialogSair();
    }

    private Integer numComprasInconsistentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btBuscar = (Button) findViewById(R.id.btBuscarCompras);
        btDescartar = (Button) findViewById(R.id.btDescartar);
        btSalvarCompra = (Button) findViewById(R.id.btSalvarCompra);
        btFinalizarCompra = (Button) findViewById(R.id.btFinalizarCompra);

        btBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ComprasSalvasActivity.class);
                startActivity(i);
            }
        });

        btDescartar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogDescartarCompra();
            }
        });

        btSalvarCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SessionApp.getLotes() == null || SessionApp.getLotes().size() == 0)
                    showDialogErro("Você precisa adicionar pelo menos 1 (um) peixe a compra.");
                else if (SessionApp.getFornecedor() == null)
                    showDialogErro("Selecione um fornecedor antes de salvar a compra.");
                else
                    showDialogSalvarCompra();
            }
        });

        btFinalizarCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SessionApp.getLotes() == null || SessionApp.getLotes().size() == 0)
                    showDialogErro("Você precisa adicionar pelo menos 1 (um) peixe a compra.");
                else if (SessionApp.getFornecedor() == null)
                    showDialogErro("Selecione um fornecedor antes de finalizar a compra.");
                else {
                    //showDialogFinalizarCompra();
                    enviarImpressao();
                }
            }
        });

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.setSubtitle("Usuário: " + SessionApp.getUsuario().getNome());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.

            ActionBar.Tab tab = actionBar.newTab();
            //tab.setText(mSectionsPagerAdapter.getPageTitle(i));
            tab.setTabListener(this);

            if (i == 0) {
                tab.setIcon(R.drawable.ic_action_peixe);
                tab.setText("Peixe");
            }else if (i == 1) {
                tab.setIcon(R.drawable.ic_action_fornecedor);
                tab.setText("Fornecedor   ");
            }else if (i == 2) {
                tab.setIcon(R.drawable.ic_action_resumo);
                tab.setText("Resumo");
            }else {
                tab.setIcon(R.drawable.ic_action_finalizacao);
            }

            actionBar.addTab(tab);
        }

        carregarConfiguracao();

        iniciarVerificacaoComprasInconsistentes();
    }

    public void iniciarVerificacaoComprasInconsistentes(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try{

                        JSONObject object = new JSONObject();
                        object.put("id", SessionApp.getUsuario().getId());

                        String resposta = callServerComprasInconsistentes("post-jason", object.toString());

                        JSONObject object1 = new JSONObject(resposta);

                        numComprasInconsistentes = Integer.parseInt(object1.getString("num"));

                        Message msg = new Message();
                        msg.what = ATUALIZAR_INCONSISTENCIAS;
                        handler.sendMessage(msg);

                        Thread.sleep(5000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    private String callServerComprasInconsistentes(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(MainActivity.this, "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws + "getNumComprasInconsistentes", method, data);

        return resposta;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();

        switch (id){
            case R.id.action_buscar:
                Intent i = new Intent(MainActivity.this, ComprasSalvasActivity.class);
                startActivity(i);
                break;
            case R.id.action_descartar:
                showDialogDescartarCompra();
                break;
            case R.id.action_salvar:
                if (SessionApp.getLotes() == null || SessionApp.getLotes().size() == 0)
                    showDialogErro("Você precisa adicionar pelo menos 1 (um) peixe a compra.");
                else if (SessionApp.getFornecedor() == null)
                    showDialogErro("Selecione um fornecedor antes de salvar a compra.");
                else
                    showDialogSalvarCompra();
                break;
            case R.id.action_finalizar:
                if (SessionApp.getLotes() == null || SessionApp.getLotes().size() == 0)
                    showDialogErro("Você precisa adicionar pelo menos 1 (um) peixe a compra.");
                else if (SessionApp.getFornecedor() == null)
                    showDialogErro("Selecione um fornecedor antes de finalizar a compra.");
                else
                    showDialogFinalizarCompra();
                break;
            default:
                break;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void buscarCompras(){

    }

    public void salvarCompra(){

        progressDialog = ProgressDialog.show(MainActivity.this, "", "Salvando, aguarde.", false, false);

        compra = SessionApp.getCompra();

        compra.setObservacao(edtObservacao.getText() != null ? edtObservacao.getText().toString() : "");
        BigDecimal totalCP =  new BigDecimal(0);
        compra.setLotes(new ArrayList<Lote>());
        List<Lote> list = SessionApp.getLotes();
        for(Lote lote : list){
            totalCP = totalCP.add(lote.getValor());
            lote.setCompra(compra);
            lote.setFornecedor(SessionApp.getFornecedor());
            compra.getLotes().add(lote);
        }

        compra.setFornecedor(SessionApp.getFornecedor());
        compra.setBarco(SessionApp.getBarco());
        compra.setValorTotal(totalCP);
        compra.setDataCompra(new Date());
        compra.setStatus(false);
        compra.setPause(true);

        new Thread(new Runnable() {
            @Override
            public void run() {

                resposta = callServerSalvarCompra("post-json", generateJSONCompra());

                Message msg = new Message();
                msg.what = SHOW_DIALOG_SALVAR_COMPRA;
                handler.sendMessage(msg);

            }
        }).start();
    }

    public void finalizarCompra(){

        progressDialog = ProgressDialog.show(MainActivity.this, "", "Finalizando, aguarde.", false, false);

        compra = SessionApp.getCompra();
        /*SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmssmm");
        String codigo = sdf.format(new Date());
        compra.setCodigo(codigo);*/
        compra.setObservacao(edtObservacao.getText() != null ? edtObservacao.getText().toString() : "");
        BigDecimal totalCP =  new BigDecimal(0);
        compra.setLotes(new ArrayList<Lote>());
        List<Lote> list = SessionApp.getLotes();
        for(Lote lote : list){
            totalCP = totalCP.add(lote.getValor());
            lote.setCompra(compra);
            lote.setFornecedor(SessionApp.getFornecedor());
            compra.getLotes().add(lote);
        }

        compra.setFornecedor(SessionApp.getFornecedor());
        compra.setBarco(SessionApp.getBarco());
        compra.setValorTotal(totalCP);
        compra.setDataCompra(new Date());
        compra.setStatus(false);
        compra.setPause(false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                resposta = callServerSalvarCompra("post-json", generateJSONCompra());

                Message msg = new Message();
                msg.what = SHOW_DIALOG_FINALIZAR_COMPRA;
                handler.sendMessage(msg);

            }
        }).start();

    }

    public void enviarImpressao(){

        progressDialog = ProgressDialog.show(MainActivity.this, "", "Imprimindo, aguarde.", false, false);

        compra = SessionApp.getCompra();
        //compra.setObservacao(edtObservacao.getText() != null ? edtObservacao.getText().toString() : "");
        BigDecimal totalCP =  new BigDecimal(0);
        compra.setLotes(new ArrayList<Lote>());
        List<Lote> list = SessionApp.getLotes();
        for(Lote lote : list){
            totalCP = totalCP.add(lote.getValor());
            lote.setCompra(compra);
            lote.setFornecedor(SessionApp.getFornecedor());
            compra.getLotes().add(lote);
        }

        compra.setFornecedor(SessionApp.getFornecedor());
        compra.setBarco(SessionApp.getBarco());
        compra.setValorTotal(totalCP);
        compra.setDataCompra(new Date());
        //compra.setStatus(false);
        //compra.setPause(false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                resposta = callServerImpressao("post-json", generateJSONImpressao(compra));

                Message msg = new Message();
                msg.what = SHOW_DIALOG_ENVIAR_IMPRESSAO;
                handler.sendMessage(msg);
            }
        }).start();
    }

    public String generateJSONImpressao(Compra compra){
        JSONObject jo = new JSONObject();

        try{
            jo.put("conteudo", getConteudoImpressao(compra));
        }catch (Exception e){

        }

        return jo.toString();
    }

    public String getConteudoImpressao(Compra compra){
        //char[] cutP = new char[]{0x1d, 'V', 1};
        BigDecimal totalPesoLiquido = BigDecimal.ZERO;
        BigDecimal totalPesoBruto = BigDecimal.ZERO;
        Integer totalCaixas = 0;
        BigDecimal totalDescontos = BigDecimal.ZERO;;
        String conteudo = "  IRANDUBA PESCADOS \n\n";
        conteudo += "COMPROVANTE DE COMPRA \n\n";
        conteudo += "DATA: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(compra.getDataCompra()) + " \n";
        conteudo += "CODIGO: " + compra.getCodigo() + " \n";
        conteudo += "USUARIO: " + SessionApp.getUsuario().getNome() + " \n";
        conteudo += "FORNECEDOR: " + compra.getFornecedor().getNome() + " \n";
        conteudo += "TRANSPORTE: " + compra.getBarco().getNome() + " \n";
        conteudo += "ITENS: (" + compra.getLotes().size() + ") \n";
        conteudo += "----------------------------------------\n";

        for (Lote lote : compra.getLotes()) {
            BigDecimal pesoLiquido = new BigDecimal(String.valueOf(lote.getPeso().subtract(lote.getPesoCacapa().multiply(new BigDecimal(lote.getQtdCaixas())))));
            conteudo += lote.getPeixe().getDescricao() + "  " + lote.getPeso().toString() + "KG - " + lote.getQtdCaixas().toString() + " = " + pesoLiquido.toString() + "KG\n";

            totalPesoBruto = totalPesoBruto.add(lote.getPeso());
            totalPesoLiquido = totalPesoLiquido.add(pesoLiquido);
            totalDescontos = totalDescontos.add(lote.getDescontokg());
            totalCaixas = totalCaixas + lote.getQtdCaixas();
        }

        conteudo += "\nTOTAIS----------------------------------\n";
        conteudo += "TOTAL P. BRUTO: " + totalPesoBruto.toString() + "KG \n";
        conteudo += "TOTAL P. LIQUIDO: " + totalPesoLiquido.toString() + "KG \n";
        conteudo += "TOTAL DESCONTOS: " + totalDescontos.toString() + "KG \n";
        conteudo += "TOTAL CAIXAS: " + totalCaixas.toString() + " \n";

        conteudo +=	"----------------------------------------";

        //conteudo += "\n \n \n \n \n \n" + new String(cutP);

        return conteudo;
    }

    public String generateJSONCompra(){
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();

        try{
            jo.put("id", compra.getId());
            jo.put("codigo", compra.getCodigo());
            jo.put("observacao", compra.getObservacao() != null ? compra.getObservacao() : "");
            jo.put("dataCompra", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(compra.getDataCompra()));
            jo.put("pause", compra.getPause());
            jo.put("status", compra.getStatus());
            jo.put("valorTotal", compra.getValorTotal());
            jo.put("idUsuarioCompra", SessionApp.getUsuario().getId());
            jo.put("nomeUsuarioCompra", SessionApp.getUsuario().getNome());

            JSONObject joF = new JSONObject();
            joF.put("id", compra.getFornecedor().getId());
            joF.put("nome", compra.getFornecedor().getNome());

            JSONObject joB = new JSONObject();
            joB.put("id", compra.getBarco().getId());
            joB.put("nome", compra.getBarco().getNome());

            jo.put("fornecedor", joF);
            jo.put("barco", joB);

            for (Lote lote : compra.getLotes()){
                JSONObject joL = new JSONObject();
                joL.put("id", lote.getId());
                joL.put("peso", lote.getPeso());
                joL.put("qtdCaixas", lote.getQtdCaixas());
                joL.put("valor", lote.getValor());
                joL.put("valorUnitarioPeixe", lote.getValorUnitarioPeixe());
                joL.put("sequencia", lote.getSequencia());
                joL.put("descontokg", lote.getDescontokg() != null ? lote.getDescontokg() : BigDecimal.ZERO);
                joL.put("pesoCacapa", lote.getPesoCacapa());
                joL.put("desconto", lote.getDesconto() != null ? lote.getDesconto() : BigDecimal.ZERO);
                joL.put("acrescimo", lote.getAcrescimo() != null ? lote.getAcrescimo() : BigDecimal.ZERO);

                JSONObject joP = new JSONObject();
                joP.put("id", lote.getPeixe().getId());
                joP.put("descricao", lote.getPeixe().getDescricao());

                joL.put("peixe", joP);

                ja.put(joL);
            }

            jo.put("lotes", ja);
        }catch (Exception e){
            e.printStackTrace();
        }

        return jo.toString();
    }

    public String generateJSONDescarteCompra(){
        JSONObject jo = new JSONObject();

        try {
            jo.put("id", compra.getId());
        }catch (Exception e){

        }

        return jo.toString();
    }

    public void showDialogSalvarCompra(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.sample_dialog_finalizar_compra, null);

        edtObservacao = (EditText) view.findViewById(R.id.txtObservacaoFinalizacao);

        if (SessionApp.getCompra() != null && (SessionApp.getCompra().getObservacao() != null && !SessionApp.getCompra().getObservacao().equals("")))
            edtObservacao.setText(SessionApp.getCompra().getObservacao().toString());

        builder.setView(view);

        builder.setTitle("Salvar compra?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                salvarCompra();
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

    public void showDialogFinalizarCompra(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.sample_dialog_finalizar_compra, null);

        edtObservacao = (EditText) view.findViewById(R.id.txtObservacaoFinalizacao);

        if (SessionApp.getCompra() != null && (SessionApp.getCompra().getObservacao() != null && !SessionApp.getCompra().getObservacao().equals("")))
            edtObservacao.setText(SessionApp.getCompra().getObservacao().toString());

        builder.setView(view);

        builder.setTitle("Finalizar compra?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finalizarCompra();
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

    public void showDialogSucesso(String msg){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Sucesso");

        builder.setMessage(msg);

        builder.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showDialogErro(String msg){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Erro");

        builder.setMessage(msg);

        builder.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void showDialogDescartarCompra(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Deseja descartar esta compra e iniciar uma nova?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                descartarCompra();
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

    public void showDialogSair(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Deseja realmente sair?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
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

    public void descartarCompra(){

        progressDialog = ProgressDialog.show(MainActivity.this, "", "Descartando, aguarde.", false, false);

        compra = SessionApp.getCompra();

        if(compra != null && compra.getId() != null){
            new Thread(new Runnable() {
                @Override
                public void run() {

                    resposta = callServerDescartarCompra("post-json", generateJSONDescarteCompra());

                    Message msg = new Message();
                    msg.what = DESCARTE_COMPRA;
                    handler.sendMessage(msg);

                }
            }).start();
        }else{

            if (progressDialog != null && progressDialog.isShowing()){
                progressDialog.dismiss();
                progressDialog = null;
            }

            limparSessionApp();
        }


    }

    private String callServerDescartarCompra(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(MainActivity.this, "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws + "descartarCompra", method, data);

        return resposta;

    }

    private String callServerSalvarCompra(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(MainActivity.this, "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws + "salvarCompra", method, data);

        return resposta;

    }

    private String callServerImpressao(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(MainActivity.this, "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws + "salvarImpressao", method, data);

        return resposta;

    }

    public void getJSONConfiguracao(String data){
        Configuracoes configuracoes = new Configuracoes();

        try {

            JSONObject object = new JSONObject(data);
            configuracoes.setPesoCacapa(new BigDecimal(object.getString("pesoCacapa")));

            SessionApp.setConfiguracoes(configuracoes);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void carregarConfiguracao(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                while(true){
                    try {

                        String resposta = callServerConfiguracao("get-json", "");

                        getJSONConfiguracao(resposta);
                        //5 minutos
                        Thread.sleep(300000);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private String callServerConfiguracao(final String method, final String data){

        String ipServidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "ip_servidor");

        String endereco_ws = SharedPreferencesUtil.getPreferences(MainActivity.this, "endereco_ws");

        String porta_servidor = SharedPreferencesUtil.getPreferences(MainActivity.this, "porta_servidor");

        String resposta = HttpConnection.getSetDataWeb("http://" + ipServidor + ":" + porta_servidor + endereco_ws + "getConfiguracoes", method, data);

        return resposta;

    }

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {

                case SHOW_DIALOG_SALVAR_COMPRA:

                    if (progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    if (resposta.equals("true")) {
                        limparSessionApp();
                        showDialogSucesso("Compra salva com sucesso.");
                    }
                    else
                        showDialogErro("Ocorreu um erro ao salvar a compra.");

                    break;
                case SHOW_DIALOG_FINALIZAR_COMPRA:

                    if (progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    if (resposta.equals("true")) {
                        limparSessionApp();
                        showDialogSucesso("Compra finalizada com sucesso.");
                    }else
                        showDialogErro("Ocorreu um erro ao finalizar a compra.");

                    break;

                case SHOW_DIALOG_ENVIAR_IMPRESSAO:

                    if (progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    if (resposta.equals("true")) {
                        showDialogFinalizarCompra();
                    }else
                        showDialogErro("Ocorreu um erro ao enviar a impressao.");

                    break;
                case DESCARTE_COMPRA:

                    if (progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    if (resposta.equals("true")) {
                        limparSessionApp();
                    }else
                        showDialogErro("Ocorreu um erro ao descartar a compra.");
                    break;
                case ATUALIZAR_INCONSISTENCIAS:
                    if (numComprasInconsistentes > 0)
                        btBuscar.setTextColor(Color.YELLOW);
                    else
                        btBuscar.setTextColor(Color.WHITE);
                    break;
                default:
                    break;
            }
        }
    };

    public void limparSessionApp(){
        SessionApp.setLotes(null);
        SessionApp.setFornecedor(null);
        SessionApp.setBarco(null);
        SessionApp.setCompra(null);
        SessionApp.setTotalCaixas(0);
        SessionApp.setTotalPesoBruto(BigDecimal.ZERO);
        SessionApp.setTotalPesoLiquido(BigDecimal.ZERO);
        SessionApp.setTotalDescontos(BigDecimal.ZERO);

        onTabSelected(getSupportActionBar().getTabAt(0), null);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        /*if(tab.getPosition() == 2){
            ((ResumoFragment) fragmentResumo).runThread();
        }*/

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            Fragment fragment = null;

            switch (position){
                case 0:
                    fragment = PeixeFragment.newInstance(position + 1);
                    break;
                case 1:
                    fragment = FornecedorFragment.newInstance(position + 1);
                    break;
                case 2:
                    fragmentResumo = new ResumoFragment().newInstance(position + 1);
                    fragment = fragmentResumo;
                    break;
            }

            //return PlaceholderFragment.newInstance(position + 1);

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

}
