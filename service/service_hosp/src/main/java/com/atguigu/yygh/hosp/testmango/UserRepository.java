package com.atguigu.yygh.hosp.testmango;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User,String> {
    List getByNameAndAge(String name, int age);

    List getByNameLike(String name);
}
