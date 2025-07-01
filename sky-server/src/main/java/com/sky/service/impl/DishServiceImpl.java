package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


    @Transactional
    public void savewithFlavor(DishDTO dishDTO) {
        Dish dish =new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //向菜品表插入1条数据
        dishMapper.insert(dish);

        //获取insert生成的主键值
        Long dishId = dish.getId();

        //向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });

            dishFlavorMapper.insert(flavors);
        }
    }


    public PageResult pagequery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.PageQuery(dishPageQueryDTO);
        long total = page.getTotal();
        List<DishVO> records = page.getResult();

        return new PageResult(total,records);
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {

        //判断是否在起售（status）
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                //当前在售
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断当前是否和套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() >0){
            throw  new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        /*
        //删除菜品表的数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品关联口味
            dishFlavorMapper.deleteByDishId(id);
        }
         */
        //减少使用for
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }


    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);

        //根据菜品id查询flavor
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        //封装vo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }


    public void updateWithFlavor(DishDTO dishDTO) {

        //修改菜品表基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //先全部删掉当前菜品的所有口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //再重新插入所有当前菜品口味
        List<DishFlavor> flavors =dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });

            dishFlavorMapper.insert(flavors);
        }

    }


}
