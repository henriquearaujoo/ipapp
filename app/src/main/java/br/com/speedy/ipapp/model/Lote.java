package br.com.speedy.ipapp.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Lote{
	
	public static final BigDecimal PESO_CACAPA = new BigDecimal(2);

	private Long id;
	private Peixe peixe;
	private Fornecedor fornecedor;
	private Compra compra;
	private BigDecimal peso;
	private BigDecimal pesoLiquido;
	private BigDecimal pesoUnitarioPeixe;
	private BigDecimal valorUnitarioPeixe;
	private BigDecimal valor;
	private Integer qtdCaixas;
    private Integer sequencia;
    private BigDecimal descontokg;
    private BigDecimal pesoCacapa;
	private BigDecimal desconto;
	private BigDecimal acrescimo;
	
	public Lote(){

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Peixe getPeixe() {
		return peixe;
	}

	public void setPeixe(Peixe peixe) {
		this.peixe = peixe;
	}

	
	public BigDecimal getPeso() {
		return peso;
	}

	public void setPeso(BigDecimal peso) {
		this.peso = peso;
	}
	
	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	
	public Fornecedor getFornecedor() {
		return fornecedor;
	}

	public void setFornecedor(Fornecedor fornecedor) {
		this.fornecedor = fornecedor;
	}
	
	public Integer getQtdCaixas() {
		return qtdCaixas;
	}

	public void setQtdCaixas(Integer qtdCaixas) {
		this.qtdCaixas = qtdCaixas;
	}


	public Compra getCompra() {
		return compra;
	}


	public void setCompra(Compra compra) {
		this.compra = compra;
	}


	public BigDecimal getPesoLiquido() {
		return pesoLiquido;
	}


	public void setPesoLiquido(BigDecimal pesoLiquido) {
		this.pesoLiquido = pesoLiquido;
	}


	public BigDecimal getPesoUnitarioPeixe() {
		return pesoUnitarioPeixe;
	}


	public void setPesoUnitarioPeixe(BigDecimal pesoUnitarioPeixe) {
		this.pesoUnitarioPeixe = pesoUnitarioPeixe;
	}


	public BigDecimal getValorUnitarioPeixe() {
		return valorUnitarioPeixe;
	}


	public void setValorUnitarioPeixe(BigDecimal valorUnitarioPeixe) {
		this.valorUnitarioPeixe = valorUnitarioPeixe;
	}


    public Integer getSequencia() {
        return sequencia;
    }

    public void setSequencia(Integer sequencia) {
        this.sequencia = sequencia;
    }

    public BigDecimal getDescontokg() {
        return descontokg;
    }

    public void setDescontokg(BigDecimal descontokg) {
        this.descontokg = descontokg;
    }

    public BigDecimal getPesoCacapa() {
        return pesoCacapa;
    }

    public void setPesoCacapa(BigDecimal pesoCacapa) {
        this.pesoCacapa = pesoCacapa;
    }

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
	}

	public BigDecimal getAcrescimo() {
		return acrescimo;
	}

	public void setAcrescimo(BigDecimal acrescimo) {
		this.acrescimo = acrescimo;
	}
}
