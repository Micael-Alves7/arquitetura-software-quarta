import domain.Produto;
import infra.HibernateUtil;
import service.ProdutoService;
import service.ServiceInterface;
import service.CrawlerService;

void main() {
    ProdutoService service = new ProdutoService();
    CrawlerService crawlerService = new CrawlerService();
    
    // Popula dados iniciais (PS5 e Xbox One) com seus links
    service.popularDadosIniciais();

    try {
        boolean menuAtivo = true;
        while (menuAtivo) {
            int opcaoSelecionada = menu();
            switch (opcaoSelecionada) {
                case 1:
                    adicionarProduto(service);
                    break;
                case 2:
                    listarProdutos(service);
                    break;
                case 3:
                    editarProdutos(service);
                    break;
                case 4:
                    deletarProdutos(service);
                    break;
                case 5:
                    atualizarPrecoProduto(service);
                    break;
                case 6:
                    adicionarLinkAoProduto(service);
                    break;
                case 7:
                    crawlerService.executarCrawler();
                    break;
                case 0:
                    menuAtivo = false;
                    break;
            }
        }
    } finally {
        HibernateUtil.shutdown();
    }
}

public void adicionarProduto(ServiceInterface service) {
    String sku = IO.readln("Digite a SKU do produto: ");
    String nome = IO.readln("Digite o nome do produto: ");
    String marca = IO.readln("Digite a marca do produto: ");
    String descricao = IO.readln("Digite a descricao do produto: ");
    Float preco = Float.parseFloat(IO.readln("Digite o preco do produto: "));

    Produto produto = new Produto(sku, nome, marca, descricao, preco);

    service.add(produto);
}

public void listarProdutos(ServiceInterface service) {
    service.list();
}

public void editarProdutos(ServiceInterface service) {
    System.out.println("Atualmente temos os seguintes produtos cadastrados: ");
    service.list();
    int indice = Integer.parseInt(IO.readln("Digite o indice do produto que deseja editar"));

    Produto produto = (Produto) service.findByIndex(indice);
    produto.setSku(IO.readln("Informe o novo SKU do produto: "));
    produto.setNome(IO.readln("Informe o novo nome do produto: "));
    produto.setDescricao(IO.readln("Informe a nova descricao do produto: "));
    produto.setMarca(IO.readln("Informe a nova marca do produto: "));
    produto.setPreco(Float.parseFloat(IO.readln("Informe o novo preco do produto: ")));

    service.edit(produto, produto.getId());
}

public void deletarProdutos(ServiceInterface service) {
    System.out.println("Atualmente temos os seguintes produtos cadastrados: ");
    service.list();
    int indice = Integer.parseInt(IO.readln("Digite o indice do produto que deseja deletar: "));
    Produto produto = (Produto) service.findByIndex(indice);
    service.remove(produto);
}

public void atualizarPrecoProduto(ServiceInterface service) {
    System.out.println("Atualmente temos os seguintes produtos cadastrados: ");
    service.list();
    int indice = Integer.parseInt(IO.readln("Digite o indice do produto que deseja atualizar o preço: "));

    Produto produto = (Produto) service.findByIndex(indice);
    System.out.println("O preço atual do produto " + produto.getNome() + " é: " + produto.getPreco());
    
    float novoPreco = Float.parseFloat(IO.readln("Informe o novo preco do produto: "));
    produto.setPreco(novoPreco);

    service.edit(produto, produto.getId());
    System.out.println("Preço atualizado com sucesso!");
}

public void adicionarLinkAoProduto(ServiceInterface service) {
    System.out.println("Atualmente temos os seguintes produtos cadastrados: ");
    service.list();
    int indice = Integer.parseInt(IO.readln("Digite o indice do produto que deseja adicionar um link: "));

    Produto produto = (Produto) service.findByIndex(indice);
    
    String loja = IO.readln("Informe o nome da loja (ex: Amazon, Kabum): ");
    String url = IO.readln("Informe a URL do produto na loja: ");
    
    ((ProdutoService) service).adicionarLink(produto.getId(), loja, url);
    System.out.println("Link adicionado com sucesso!");
}

public Integer menu() {
    System.out.println("Digite a opção desejada: ");
    System.out.println("1 = Adicionar um novo produto");
    System.out.println("2 = Listar os produtos");
    System.out.println("3 = Editar um produto");
    System.out.println("4 = Deletar um produto");
    System.out.println("5 = Atualizar preço de um produto");
    System.out.println("6 = Adicionar Link de Loja a um produto");
    System.out.println("7 = Executar Crawler de Preços (Buscar menores preços)");
    System.out.println("0 = Sair");

    int opcao = Integer.parseInt(IO.readln());
    return opcao;
}
