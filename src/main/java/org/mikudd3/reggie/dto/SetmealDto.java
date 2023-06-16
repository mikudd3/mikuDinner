package org.mikudd3.reggie.dto;


import lombok.Data;
import org.mikudd3.reggie.entity.Setmeal;
import org.mikudd3.reggie.entity.SetmealDish;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
