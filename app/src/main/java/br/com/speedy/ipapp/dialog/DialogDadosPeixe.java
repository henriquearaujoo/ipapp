package br.com.speedy.ipapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import br.com.speedy.ipapp.R;
import br.com.speedy.ipapp.model.Compra;
import br.com.speedy.ipapp.model.Lote;
import br.com.speedy.ipapp.model.Peixe;
import br.com.speedy.ipapp.util.DialogUtil;
import br.com.speedy.ipapp.util.SessionApp;

/**
 * TODO: document your custom view class.
 */
public class DialogDadosPeixe extends DialogFragment {

    private Peixe peixe;

    private EditText editTextPeso;

    private EditText editTextQtdeCacapa;

    public DialogDadosPeixe(){

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.sample_dialog_dados_peixe, null);

        this.peixe = SessionApp.getPeixe();

        editTextPeso = (EditText) view.findViewById(R.id.peso);

        editTextQtdeCacapa = (EditText) view.findViewById(R.id.quantidade_cacapa);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String peso = editTextPeso.getText().toString();

                        String qtdeCacapa = editTextQtdeCacapa.getText().toString();

                        if (!peso.isEmpty() && !qtdeCacapa.isEmpty()) {
                            Lote lote = new Lote();
                            lote.setPeixe(peixe);
                            lote.setQtdCaixas(Integer.parseInt(qtdeCacapa));
                            lote.setPeso(new BigDecimal(peso));
                            lote.setPesoCacapa(SessionApp.getConfiguracoes().getPesoCacapa());
                            lote.setPesoLiquido(lote.getPeso().subtract(lote.getPesoCacapa().multiply(new BigDecimal(lote.getQtdCaixas()))));
                            lote.setValor(lote.getPesoLiquido().multiply(peixe.getValor()));
                            lote.setValorUnitarioPeixe(peixe.getValor());
                            lote.setDescontokg(BigDecimal.ZERO);

                            if (SessionApp.getLotes() == null) {
                                SessionApp.setLotes(new ArrayList<Lote>());
                                lote.setSequencia(1);
                                SessionApp.getLotes().add(lote);
                            } else {
                                lote.setSequencia(SessionApp.getLotes().size() + 1);
                                SessionApp.getLotes().add(lote);
                            }

                            if (SessionApp.getCompra() == null) {
                                SessionApp.setCompra(new Compra());
                                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss");
                                String codigo = sdf.format(new Date());
                                SessionApp.getCompra().setCodigo(codigo);
                            }

                            DialogUtil.showDialogInformacao(getActivity(), "Peixe adicionado com sucesso.");

                        }
                    }
                })
                .setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogDadosPeixe.this.getDialog().cancel();
                    }
                });

        builder.setTitle(peixe.getDescricao());

        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
