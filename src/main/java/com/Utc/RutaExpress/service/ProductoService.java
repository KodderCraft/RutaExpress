package com.Utc.RutaExpress.service;
import com.Utc.RutaExpress.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.Utc.RutaExpress.entity.Producto;

@Service

public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository producto) {
        this.productoRepository = producto;
    }

    public Producto guardarProducto( Producto producto){
        return productoRepository.save(producto);
    }

    public List<Producto> listarProductos(){
        return productoRepository.findAll();
    }

}
