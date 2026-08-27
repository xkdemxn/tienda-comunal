package com.tienda.inventario.service;

import com.tienda.inventario.dto.FiscalizacionAnualResponse;
import com.tienda.inventario.dto.FiscalizacionItemDto;
import com.tienda.inventario.dto.FiscalizacionMesDto;
import com.tienda.inventario.dto.FiscalizacionPorPesoDto;
import com.tienda.inventario.dto.FiscalizacionResponse;
import com.tienda.inventario.dto.ProductoStockBajoDto;
import com.tienda.inventario.dto.ReporteVentasResponse;
import com.tienda.inventario.entity.DetalleVenta;
import com.tienda.inventario.entity.MovimientoInventario;
import com.tienda.inventario.entity.Producto;
import com.tienda.inventario.entity.Venta;
import com.tienda.inventario.enums.TipoMovimiento;
import com.tienda.inventario.repository.DetalleVentaRepository;
import com.tienda.inventario.repository.MovimientoInventarioRepository;
import com.tienda.inventario.repository.ProductoRepository;
import com.tienda.inventario.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final GastoService gastoService;
    private final DeudorService deudorService;

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

    private static final String SIN_CATEGORIA = "SIN CATEGORIA";

    /**
     * Informe de Fiscalizacion Mensual: reconstruye, para cada producto activo,
     * el stock que tenia justo antes de "desde" y justo en "hasta" a partir del
     * kardex (MovimientoInventario), sin necesidad de guardar snapshots
     * historicos de stock. Formulas y agrupacion por categoria replican el
     * reporte original en Excel de la tienda.
     */
    public FiscalizacionResponse generarReporteFiscalizacion(LocalDateTime desde, LocalDateTime hasta) {
        List<Producto> productosActivos = productoRepository.findByActivoTrue();

        // Un solo query trae todo lo necesario: movimientos desde "desde" hasta ahora
        // ya cubre tambien la ventana desde "hasta" hasta ahora (es un subconjunto).
        Map<Long, List<MovimientoInventario>> movimientosPorProducto = movimientoInventarioRepository
                .findByFechaGreaterThanEqual(desde).stream()
                .collect(Collectors.groupingBy(m -> m.getProducto().getId()));

        List<FiscalizacionItemDto> items = new ArrayList<>();
        Map<String, List<FiscalizacionItemDto>> porCategoria = new LinkedHashMap<>();

        for (Producto producto : productosActivos) {
            // Los productos por peso (ej: carne) nunca pasan por el kardex a
            // proposito (ver VentaService) - si entraran aca les daria 0 en
            // todo. Se calculan aparte, mas abajo, directo de las ventas.
            if (Boolean.TRUE.equals(producto.getVendidoPorPeso())) {
                continue;
            }

            List<MovimientoInventario> movimientos = movimientosPorProducto
                    .getOrDefault(producto.getId(), List.of());

            List<MovimientoInventario> movimientosDesdeHasta = movimientos.stream()
                    .filter(m -> !m.getFecha().isBefore(hasta))
                    .toList();

            int saldoAnterior = producto.getStockActual() - sumaSigno(movimientos);
            int existenciaActual = producto.getStockActual() - sumaSigno(movimientosDesdeHasta);

            int entradaDelMes = movimientos.stream()
                    .filter(m -> !m.getFecha().isBefore(desde) && !m.getFecha().isAfter(hasta))
                    .filter(m -> esEntrada(m.getTipo()))
                    .mapToInt(MovimientoInventario::getCantidad)
                    .sum();

            int total = saldoAnterior + entradaDelMes;

            // Antes "cantidadVendida" salia de total-existenciaActual, lo que
            // mezclaba ventas reales con bajas por caducado/merma (ambas
            // restan igual del stock) e inflaba la ganancia con el margen de
            // unidades que en realidad se perdieron. Ahora se cuentan aparte,
            // directo del kardex del periodo, por tipo de movimiento.
            int cantidadVendida = movimientos.stream()
                    .filter(m -> !m.getFecha().isBefore(desde) && !m.getFecha().isAfter(hasta))
                    .filter(m -> m.getTipo() == TipoMovimiento.VENTA)
                    .mapToInt(MovimientoInventario::getCantidad)
                    .sum();
            int cantidadCaducada = movimientos.stream()
                    .filter(m -> !m.getFecha().isBefore(desde) && !m.getFecha().isAfter(hasta))
                    .filter(m -> m.getTipo() == TipoMovimiento.AJUSTE_NEGATIVO)
                    .mapToInt(MovimientoInventario::getCantidad)
                    .sum();

            BigDecimal precioVenta = producto.getPrecioVenta();
            BigDecimal precioCompra = producto.getPrecioCompra() != null ? producto.getPrecioCompra() : BigDecimal.ZERO;

            BigDecimal valorExistencia = precioVenta.multiply(BigDecimal.valueOf(existenciaActual));
            BigDecimal salidaConGanancia = precioVenta.multiply(BigDecimal.valueOf(cantidadVendida));
            BigDecimal salidaPrecioMercado = precioCompra.multiply(BigDecimal.valueOf(cantidadVendida));
            BigDecimal productoNetoExistentes = precioCompra.multiply(BigDecimal.valueOf(saldoAnterior));
            BigDecimal sumaGanancia = salidaConGanancia.subtract(salidaPrecioMercado);

            FiscalizacionItemDto item = new FiscalizacionItemDto(
                    producto.getNombre(), producto.getCodigoBarras(),
                    saldoAnterior, entradaDelMes, total, existenciaActual, valorExistencia,
                    cantidadVendida, cantidadCaducada, salidaConGanancia, salidaPrecioMercado,
                    productoNetoExistentes, sumaGanancia
            );

            items.add(item);
            String categoria = producto.getCategoria() != null ? producto.getCategoria().getNombre() : SIN_CATEGORIA;
            porCategoria.computeIfAbsent(categoria, k -> new ArrayList<>()).add(item);
        }

        BigDecimal totalGeneralGanancia = items.stream()
                .map(FiscalizacionItemDto::getSumaGanancia)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FiscalizacionPorPesoDto> productosPorPeso = calcularProductosPorPeso(desde, hasta);
        BigDecimal gananciaPorPeso = productosPorPeso.stream()
                .map(FiscalizacionPorPesoDto::gananciaEstimada)
                .filter(g -> g != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalGeneralGanancia = totalGeneralGanancia.add(gananciaPorPeso);

        BigDecimal totalGastos = gastoService.totalEnRango(desde, hasta);
        BigDecimal q = totalGeneralGanancia.subtract(totalGastos);
        BigDecimal deudasPendientes = deudorService.totalPendiente();

        BigDecimal totalVentasBruto = ventaRepository.findByFechaBetween(desde, hasta).stream()
                .map(Venta::getTotalMonedaLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FiscalizacionResponse(porCategoria, totalGeneralGanancia, totalVentasBruto, totalGastos, q,
                deudasPendientes, productosPorPeso);
    }

    private static final String[] NOMBRES_MES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    /**
     * Progreso anual (Enero-Diciembre) para el grafico de lineas al final de
     * Fiscalizacion. Reutiliza generarReporteFiscalizacion mes a mes: no hace
     * falta guardar snapshots historicos por anio, el kardex ya permite
     * reconstruir cualquier mes pasado bajo demanda. Los anios anteriores
     * quedan "congelados" (los 12 meses ya sucedieron); el anio en curso solo
     * trae datos hasta el mes actual, y sigue completandose mes a mes.
     */
    public FiscalizacionAnualResponse generarReporteFiscalizacionAnual(int anio) {
        LocalDate hoy = LocalDate.now();
        int mesLimite = (anio == hoy.getYear()) ? hoy.getMonthValue() : 12;

        List<FiscalizacionMesDto> meses = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            if (mes > mesLimite) {
                meses.add(new FiscalizacionMesDto(mes, NOMBRES_MES[mes - 1], null, null));
                continue;
            }

            LocalDateTime desde = LocalDateTime.of(anio, mes, 1, 0, 0);
            LocalDateTime hasta = (mes == mesLimite && anio == hoy.getYear())
                    ? LocalDateTime.now()
                    : desde.plusMonths(1).minusSeconds(1);

            FiscalizacionResponse reporteMes = generarReporteFiscalizacion(desde, hasta);
            meses.add(new FiscalizacionMesDto(mes, NOMBRES_MES[mes - 1],
                    reporteMes.getTotalGeneralGanancia(), reporteMes.getTotalVentasBruto()));
        }

        return new FiscalizacionAnualResponse(anio, meses);
    }

    // Productos por peso (ej: carne) no tienen kardex, asi que se calculan
    // directo de DetalleVenta en vez de MovimientoInventario.
    private List<FiscalizacionPorPesoDto> calcularProductosPorPeso(LocalDateTime desde, LocalDateTime hasta) {
        // Se agrupa por id (no por la entidad directamente) para no depender
        // de que Producto tenga equals/hashCode propios.
        Map<Long, List<DetalleVenta>> porProductoId = detalleVentaRepository
                .findConPesoEnRango(desde, hasta).stream()
                .collect(Collectors.groupingBy(d -> d.getProducto().getId()));

        return porProductoId.values().stream()
                .map(detalles -> {
                    Producto producto = detalles.get(0).getProducto();

                    BigDecimal pesoVendido = detalles.stream()
                            .map(DetalleVenta::getPeso)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal ingresoTotal = detalles.stream()
                            .map(DetalleVenta::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal gananciaEstimada = null;
                    if (producto.getPrecioCompra() != null) {
                        BigDecimal costoTotal = producto.getPrecioCompra().multiply(pesoVendido);
                        gananciaEstimada = ingresoTotal.subtract(costoTotal);
                    }

                    return new FiscalizacionPorPesoDto(producto.getNombre(), pesoVendido, ingresoTotal, gananciaEstimada);
                })
                .toList();
    }

    private boolean esEntrada(TipoMovimiento tipo) {
        return tipo == TipoMovimiento.COMPRA || tipo == TipoMovimiento.DEVOLUCION
                || tipo == TipoMovimiento.AJUSTE_POSITIVO;
    }

    // Suma "cantidad" de cada movimiento con signo: positivo para
    // COMPRA/DEVOLUCION/AJUSTE_POSITIVO, negativo para VENTA/AJUSTE_NEGATIVO.
    private int sumaSigno(List<MovimientoInventario> movimientos) {
        int suma = 0;
        for (MovimientoInventario m : movimientos) {
            suma += (esEntrada(m.getTipo()) ? 1 : -1) * m.getCantidad();
        }
        return suma;
    }
}
