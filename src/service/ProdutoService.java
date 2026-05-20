package service;

import domain.EntityInterface;
import domain.Produto;
import infra.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import domain.Preco;
import domain.ProdutoLink;

public class ProdutoService implements ServiceInterface {

    @Override
    public void add(EntityInterface entity) {
        IO.println("Salvando o produto");
        Produto produto = (Produto) entity;
        
        // Cria o registro inicial do histórico de preços
        Preco precoInicial = new Preco();
        precoInicial.setPreco(produto.getPreco());
        precoInicial.setDataAtual(new Date());
        precoInicial.setProduto(produto);
        produto.getHistoricoDePrecos().add(precoInicial);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(produto);
            tx.commit();
        }
    }

    @Override
    public void salvarPreco(EntityInterface entity) {
        IO.println("Salvando o preço");
        Produto produto = (Produto) entity;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(produto);
            tx.commit();
        }
    }

    @Override
    public void remove(EntityInterface entity) {
        IO.println("Excluindo o produto");
        Produto produto = (Produto) entity;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Produto managed = session.get(Produto.class, produto.getId());
            if (managed != null) {
                session.remove(managed);
            }
            tx.commit();
        }
    }

    @Override
    public void list() {
        List<Produto> produtos = listar();
        for (int i = 0; i < produtos.size(); i++) {
            Produto p = produtos.get(i);
            System.out.printf("\nIndice: %s\n", i);
            System.out.printf("Id: %s\n", p.getId());
            System.out.printf("SKU: %s\n", p.getSku());
            System.out.printf("Nome: %s\n", p.getNome());
            System.out.printf("Descricao: %s\n", p.getDescricao());
            System.out.printf("Marca: %s\n", p.getMarca());
            System.out.printf("Preço Atual: %s\n", p.getPreco());
            
            System.out.println("Histórico de Preços:");
            if (p.getHistoricoDePrecos().isEmpty()) {
                System.out.println("  Nenhum registro encontrado.");
            } else {
                for (Preco h : p.getHistoricoDePrecos()) {
                    String infoLoja = (h.getLoja() != null) ? " | Loja: " + h.getLoja() : "";
                    System.out.printf("  - Data: %s | Valor: %s%s\n", h.getDataAtual(), h.getPreco(), infoLoja);
                }
            }
            
            System.out.println("Links Cadastrados:");
            if (p.getLinks() == null || p.getLinks().isEmpty()) {
                System.out.println("  Nenhum link cadastrado.");
            } else {
                for (ProdutoLink link : p.getLinks()) {
                    System.out.printf("  - %s: %s\n", link.getLoja(), link.getUrl());
                }
            }
            
            System.out.println("---------------------------------\n");
        }
    }

    @Override
    public EntityInterface findByIndex(int index) {
        List<Produto> produtos = listar();
        return produtos.get(index);
    }

    @Override
    public void edit(EntityInterface entity, UUID id) {
        IO.println("editando o produto");
        Produto atualizado = (Produto) entity;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Produto managed = session.get(Produto.class, id);
            if (managed != null) {
                managed.setSku(atualizado.getSku());
                managed.setNome(atualizado.getNome());
                managed.setMarca(atualizado.getMarca());
                managed.setDescricao(atualizado.getDescricao());
                
                // Se o preço foi alterado, adicionamos no histórico
                if (!managed.getPreco().equals(atualizado.getPreco())) {
                    Preco novoPreco = new Preco();
                    novoPreco.setPreco(atualizado.getPreco());
                    novoPreco.setDataAtual(new Date());
                    novoPreco.setProduto(managed);
                    
                    managed.getHistoricoDePrecos().add(novoPreco);
                    managed.setPreco(atualizado.getPreco());
                }
            }
            tx.commit();
        }
    }

    public void adicionarLink(UUID produtoId, String loja, String url) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Produto managed = session.get(Produto.class, produtoId);
            if (managed != null) {
                ProdutoLink link = new ProdutoLink(loja, url, managed);
                managed.getLinks().add(link);
                session.merge(managed);
            }
            tx.commit();
        }
    }

    public void popularDadosIniciais() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            
            Long count = session.createQuery("select count(p) from Produto p where p.nome = 'PlayStation 5'", Long.class).uniqueResult();
            
            if (count == null || count == 0) {
                Produto ps5 = new Produto("PS5-001", "PlayStation 5", "Sony", "Console PlayStation 5", 3799.00f);
                ps5.getLinks().add(new ProdutoLink("Amazon", "https://www.amazon.com.br/PlayStation-Console-PlayStation%C2%AE5/dp/B088GNRX3J/", ps5));
                ps5.getLinks().add(new ProdutoLink("Kabum", "https://www.kabum.com.br/produto/989702/console-sony-playstation-5-ssd-825gb-controle-sem-fio-dualsense-2-jogos-digitais-edicao-digital", ps5));
                
                Preco precoPs5 = new Preco();
                precoPs5.setPreco(3799.00f);
                precoPs5.setDataAtual(new Date());
                precoPs5.setProduto(ps5);
                ps5.getHistoricoDePrecos().add(precoPs5);
                
                session.persist(ps5);
            }

            Long countXbox = session.createQuery("select count(p) from Produto p where p.nome = 'Xbox One'", Long.class).uniqueResult();
            
            if (countXbox == null || countXbox == 0) {
                Produto xbox = new Produto("XBOX-001", "Xbox One", "Microsoft", "Console Xbox One", 2500.0f);
                xbox.getLinks().add(new ProdutoLink("Amazon", "https://www.amazon.com.br/Microsoft-All-Digital-Console-controle-branco/dp/B09P7CT2W6/", xbox));
                xbox.getLinks().add(new ProdutoLink("Kabum", "https://www.kabum.com.br/produto/200089/console-microsoft-xbox-series-s-512gb-branco-rrs-00006", xbox));
                
                Preco precoXbox = new Preco();
                precoXbox.setPreco(2500.0f);
                precoXbox.setDataAtual(new Date());
                precoXbox.setProduto(xbox);
                xbox.getHistoricoDePrecos().add(precoXbox);
                
                session.persist(xbox);
            }

            Long countIphone = session.createQuery("select count(p) from Produto p where p.nome = 'iPhone 17'", Long.class).uniqueResult();
            
            if (countIphone == null || countIphone == 0) {
                Produto iphone = new Produto("IPH17-001", "iPhone 17", "Apple", "Smartphone Apple iPhone 17 256GB", 8000.0f);
                iphone.getLinks().add(new ProdutoLink("Amazon", "https://www.amazon.com.br/Apple-iPhone-17-256-GB/dp/B0GQWK8Y7F/", iphone));
                iphone.getLinks().add(new ProdutoLink("Kabum", "https://www.kabum.com.br/produto/925367/iphone-17-apple-256gb-camera-dupla-fusion-48mp-tela-6-3-super-retina-xdr-preto", iphone));
                
                Preco precoIphone = new Preco();
                precoIphone.setPreco(8000.0f);
                precoIphone.setDataAtual(new Date());
                precoIphone.setProduto(iphone);
                iphone.getHistoricoDePrecos().add(precoIphone);
                
                session.persist(iphone);
            }
            
            tx.commit();
        }
    }

    private List<Produto> listar() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Produto> produtos = session.createQuery("select p from Produto p order by p.nome", Produto.class)
                    .getResultList();
            for (Produto p : produtos) {
                org.hibernate.Hibernate.initialize(p.getHistoricoDePrecos());
                org.hibernate.Hibernate.initialize(p.getLinks());
            }
            return produtos;
        }
    }
}
