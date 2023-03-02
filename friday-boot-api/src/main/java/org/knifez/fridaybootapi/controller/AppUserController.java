package org.knifez.fridaybootapi.controller;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.knifez.fridaybootadmin.dto.AppUserDTO;
import org.knifez.fridaybootadmin.dto.AppUserModifyDTO;
import org.knifez.fridaybootadmin.dto.AppUserPagedQueryRequest;
import org.knifez.fridaybootadmin.entity.AppOrganizationUnit;
import org.knifez.fridaybootadmin.entity.AppUser;
import org.knifez.fridaybootadmin.service.IAppOrganizationUnitService;
import org.knifez.fridaybootadmin.service.IAppUserRoleService;
import org.knifez.fridaybootadmin.service.IAppUserService;
import org.knifez.fridaybootcore.annotation.ApiRestController;
import org.knifez.fridaybootcore.annotation.permission.AllowAuthenticated;
import org.knifez.fridaybootcore.dto.FridayResult;
import org.knifez.fridaybootcore.dto.PageResult;
import org.knifez.fridaybootcore.enums.ResultStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author KnifeZ
 * @since 2022-04-01
 */
@AllowAuthenticated
@Tag(name = "用户管理")
@ApiRestController
@RequestMapping("/user")
public class AppUserController {
    private final IAppUserService appUserService;

    private final IAppUserRoleService userRoleService;

    private final IAppOrganizationUnitService organizationUnitService;

    public AppUserController(IAppUserService appUserService, IAppUserRoleService userRoleService, IAppOrganizationUnitService organizationUnitService) {
        this.appUserService = appUserService;
        this.userRoleService = userRoleService;
        this.organizationUnitService = organizationUnitService;
    }

    /**
     * 分页列表
     *
     * @param queryRequest 查询请求
     * @return <{@link PageResult}<{@link AppUser}>
     */
    @Operation(summary = "分页列表")
    @PostMapping("list")
    public PageResult<AppUserDTO> pagedList(@RequestBody AppUserPagedQueryRequest queryRequest) {
        //查询列表数据
        var ret = appUserService.listByPageQuery(queryRequest);
        var responseList = BeanUtil.copyToList(ret.getItems(), AppUserDTO.class);
        var organizationList = organizationUnitService.list();
        for (AppUserDTO user : responseList) {
            var organization = organizationList.stream()
                    .filter(x -> x.getId().equals(user.getOrganizationId())).map(AppOrganizationUnit::getName).collect(Collectors.joining());
            user.setOrganizationName(organization);
        }
        var list = new PageResult<AppUserDTO>();
        list.setTotal(ret.getTotal());
        list.setItems(responseList);
        return list;
    }


    /**
     * 通过id获取用户
     *
     * @param id id
     * @return {@link AppUserDTO}
     */
    @GetMapping("{id}")
    @Operation(summary = "根据id获取用户")
    public AppUserDTO findById(@PathVariable Long id) {
        var user = appUserService.getById(id);
        AppUserDTO result = new AppUserDTO();
        BeanUtils.copyProperties(user, result);
        return result;
    }

    /**
     * 创建
     *
     * @param user 用户
     * @return {@link Boolean}
     */
    @PostMapping
    @Operation(summary = "新增用户")
    public Boolean create(@RequestBody AppUserModifyDTO user) {
        return appUserService.saveWithUserRoles(user, true);
    }

    /**
     * 更新
     *
     * @param user 用户
     * @return {@link Boolean}
     */
    @PostMapping("{id}")
    @Operation(summary = "修改用户")
    public Boolean update(@RequestBody AppUserModifyDTO user) {
        return appUserService.saveWithUserRoles(user, false);
    }

    /**
     * 删除
     *
     * @param id id
     * @return {@link FridayResult}<{@link Boolean}>
     */
    @DeleteMapping("{id}")
    @Operation(summary = "删除用户")
    public Boolean delete(@PathVariable Long id) {
        return appUserService.removeById(id);
    }


    /**
     * 检查帐号是否存在
     *
     * @param account 账户
     * @return {@link String}
     */
    @AllowAuthenticated
    @GetMapping("account-exist/{account}")
    @Operation(summary = "检查帐号是否存在")
    public FridayResult<String> exist(@PathVariable String account) {
        var user = appUserService.accountExist(account);
        if (user) {
            return FridayResult.fail(ResultStatus.FORBIDDEN_003);
        } else {
            return FridayResult.ok(account + "可使用");
        }
    }

    /**
     * 获取用户关联角色
     *
     * @param id userid
     * @return roleIds
     */
    @GetMapping("{id}/roles")
    @Operation(summary = "获取用户关联角色")
    public List<Long> getUserRoles(@PathVariable Long id) {
        return userRoleService.listRolesByUserId(id);
    }
}