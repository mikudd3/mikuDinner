package org.mikudd3.reggie.dto;


import lombok.Data;
import org.mikudd3.reggie.entity.Dish;
import org.mikudd3.reggie.entity.DishFlavor;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
