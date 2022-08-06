package com.atguigu.yygh.hosp.testmongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mongo2")
public class TestMongo2 {
    //@Autowired
    //private UserRepository userRepository;

    @Autowired
    private UserRepository userRepository;

    //新增
    @GetMapping("create")
    public void create(){
        User user = new User();
        user.setAge(20);
        user.setName("张三");
        user.setEmail("3332200@qq.com");
        //User user1 = userRepository.save(user);
        User user1 = userRepository.save(user);
        System.out.println("user1 = " + user1);
    }

    //查询所有
    @GetMapping("findAll")
    public void findAll(){
        List<User> users = userRepository.findAll();
        users.forEach(System.out::println);
    }

    //根据id查询
    @GetMapping("findById")
    public void findById(){
        User user = userRepository.findById("62d7b15cf73f535ac207dff9").get();
        System.out.println("user = " + user);
    }

    //条件查询
    @GetMapping("findUser")
    public void findUserList() {
        User user = new User();
        user.setName("张三");
        user.setAge(20);
        //Example<User> example = Example.of(user);
        Example<User> example = Example.of(user);
        List<User> users = userRepository.findAll(example);
        users.forEach(System.out::println);
    }

    //模糊查询
    @GetMapping("findLike")
    public void findUsersLikeName() {
        User user = new User();
        user.setName("三");
        /*ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<User> example = Example.of(user,matcher);*/
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<User> example = Example.of(user,matcher);
        List<User> users = userRepository.findAll(example);
        users.forEach(System.out::println);
    }

    //分页查询
    @GetMapping("findPage")
    public void findUsersPage() {
        //Sort sort = Sort.by(Sort.Direction.DESC, "age");
        Sort sort = Sort.by(Sort.Direction.DESC,"age");
        //第一页从0开始
        //Pageable pageable = PageRequest.of(0,10,sort);
        Pageable pageable = PageRequest.of(0,10,sort);

        User user = new User();
        user.setName("三");

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<User> example = Example.of(user,matcher);
        Page<User> userPage = userRepository.findAll(example, pageable);
        System.out.println( userPage.getTotalElements());
        userPage.getContent().forEach(System.out::println);

    }


    //修改
    @GetMapping("update")
    public void updateUser() {
        User user = userRepository.findById("60b8d57ed539ed5b124942de").get();
        user.setName("张三_1");
        user.setAge(25);
        user.setEmail("883220990@qq.com");
        User save = userRepository.save(user);
        System.out.println(save);
    }

    //删除
    @GetMapping("delete")
    public void delete() {
        userRepository.deleteById("60b8d57ed539ed5b124942de");
    }


}
