package service;

import domain.Preco;
import domain.Produto;
import domain.ProdutoLink;
import infra.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class CrawlerService {

    public void executarCrawler() {
        System.out.println("Iniciando o crawler de preços...");
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Produto> produtos = session.createQuery("select p from Produto p", Produto.class)
                    .getResultList();

            for (Produto p : produtos) {
                org.hibernate.Hibernate.initialize(p.getLinks());
                org.hibernate.Hibernate.initialize(p.getHistoricoDePrecos());
            }

            for (Produto produto : produtos) {
                System.out.println("\nBuscando preços para: " + produto.getNome());
                
                Float menorPrecoEncontrado = null;
                String lojaMenorPreco = null;

                for (ProdutoLink link : produto.getLinks()) {
                    System.out.println("  Acessando " + link.getLoja() + "...");
                    Float preco = buscarPreco(link.getUrl(), link.getLoja());
                    
                    if (preco != null) {
                        System.out.println("    Preço encontrado: R$ " + preco);
                        if (menorPrecoEncontrado == null || preco < menorPrecoEncontrado) {
                            menorPrecoEncontrado = preco;
                            lojaMenorPreco = link.getLoja();
                        }
                    } else {
                        System.out.println("    Preço não encontrado ou acesso bloqueado.");
                    }
                }

                if (menorPrecoEncontrado != null) {
                    System.out.println("  -> Menor preço para " + produto.getNome() + " é R$ " + menorPrecoEncontrado + " na " + lojaMenorPreco);
                    
                    // Adiciona ao histórico se houver alteração
                    Transaction tx = session.beginTransaction();
                    try {
                        Preco novoPreco = new Preco();
                        novoPreco.setPreco(menorPrecoEncontrado);
                        novoPreco.setDataAtual(new Date());
                        novoPreco.setProduto(produto);
                        novoPreco.setLoja(lojaMenorPreco);
                        
                        produto.getHistoricoDePrecos().add(novoPreco);
                        produto.setPreco(menorPrecoEncontrado); // Atualiza o preço principal do produto
                        
                        session.merge(produto);
                        tx.commit();
                        System.out.println("  Histórico de preços atualizado!");
                    } catch (Exception e) {
                        tx.rollback();
                        System.out.println("  Erro ao salvar histórico: " + e.getMessage());
                    }
                } else {
                    System.out.println("  -> Nenhum preço encontrado para este produto.");
                }
            }
        }
        System.out.println("\nCrawler finalizado.");
    }

    private Float buscarPreco(String url, String loja) {
        try {
            // Usa User-Agent para tentar evitar bloqueios básicos
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            String precoString = null;

            if (loja.toLowerCase().contains("amazon")) {
                // Tenta buscar o preço na Amazon
                Element elPrice = doc.selectFirst(".a-price .a-offscreen, .a-color-price");
                if (elPrice != null) {
                    precoString = elPrice.text();
                }
            } else if (loja.toLowerCase().contains("kabum")) {
                // Tenta buscar o preço no Kabum
                Element elPrice = doc.selectFirst("h4[class^='sc-'], b[class*='regularPrice']");
                if (elPrice != null) {
                    precoString = elPrice.text();
                } else {
                    // Tenta um fallback procurando R$
                    String body = doc.body().text();
                    int idx = body.indexOf("R$");
                    if (idx != -1 && idx + 15 < body.length()) {
                        precoString = body.substring(idx, idx + 15);
                    }
                }
            } else {
                // Lógica genérica simples
                String body = doc.body().text();
                int idx = body.indexOf("R$");
                if (idx != -1 && idx + 15 < body.length()) {
                    precoString = body.substring(idx, idx + 15);
                }
            }

            if (precoString != null) {
                return parsePreco(precoString);
            }

        } catch (IOException e) {
            System.err.println("    Erro ao acessar " + url + ": " + e.getMessage());
        }
        return null;
    }

    private Float parsePreco(String texto) {
        try {
            // Remove R$, espaços e converte pontos de milhar e vírgulas decimais
            String limpo = texto.replaceAll("[^0-9,.]", "");
            
            // Corrige formato brasileiro (ex: 3.799,00 -> 3799.00)
            if (limpo.contains(",") && limpo.contains(".")) {
                limpo = limpo.replace(".", ""); // Remove pontos de milhar
                limpo = limpo.replace(",", "."); // Substitui vírgula decimal por ponto
            } else if (limpo.contains(",")) {
                limpo = limpo.replace(",", ".");
            }
            
            return Float.parseFloat(limpo);
        } catch (Exception e) {
            return null;
        }
    }
}
