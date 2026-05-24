package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class VectorSearchResult {

    private Long id;
    private Double score;

    public VectorSearchResult() {}

    public VectorSearchResult(Long id, Double score) {
        this.id = id;
        this.score = score;
    }

    public static VectorSearchResult of(Long id, Double score) {
        return new VectorSearchResult(id, score);
    }
}
