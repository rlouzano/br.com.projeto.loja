package com.example.demo.controller;

import com.example.demo.infra.FileSever;
import com.example.demo.model.Produto;
import com.example.demo.model.User;
import com.example.demo.respository.querys.ProdutoCustomRepository;
import com.example.demo.respository.UserRepository;
import com.example.demo.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.security.PermitAll;
import java.util.List;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoCustomRepository produtoCustomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private FileSever fileSever;

    private User usuario = new User();

    public ProdutoController(ProdutoCustomRepository produtoCustomRepository, ProdutoService produtoService, FileSever fileSever) {
        this.produtoCustomRepository = produtoCustomRepository;
        this.produtoService = produtoService;
        this.fileSever = fileSever;
    }

    @GetMapping("/listar")
    public ModelAndView listaAtivo(Produto produto, Model model) {
        buscarUsuarioLogado();
        List<Produto> produtos = produtoCustomRepository.getProdutoAtivo();
        ModelAndView mv = new ModelAndView("adm/listar");
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        mv.addObject("produtos", produtos);
        return mv;
    }

    /**
     * TELA DE LISTAGEM DE PRODUTO
     */
    @GetMapping("/produtos")
    public ModelAndView listaProdutos(Produto produto, Model model) {
        buscarUsuarioLogado();
        List<Produto> produtos = produtoCustomRepository.getProdutoAtivo();
        ModelAndView mv = new ModelAndView("produtos/index");
        mv.addObject("produtos", produtos);
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        return mv;
    }


    @GetMapping("/inativo")
    public ModelAndView listaInativo(Produto produto, Model model) {
        buscarUsuarioLogado();
        List<Produto> produtos = produtoCustomRepository.getProdutoInativo();
        ModelAndView mv = new ModelAndView("adm/inativo");
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        mv.addObject("produtos", produtos);
        return mv;
    }

    /*
        DETALHES DO PRODUTO
     */
    @GetMapping("/detalhes/{id}")
    public String produtoDetalhes(@PathVariable("id") Integer id, @ModelAttribute("prod") Produto produto, Model model) {
        buscarUsuarioLogado();
        if(usuario.getId() == null){
            model.addAttribute("prod", produtoService.listaPorUm(id));
            model.addAttribute("role", "null");
        }else{
            model.addAttribute("role", usuario.getRoles().iterator().next().getName());
            model.addAttribute("prod", produtoService.listaPorUm(id));
        }

        return "produtos/detalhes";
    }

    @GetMapping("/estoque")
    public ModelAndView listarEstoque(Produto produto, String nomeproduto) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("estoque/estoque");
        List<Produto> produtos = produtoService.listarTodos();
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        mv.addObject("produtos", produtos);
        return mv;
    }


    @GetMapping("/create")
    public ModelAndView buscaCadastro(Produto produto) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("produtos/create");
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        return mv;
    }

    @PostMapping("/create")
    public String create(MultipartFile sumario1, MultipartFile sumario2, MultipartFile sumario3, Produto produto) {
        String path1 = fileSever.write("/img", sumario1);
        String path2 = fileSever.write("/img", sumario2);
        String path3 = fileSever.write("/img", sumario3);
        produto.setImg1(path1);
        produto.setImg2(path2);
        produto.setImg3(path3);
        produtoService.adicionar(produto);
        return "redirect:/produtos/confirmar/" + produto.getId();
    }


    @GetMapping("/edit/{id}")
    public ModelAndView buscarEdit(@PathVariable Integer id) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("produtos/edit");
        Produto prod = produtoService.listaPorUm(id);
        mv.addObject("prod", prod);
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        return mv;
    }

    @PutMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, @ModelAttribute Produto produto) {
        produtoService.update(id, produto);
        return "redirect:/produtos/listar";

    }

    @PutMapping("/alterar/{id}")
    public String alterar(@PathVariable("id") Integer id, Produto produto) {
        produtoService.updateQuantidade(id, produto);
        return "redirect:/produtos/estoque?nomeproduto=";
    }

    @DeleteMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Integer id) {
        produtoService.excluir(id);
        return "redirect:/produtos/estoque?nomeproduto=";
    }

    @GetMapping("/categoria")
    public ModelAndView buscaPorCategoria(String info, Produto produto) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("/adm/pesquisa");
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        List<Produto> p = produtoCustomRepository.getProdutoPorFiltros(info+"%");
        for (Produto prod : p){
            if(!prod.getCategoria().equals(null)){
                mv.addObject("produtos", p);
                return mv;
            }
        }
        int n = Integer.parseInt(info);
        List<Produto> produtos = produtoCustomRepository.getProdutoPorFiltrosCodigo(n);
        mv.addObject("produtos", produtos);
        return mv;
    }

    @GetMapping("/genero")
    public ModelAndView buscaPorGenero(String info, Produto produto) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("produtos/categoria");
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        List<Produto> prod = produtoCustomRepository.getProdutoPorGenero(info);
        mv.addObject("lista", prod);
        return mv;
    }

    @GetMapping("/tamanho")
    public ModelAndView buscaPorTamanho(String info, Produto produto) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("produtos/categoria");
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        List<Produto> prodTamanho = produtoCustomRepository.getProdutoPorTamanho(info);
        mv.addObject("lista", prodTamanho);
        return mv;
    }
    @GetMapping("/pesquisa")
    public ModelAndView buscaPorPesquisa(String info, Produto produto) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("produtos/categoria");
        mv.addObject("role", usuario.getRoles().iterator().next().getName());
        List<Produto> prodTamanho = produtoCustomRepository.getProdutoPorCategoria(info);
        mv.addObject("lista", prodTamanho);
        return mv;
    }

    private void buscarUsuarioLogado() {
        Authentication autenticado = SecurityContextHolder.getContext().getAuthentication();
        if (!(autenticado instanceof AnonymousAuthenticationToken)) {
            String email = autenticado.getName();
            this.usuario = userRepository.buscaClienteEmail(email).get(0);
        }
    }
}