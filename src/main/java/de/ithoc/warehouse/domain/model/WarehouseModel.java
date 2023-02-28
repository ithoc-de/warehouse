package de.ithoc.warehouse.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WarehouseModel {

    private String name;
    private List<PackageModel> packageModels = new ArrayList<>();

}
