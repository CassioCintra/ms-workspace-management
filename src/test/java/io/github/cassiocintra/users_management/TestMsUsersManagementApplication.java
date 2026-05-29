package io.github.cassiocintra.users_management;

import org.springframework.boot.SpringApplication;

public class TestMsUsersManagementApplication {

    public static void main(String[] args) {
        SpringApplication.from(UsersManagementApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
