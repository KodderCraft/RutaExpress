package com.Utc.RutaExpress.DTO;

import java.util.List;

public class RutaResponseDTO {

    private List<String> camino;

    public RutaResponseDTO(List<String> camino) {
        this.camino = camino;
    }

    public List<String> getCamino() {
        return camino;
    }

    public void setCamino(List<String> camino) {
        this.camino = camino;
    }
}