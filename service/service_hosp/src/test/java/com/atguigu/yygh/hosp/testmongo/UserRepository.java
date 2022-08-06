package com.atguigu.yygh.hosp.testmongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository//MongoRepository接口后面的泛型是对应实体类和表id的类型
public interface UserRepository extends MongoRepository<User,String> {
}
