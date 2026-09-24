package com.tienda.inventario.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.tienda.inventario.entity.Categoria;
import com.tienda.inventario.entity.Producto;
import com.tienda.inventario.repository.CategoriaRepository;
import com.tienda.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
    }

    public Producto obtenerPorCodigoBarras(String codigo) {
        return productoRepository.findByCodigoBarras(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con codigo: " + codigo));
    }

    public Producto obtenerPorCodigoCaja(String codigo) {
        return productoRepository.findByCodigoBarrasCaja(codigo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ningun producto tiene ese codigo registrado como caja: " + codigo));
    }

    // Un producto sin categoria queda "suelto" (no aparece en su grupo, ni en
    // los reportes por categoria): se exige siempre una categoria existente.
    private Categoria categoriaObligatoria(Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            throw new IllegalArgumentException(
                    "El producto debe tener una categoria. Elige una (o crea una en Categorias)");
        }
        return categoriaRepository.findById(categoria.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La categoria elegida no existe: " + categoria.getId()));
    }

    @Transactional
    public Producto crear(Producto producto) {
        producto.setCategoria(categoriaObligatoria(producto.getCategoria()));
        if (productoRepository.existsByCodigoBarras(producto.getCodigoBarras())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el codigo de barras: " + producto.getCodigoBarras());
        }
        if (producto.getCodigoBarrasCaja() != null && !producto.getCodigoBarrasCaja().isBlank()
                && productoRepository.existsByCodigoBarrasCaja(producto.getCodigoBarrasCaja())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese codigo de caja: " + producto.getCodigoBarrasCaja());
        }
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto producto = obtenerPorId(id);
        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecioVenta(datos.getPrecioVenta());
        producto.setPrecioCompra(datos.getPrecioCompra());
        producto.setStockMinimo(datos.getStockMinimo());
        producto.setCategoria(categoriaObligatoria(datos.getCategoria()));
        producto.setProveedor(datos.getProveedor());
        producto.setCodigoBarrasCaja(datos.getCodigoBarrasCaja());
        producto.setUnidadesPorCaja(datos.getUnidadesPorCaja());
        producto.setVendidoPorPeso(Boolean.TRUE.equals(datos.getVendidoPorPeso()));
        if (datos.getActivo() != null) {
            // Permite reactivar un producto desactivado enviando activo:true;
            // si el cliente no manda este campo, no se toca (evita desactivar por accidente)
            producto.setActivo(datos.getActivo());
        }
        // El stock NO se edita aqui directamente: siempre debe pasar por
        // InventarioService (venta, compra, ajuste) para quedar en el kardex.
        return productoRepository.save(producto);
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = obtenerPorId(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    public List<Producto> productosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    public byte[] generarImagenCodigoBarras(Long id) {
        Producto producto = obtenerPorId(id);
        return generarImagenCodigoBarras(producto.getCodigoBarras());
    }

    private byte[] generarImagenCodigoBarras(String codigo) {
        try {
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(codigo, BarcodeFormat.CODE_128, 300, 100);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new UncheckedIOException("No se pudo generar el codigo de barras", new IOException(e));
        }
    }
}
