package de.ithoc.warehouse.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClientModel {

    private String name;
    private List<CustomerModel> customers = new ArrayList<>();
    private List<WarehouseModel> warehouseModels = new ArrayList<>();
    private List<ProductModel> productModels = new ArrayList<>();

}
