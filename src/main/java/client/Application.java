package client;

import orm.manager.OrmManager;
import client.entities.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Application {

    List<Animal> list = new ArrayList<>();

    public static void main(String[] args) throws Exception {

//        OrmManager ormManager = OrmManager.get("DataBase");
//        ormManager.scanPackages(Application.class);
//
//        Zoo zoo = new Zoo("New York Zoo");
//        Animal animal1 = new Animal("Ricky", 4);
//        Animal animal2 = new Animal("Jack", 10);
//        zoo.addAnimal(animal1);
//        zoo.addAnimal(animal2);
//        ormManager.save(zoo);
//        ormManager.delete(animal1);
//        for (var el : zoo.getAnimals()) {
//            System.out.println(el);
//        }
    }
}


/*
delete from animal;
delete from zoo;


drop table animal;
drop table zoo;
drop table employee;
 */