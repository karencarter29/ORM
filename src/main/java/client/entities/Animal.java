package client.entities;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.ManyToOne;

@Entity("Animal")
public class Animal {

    @Id
    private Long id;

    @Column("NAME")
    private String name;

    @Column("AGE")
    private int age;

    @ManyToOne("ZOO_ID")
    private Zoo zoo;

    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setZoo(Zoo zoo) {
        this.zoo = zoo;
    }

    public Zoo getZoo() {
        return this.zoo;
    }

    @Override
    public String toString() {
        return "Animal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", zoo=" + zoo +
                '}';
    }
}
