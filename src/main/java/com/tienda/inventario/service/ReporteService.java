package com.tienda.inventario.service;

import com.tienda.inventario.dto.ProductoStockBajoDto;
import com.tienda.inventario.dto.ReporteVentasResponse;
import com.tienda.inventario.entity.DetalleVenta;
import com.tienda.inventario.entity.Producto;
import com.tienda.inventario.entity.Venta;
import com.tienda.inventario.repository.ProductoRepository;
import com.tienda.inventario.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    /**
     * Reporte de ventas en un rango de fechas: total vendido, cantidad de ventas,
     * productos mas vendidos y alerta de productos con poco stock.
     */
    public ReporteVentasResponse generarReporte(LocalDateTime desde, LocalDateTime hasta) {
        List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta);

        BigDecimal totalVendido = ventas.stream()
                .map(Venta::getTotalMonedaLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer> productosMasVendidos = ventas.stream()
                .flatMap(v -> v.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getNombre(),
                        LinkedHashMap::new,
                        Collectors.summingInt(DetalleVenta::getCantidad)
                ))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        List<ProductoStockBajoDto> stockBajo = productoRepository.findProductosConStockBajo().stream()
                .map(p -> new ProductoStockBajoDto(p.getNombre(), p.getCodigoBarras(),
                        p.getStockActual(), p.getStockMinimo()))
                .toList();

        return new ReporteVentasResponse(totalVendido, ventas.size(), productosMasVendidos, stockBajo);
    }
}
