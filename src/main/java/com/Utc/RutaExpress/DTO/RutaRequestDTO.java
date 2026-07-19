package com.Utc.RutaExpress.DTO;

import java.util.ArrayList;
import java.util.List;

public class RutaRequestDTO {

    private Long origenId;

    private Long destinoId;

    private List<Long> paradas = new ArrayList<>();

    public Long getOrigenId() {
        return origenId;
    }

    public void setOrigenId(Long origenId) {
        this.origenId = origenId;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public void setDestinoId(Long destinoId) {
        this.destinoId = destinoId;
    }

    public List<Long> getParadas() {
        return paradas;
    }

    public void setParadas(List<Long> paradas) {
        this.paradas = paradas;
    }

}