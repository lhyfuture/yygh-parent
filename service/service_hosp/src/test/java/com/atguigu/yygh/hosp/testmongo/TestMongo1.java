package com.atguigu.yygh.hosp.testmongo;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/mongo1")
public class TestMongo1 {
    @Autowired
    private MongoTemplate mongoTemplate;

    //新增
    @GetMapping("create")
    public void create(){
        User user = new User();
        user.setAge(20);
        user.setName("test");
        user.setEmail("4932200@qq.com");
        //User user1 = mongoTemplate.insert(user);
        User user1 = mongoTemplate.insert(user);
        System.out.println("user1 = " + user1);
    }

    //查询所有
    @GetMapping("findAll")
    public void findAll(){
        //List<User> users = mongoTemplate.findAll(User.class);
        List<User> users = mongoTemplate.findAll(User.class);
        users.forEach(System.out::println);
    }

    //根据id查询
    @GetMapping("findById")
    public void findById(){
        User user = mongoTemplate.findById("62d7a3fdb583653eef56b026", User.class);
        System.out.println("user = " + user);
    }


    //条件查询
    @GetMapping("findUser")
    public void findUserList() {
        /*Query query = new Query(
                Criteria.where("name").is("test")
                        .and("age").is(20));
        List<User> users = mongoTemplate.find(query,User.class);*/
        Query query = new Query(
                Criteria.where("name").is("test")
                .and("age").is(20));
        List<User> users = mongoTemplate.find(query, User.class);
        users.forEach(System.out::println);
    }

    //模糊查询
    @GetMapping("findLike")
    public void findUsersLikeName() {
        String name = "est";
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Query query = new Query(
                Criteria.where("name").regex(pattern));
        List<User> users = mongoTemplate.find(query,User.class);
        users.forEach(System.out::println);
    }


    //条件查询
    @GetMapping("findPage")
    public void findUsersPage() {
        String name = "est";
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        int pageNo = 1;
        int pageSize = 10;
        //模糊查询
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Query query = new Query(
                Criteria.where("name").regex(pattern));
        //总记录数据,不在page记录数中
        //long total = mongoTemplate.count(query, User.class);
        Long total = mongoTemplate.count(query,User.class);
        //分页条件设置
        //query.skip((pageNo-1)*pageSize).limit(pageSize);
        query.skip((pageNo-1)*pageSize).limit(pageSize);
        List<User> users = mongoTemplate.find(query,User.class);
        users.forEach(System.out::println);
        System.out.println("total = " + total);
    }


    //修改
    @GetMapping("update")
    public void updateUser() {
        User user = mongoTemplate.findById("62d7a3fdb583653eef56b026", User.class);
        user.setName("test_1");
        user.setAge(25);
        user.setEmail("493220990@qq.com");
        //修改前先根据id查询对应数据
        Query query = new Query(Criteria.where("_id").is(user.getId()));
        Update update = new Update();
        update.set("name", user.getName());
        update.set("age", user.getAge());
        update.set("email", user.getEmail());
        UpdateResult result = mongoTemplate.upsert(query, update, User.class);
        long count = result.getModifiedCount();
        System.out.println(count);
    }

    //删除操作
    @GetMapping("delete")
    public void delete() {
        Query query =
                new Query(Criteria.where("_id").is("62d7a3fdb583653eef56b026"));
        DeleteResult result = mongoTemplate.remove(query, User.class);
        long count = result.getDeletedCount();
        System.out.println(count);
    }
    

}
