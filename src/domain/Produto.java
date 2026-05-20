package domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "produto")
public class Produto implements EntityInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sku", length = 64, nullable = false, unique = true)
    private String sku;

    @Column(name = "nome", length = 255, nullable = false)
    private String nome;

    @Column(name = "marca", length = 128)
    private String marca;

    @Column(name = "descricao", length = 1024)
    private String descricao;

    @Column(name = "preco", nullable = false)
    private Float preco;

    @OneToMany(
            mappedBy = "produto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Preco> historicoDePrecos = new ArrayList<>();

    @OneToMany(
            mappedBy = "produto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ProdutoLink> links = new ArrayList<>();

    public Produto() {
    }

    public Produto(String sku, String nome, String marca, String descricao, Float preco) {
        this.sku = sku;
        this.nome = nome;
        this.marca = marca;
        this.descricao = descricao;
        this.preco = preco;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Float getPreco() {
        return preco;
    }

    public void setPreco(Float preco) {
        this.preco = preco;
    }

    public List<Preco> getHistoricoDePrecos() {
        return historicoDePrecos;
    }

    public void setHistoricoDePrecos(List<Preco> historicoDePrecos) {
        this.historicoDePrecos = historicoDePrecos;
    }

    public List<ProdutoLink> getLinks() {
        return links;
    }

    public void setLinks(List<ProdutoLink> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", nome='" + nome + '\'' +
                ", marca='" + marca + '\'' +
                ", descricao='" + descricao + '\'' +
                ", preco=" + preco +
                '}';
    }
}
