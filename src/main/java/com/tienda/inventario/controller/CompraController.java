package com.tienda.inventario.controller;

import com.tienda.inventario.dto.CompraRequest;
import com.tienda.inventario.entity.Compra;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.repository.CompraRepository;
import com.tienda.inventario.security.UsuarioPrincipal;
import com.tienda.inventario.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;
    private final CompraRepository compraRepository;

    @GetMapping
    public ResponseEntity<List<Compra>> listar() {
        return ResponseEntity.ok(compraRepository.findAllByOrderByFechaDesc());
    }

    @PostMapping
    public ResponseEntity<Compra> registrar(@Valid @RequestBody CompraRequest request,
                                             @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuario = principal.getUsuario();
        return ResponseEntity.ok(compraService.registrarCompra(request, usuario));
    }
}
