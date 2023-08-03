package com.kclm.xsap.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kclm.xsap.entity.EmployeeEntity;
import com.kclm.xsap.service.EmployeeService;
import com.kclm.xsap.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;


/**
 * 员工表
 *
 * @author fangkai
 * @email fk_qing@163.com
 * @date 2021-12-04 16:18:21
 */

@Slf4j
@Controller
@RequestMapping("/user")
public class EmployeeController {
    private EmployeeService employeeService;

    @Autowired
    private void setApplicationContext(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping({"/toLogin", "/", "/login"})
    public String toLogin() {
        return "x_login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        LambdaQueryWrapper<EmployeeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(username), EmployeeEntity::getName, username);
//        .and(wrapper -> wrapper.eq(StringUtils.isNotEmpty(password), EmployeeEntity::getRolePassword, password)
        EmployeeEntity one = employeeService.getOne(queryWrapper);
        if (one == null || password.trim().equals(one.getRolePassword())) {
            model.addAttribute("USER_NOT_EXIST", false);
            return "x_login";
        }

        return "x_index_home";
    }

    /**
     * 跳转添加老师页面
     *
     * @return x_teacher_add.html
     */
    @GetMapping("/x_teacher_add.do")
    public String togoTeacherAdd() {
        return "employee/x_teacher_add";
    }

    /**
     * 跳转老师详情页
     *
     * @param id    老师id
     * @param model 携带老师id
     * @return x_teacher_list_data.html
     */
    @GetMapping("/x_teacher_list_data.do")
    public String togoTeacherListData(@RequestParam("id") Long id, Model model) {
        log.debug("\n==>前台传入的id：==>{}", id);
        model.addAttribute("ID", id);

        return "employee/x_teacher_list_data";
    }

    /**
     * 用户点击忘记密码跳转到充值页面
     *
     * @return x_ensure_user.html
     */
    @GetMapping("/toEnsureUser")
    public String toEnsureUser() {
        return "x_ensure_user";
    }

    /**
     * 携带数据跳转老师更新页面
     *
     * @param id 老师id
     * @return x_teacher_update.html（携带老师信息）
     */
    @GetMapping("/x_teacher_update")
    public ModelAndView updateTeacher(@RequestParam("id") Long id) {
        EmployeeEntity teacherById = employeeService.getById(id);

        ModelAndView mv = new ModelAndView();
        mv.addObject("teacherMsg", teacherById);
        mv.setViewName("employee/x_teacher_update");

        return mv;
    }

    /**
     * 菜单栏点击管理员类型跳转修改密码页面
     *
     * @return x_modify_password.html
     */
    @GetMapping("/x_modify_password.do")
    public String modifyPassword() {
        return "x_modify_password";
    }

    /**
     * 菜单栏点击管理员类型跳转个人资料页面
     *
     * @return x_profile.html
     */
    @GetMapping("/x_profile.do")
    public String profile(Long id, Model model) {
        EmployeeEntity employeeServiceById = employeeService.getById(id);
        model.addAttribute("userInfo", employeeServiceById);
        return "x_profile";
    }

    /**
     * 返回所有老师信息给suggest提供搜索建议
     *
     * @return 所有老师信息
     */
    @GetMapping("/toSearch.do")
    @ResponseBody
    public R toSearch() {
        List<EmployeeEntity> allEmployeeList = employeeService.list();
        log.debug("\n==>返回到前端的所有老师信息allEmployeeList==>{}", allEmployeeList);
        return new R().put("value", allEmployeeList);
    }

    /**
     * 获取所有员工信息并返回给前端
     *
     * @return R ->员工数据【json】
     */
    @PostMapping("/teacherList.do")
    @ResponseBody
    public R teacherList() {
        List<EmployeeEntity> teachers = employeeService.list();
        return new R().put("data", teachers);
    }


}