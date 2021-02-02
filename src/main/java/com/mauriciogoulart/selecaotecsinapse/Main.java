package com.mauriciogoulart.selecaotecsinapse;

import java.io.FileReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {

	private static class Produto {

		private String name;
		private Integer quantidade;
		private BigDecimal valor;
		private Calendar dia;

		public Produto() {
			this.quantidade = 0;
			this.valor = new BigDecimal("0");
			this.dia = Calendar.getInstance();
		}

		public Produto(String name, Integer quantidade, BigDecimal valor, Calendar dia) {
			this.name = name;
			this.quantidade = quantidade;
			this.valor = valor;
			this.dia = dia;
		}

		@Override
		public String toString() {
			return "Produto [name=" + name + ", quantidade=" + quantidade + ", valor=" + valor + ", dia="
					+ new SimpleDateFormat("dd-MM-yyyy").format(dia.getTime()) + "]";
		}

		public String getName() {
			return name;
		}

		public Integer getQuantidade() {
			return quantidade;
		}

		public BigDecimal getValor() {
			return valor;
		}

		public Calendar getDia() {
			return dia;
		}

	}

	public static void main(String[] args) throws ParseException {

		List<Produto> produtos = new ArrayList<>();

		JSONParser parser = new JSONParser();
		try (FileReader reader = new FileReader("pedidos.json")) {
			Object obj = parser.parse(reader);
			JSONArray produtosArray = (JSONArray) obj;
			produtosArray.forEach(produto -> {
				try {
					produtos.add(parserProduto((JSONObject) produto));
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});

			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		//agrupa todos os elementos da lista produtos em um map filtrado pelo mes de dezembro e agrupado por nome.
		Map<String, List<Produto>> produtosPorName = produtos.stream()
				.filter(produto -> produto.dia.get(Calendar.MONTH) == Calendar.DECEMBER)
				.collect(Collectors.groupingBy(Produto::getName));
		
		//percorre todos os elementos da lista e realiza os calculos de valor e quantidade;
		Map<String, Produto> totalProdutoPorNome = produtosPorName.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey,
						e -> e.getValue().stream().reduce(new Produto(),
								(total, produto) -> new Produto(produto.getName(),
										produto.getQuantidade() + total.getQuantidade(),
										produto.getValor().add(total.getValor()), produto.getDia()))));
		
		//Instancia de um treeMap para ordenar
		TreeMap<String, Produto> totalProdutoOrdenado = new TreeMap<>();
		
		//Ordena o TreeMap 
		totalProdutoPorNome.entrySet().forEach(entry -> totalProdutoOrdenado.put(entry.getKey(), entry.getValue()));
		
		//Verifica qual o item com maior quantidade e retorna ao objeto produto
		Produto produto = totalProdutoOrdenado.entrySet().stream()
				.max((e1, e2) -> e1.getValue().getQuantidade() - e2.getValue().getQuantidade())
				.map(entry -> entry.getValue()).orElse(null);
		
		//Atribuiu o nome do item e o valor final 
		String resultadoFinal = produto.getName() + "#" + produto.getValor();
		
		//Imprime o valor final
		System.out.println("Resultado Final: " + resultadoFinal);

	}
	
	// Metodo que converte os dados do arquivo JSON para os atributos da classe
	// Produto
	private static Produto parserProduto(JSONObject pProdutos) throws ParseException {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		cal.setTime(sdf.parse((String) pProdutos.get("dia")));

		return new Produto((String) pProdutos.get("item"), Integer.parseInt(pProdutos.get("quantidade").toString()),
				new BigDecimal(pProdutos.get("total").toString()), cal);

	}
}
