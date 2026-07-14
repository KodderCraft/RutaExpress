package com.Utc.RutaExpress.service;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.Utc.RutaExpress.repository.ClienteRepository;
import com.Utc.RutaExpress.DTO.loginValidar;
import com.Utc.RutaExpress.entity.Cliente;
import com.Utc.RutaExpress.DTO.loginValidar;
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository; 

    public ClienteService (ClienteRepository cliente){
        this.clienteRepository = cliente; 
    }

    public Cliente guardarCliente(Cliente cliente){
        return clienteRepository.save(cliente);
    }

    public Boolean login(loginValidar login){
        Optional<Cliente> cliente = clienteRepository.findBynombre(login.getnombre());

        if(cliente.isEmpty()){
            return false;
        }
        return cliente.get().getpassword().equals(login.getPassword());
    }

}
