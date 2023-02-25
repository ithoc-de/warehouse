package de.ithoc.warehouse.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PackageModel {

    private String name;
    private String externalId;
    private List<ProductModel> productModels = new ArrayList<>();

}
