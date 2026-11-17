package br.edu.pos.apiprodutos.repository;

import br.edu.pos.apiprodutos.model.Produto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

public class ProdutoRepositoryMemoria implements ProdutoRepository {

    private final List<Produto> produtos = new ArrayList<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    @Override
    public synchronized void salvar(Produto produto) {

        if (produto.getId() == null) {
            produto.setId(proximoId.getAndIncrement());
        } else {
            produtos.removeIf(p -> p.getId().equals(produto.getId()));
        }
        produtos.add(produto);
    }

    @Override
    public synchronized List<Produto> listarTodos() {
        return new ArrayList<>(produtos);
    }

    @Override
    public synchronized Optional<Produto> buscarPorId(Long id) {

        return produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized void excluir(Long id) {

        produtos.removeIf(p -> p.getId().equals(id));
    }

    @Override
    public synchronized void adicionarEstoque(Long id, int quantidade) {

        Optional<Produto> produto = buscarPorId(id);

        if (produto.isEmpty()) {
            throw new NoSuchElementException("Produto não encontrado: " + id);
        }

        try {
            produto.get().setQuantidadeEstoque(Math.addExact(
                    produto.get().getQuantidadeEstoque(), quantidade));
        } catch (ArithmeticException erro) {
            throw new IllegalArgumentException("A movimentação ultrapassa o limite máximo de estoque.");
        }
    }

    @Override
    public synchronized void removerEstoque(Long id, int quantidade) {

        Optional<Produto> produto = buscarPorId(id);

        if (produto.isEmpty()) {
            throw new NoSuchElementException("Produto não encontrado: " + id);
        }

        int novoEstoque = produto.get().getQuantidadeEstoque() - quantidade;
        if (novoEstoque < 0) {
            throw new IllegalArgumentException("Estoque insuficiente para realizar a saída.");
        }

        produto.get().setQuantidadeEstoque(novoEstoque);
    }

    @Override
    public synchronized List<Produto> listarProdutosComEstoqueBaixo(int limite) {

        return produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() <= limite)
                .toList();
    }
}